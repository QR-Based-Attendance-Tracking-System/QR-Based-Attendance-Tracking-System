# Authentication deployment

The browser and Next.js app should share an origin. Next.js proxies `/api/*` to Spring Boot, so the browser only sees the opaque `ATTENDANCE_SESSION` cookie. The cookie is `HttpOnly`, `SameSite=Lax`, and `Secure` by default. Spring stores only the SHA-256 digest of the 256-bit session token and expires sessions after eight hours. All unsafe browser requests obtain a Spring CSRF token and send it in `X-CSRF-TOKEN`.

Passwords use Spring Security's Argon2id encoder. Student and Lecturer credentials use the existing `users` table; profile data is written to the existing `students` or `lecturers` table. Flyway migration `V6` adds optional email, normalized uniqueness indexes, and persisted sessions.

## Environment

Configure `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` through the deployment secret manager. Do not commit production secrets. Set `APP_SECURITY_ALLOWED_ORIGINS` to the exact frontend origin(s). Keep `APP_SECURITY_COOKIE_SECURE=true` in production; set it to `false` only for local HTTP development.

Account creation requires distinct, high-entropy `APP_STUDENT_REGISTRATION_CODE` and `APP_LECTURER_REGISTRATION_CODE` values. Leave them unset to disable registration. Supply each code through an out-of-band faculty process. This project does not currently have email verification or a password-reset delivery service.

`backend/.env.example` lists the supported variable names for local setup. Export them before starting Spring Boot; the application does not load `.env` files itself.

Before internet-facing deployment, apply distributed rate limits and alerting to `/api/auth/login` and the two registration endpoints at the API gateway/WAF. This repository does not include a distributed rate-limit store. Password recovery is also intentionally not simulated; users must contact the faculty until a verified email/reset delivery flow is configured.
