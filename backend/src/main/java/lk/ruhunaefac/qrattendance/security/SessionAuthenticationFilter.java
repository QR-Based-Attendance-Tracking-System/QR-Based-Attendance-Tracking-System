package lk.ruhunaefac.qrattendance.security;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lk.ruhunaefac.qrattendance.auth.repository.AuthSessionRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SessionAuthenticationFilter extends OncePerRequestFilter {
    public static final String COOKIE_NAME = "ATTENDANCE_SESSION";
    private final AuthSessionRepository sessions;
    public SessionAuthenticationFilter(AuthSessionRepository sessions) { this.sessions = sessions; }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = cookie(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String tokenHash;
            try { tokenHash = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
            catch (java.security.NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
            sessions.findByTokenHashAndExpiresAtAfter(tokenHash, Instant.now()).ifPresent(session -> {
                var user = session.getUser();
                var principal = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                        .password("").authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())).build();
                var auth = UsernamePasswordAuthenticationToken.authenticated(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(request, response);
    }
    public static String cookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) if (COOKIE_NAME.equals(cookie.getName())) return cookie.getValue();
        return null;
    }
}
