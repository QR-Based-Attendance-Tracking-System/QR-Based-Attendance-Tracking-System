package lk.ruhunaefac.qrattendance.security;

import java.time.Duration;
import java.util.Arrays;
import lk.ruhunaefac.qrattendance.auth.repository.AuthSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new Argon2PasswordEncoder(16, 32, 1, 19456, 2); }
    @Bean DaoAuthenticationProvider authenticationProvider(DatabaseUserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users); provider.setPasswordEncoder(encoder); return provider;
    }
    @Bean AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) { return new org.springframework.security.authentication.ProviderManager(provider); }

    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, AuthSessionRepository sessions,
            CorsConfigurationSource corsConfigurationSource, @Value("${app.security.cookie-secure:true}") boolean secureCookie) throws Exception {
        var csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepo.setHeaderName("X-CSRF-TOKEN");
        csrfRepo.setCookiePath("/"); csrfRepo.setCookieCustomizer(cookie -> cookie.sameSite("Lax").secure(secureCookie));
        var csrfHandler = new CsrfTokenRequestAttributeHandler(); csrfHandler.setCsrfRequestAttributeName(null);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.csrfTokenRepository(csrfRepo).csrfTokenRequestHandler(csrfHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register/student", "/api/auth/register/lecturer", "/api/auth/csrf", "/api/auth/logout").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/attendance/sessions", "/api/attendance/sessions/*/end").hasRole("LECTURER")
                .requestMatchers(HttpMethod.GET, "/api/attendance/lecturer-dashboard", "/api/attendance/sessions/*/records", "/api/attendance/sessions/*/qr-challenge", "/api/attendance/sessions/latest").hasRole("LECTURER")
                .requestMatchers(HttpMethod.POST, "/api/attendance/check-in", "/api/attendance/check-in/code").hasRole("STUDENT")
                .requestMatchers("/api/attendance/**").authenticated()
                .requestMatchers("/api/lecturers/me/**").hasRole("LECTURER")
                .requestMatchers("/actuator/health", "/error").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new SessionAuthenticationFilter(sessions), UsernamePasswordAuthenticationFilter.class)
            .httpBasic(basic -> basic.disable()).formLogin(form -> form.disable()).logout(logout -> logout.disable());
        return http.build();
    }
    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.security.allowed-origins:http://localhost:3000}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::strip).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Content-Type", "X-XSRF-TOKEN", "X-CSRF-TOKEN"));
        config.setAllowCredentials(true); config.setExposedHeaders(java.util.List.of("X-CSRF-TOKEN")); config.setMaxAge(Duration.ofHours(1));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/api/**", config); return source;
    }
}
