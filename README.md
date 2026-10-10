# QR Attendance

QR Attendance is a web application for lecturers to create attendance sessions and for students to check in by scanning a QR code or entering a session code.

## Project structure

```text
qr-attendance/
├── backend/                         # Java 25, Spring Boot REST API
│   ├── src/main/java/...            # API organized by feature
│   │   ├── attendance/              # Attendance workflows grouped by capability
│   │   │   ├── session/             # Session lifecycle and its web/data classes
│   │   │   ├── checkin/             # Student QR/code check-in and records
│   │   │   ├── challenge/           # Time-limited QR token generation/validation
│   │   │   └── reporting/           # Lecturer dashboard and attendance views
│   │   │   └── service/              # Shared session lifecycle and ownership rules
│   │   ├── auth/                    # Registration, login, persisted sessions
│   │   ├── course/                  # Lecturer courses and enrollment
│   │   ├── lecturehall/              # Lecture hall operations
│   │   ├── security/                # Session authentication and security config
│   │   └── user/                    # Student, lecturer, and admin users
│   ├── src/main/resources/          # Spring configuration
│   ├── src/test/                    # Backend tests
│   ├── .env.example                 # Backend environment variable names
│   ├── AUTHENTICATION.md            # Authentication and deployment notes
│   ├── mvnw                         # Maven wrapper
│   └── pom.xml
├── database/
│   └── migrations/                  # Flyway PostgreSQL migrations (V1–V6)
├── docs/                            # Project documentation
└── frontend/                        # Next.js and React application
    ├── src/app/                     # Routes and layouts
    ├── src/components/              # Shared UI components
    ├── src/features/                # Auth, attendance, dashboard, lecturer, student
    ├── .env.local                   # Local frontend API URL
    └── package.json
```

## Requirements

- Java 25
- Node.js and npm
- PostgreSQL

The backend uses PostgreSQL and applies the SQL files in `database/migrations` through Flyway on startup. Hibernate validates the schema; it does not create or update it.

## Local setup

### 1. Create the PostgreSQL user and database

Using `psql` or another PostgreSQL admin tool, create a local role and database. Replace the example password with one of your choice:

```sql
CREATE USER qr_attendance_app WITH PASSWORD 'change-me';
CREATE DATABASE qr_attendance_db OWNER qr_attendance_app;
```

The defaults expect PostgreSQL on `localhost:5432`. If your local connection differs, set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` accordingly.

### 2. Start the backend

In terminal 1, from the repository root:

```bash
cd backend
export DATABASE_URL=jdbc:postgresql://localhost:5432/qr_attendance_db
export DATABASE_USERNAME=qr_attendance_app
export DATABASE_PASSWORD='change-me'
export APP_SECURITY_COOKIE_SECURE=false
export APP_SECURITY_ALLOWED_ORIGINS=http://localhost:3000
export APP_STUDENT_REGISTRATION_CODE=student-local
export APP_LECTURER_REGISTRATION_CODE=lecturer-local
export ATTENDANCE_QR_TOKEN_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`. Registration codes are required to create student and lecturer accounts; use the local values above in the corresponding sign-up flow. Leave a registration code unset to disable registration for that role. Keep `APP_SECURITY_COOKIE_SECURE=false` only for local HTTP development. For production, use long, random registration secrets instead of the local example values.

Instead of exporting variables each time, copy `backend/.env.example` as a reference and set the variables in your shell or IDE run configuration. The application does not automatically load `.env` files.

### 3. Start the frontend

In terminal 2, from the repository root:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000`. The checked-in `frontend/.env.local` sets `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080` for local development.

### 4. Use the app

Create a student or lecturer account using its matching registration code, then sign in. Lecturers can create attendance sessions and display the QR code; students can scan it or enter the session code to check in. The browser uses the frontend's `/api` routes to communicate with the backend.

## Tests and checks

Run the backend test suite from `backend/`:

```bash
./mvnw test
```

Run the frontend ESLint check from `frontend/`:

```bash
npm run lint
```

The frontend package currently has no dedicated automated test script. The lint command checks the frontend code; it does not run browser-based end-to-end tests.

## Configuration reference

Backend settings are read from environment variables; the defaults are defined in `backend/src/main/resources/application.yaml` and listed in `backend/.env.example`:

| Variable | Purpose |
| --- | --- |
| `DATABASE_URL` | JDBC URL for PostgreSQL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Database credentials |
| `APP_SECURITY_COOKIE_SECURE` | Set to `false` for local HTTP; keep `true` for HTTPS deployments |
| `APP_SECURITY_ALLOWED_ORIGINS` | Allowed frontend origin(s), such as `http://localhost:3000` |
| `APP_STUDENT_REGISTRATION_CODE` | Code required for student registration |
| `APP_LECTURER_REGISTRATION_CODE` | Code required for lecturer registration |
| `ATTENDANCE_QR_TOKEN_SECRET` | HMAC secret used to sign short-lived attendance QR tokens; use a high-entropy secret and keep it private |
| `ATTENDANCE_QR_TOKEN_VALIDITY_SECONDS` | Lifetime of a QR token and attendance code (default: `30` seconds) |

See [`backend/AUTHENTICATION.md`](backend/AUTHENTICATION.md) for session, security, and deployment details.
