# Notes API (Java / Spring Boot)

A secure multi-user notes backend with JWT authentication, note sharing, search, pagination, soft delete, and archived notes.

## Tech stack

- **Java 21** + **Spring Boot 3.2**
- **Spring Security** + **JWT** (JJWT)
- **BCrypt** password hashing
- **Spring Data JPA**
- **SQLite** (local) or **PostgreSQL** (production)
- **OpenAPI 3** via springdoc (`GET /openapi.json`)

## Prerequisites

- JDK 21+
- Maven 3.9+
- (Optional) PostgreSQL 14+ for production profile

## Quick start (SQLite)

```bash
cd notes_app
cp .env.example .env
# Edit JWT_SECRET and ABOUT_* in .env if desired

mvn clean package -DskipTests
mvn spring-boot:run
```

The API listens on `http://localhost:8080` by default.

### Environment variables

Copy `.env.example` to `.env` and set:

| Variable | Description |
|----------|-------------|
| `PORT` | HTTP port (default `8080`) |
| `SPRING_PROFILES_ACTIVE` | `sqlite` or `postgres` |
| `JWT_SECRET` | HMAC secret (min 32 chars) |
| `JWT_EXPIRATION_MS` | Token lifetime in ms |
| `SQLITE_PATH` | SQLite file path |
| `DATABASE_URL` | JDBC URL for PostgreSQL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | DB credentials |
| `ABOUT_NAME` / `ABOUT_EMAIL` | `/about` response |

On Windows PowerShell:

```powershell
$env:JWT_SECRET="your-long-secret-at-least-32-characters-long"
mvn spring-boot:run
```

## API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/register` | No | Create account |
| POST | `/login` | No | Returns `access_token` |
| GET | `/notes` | Bearer | List accessible notes |
| GET | `/notes/{id}` | Bearer | Get note (owner or shared) |
| POST | `/notes` | Bearer | Create note |
| PUT | `/notes/{id}` | Bearer | Update note (owner only) |
| DELETE | `/notes/{id}` | Bearer | Soft-delete note (owner only) |
| POST | `/notes/{id}/share` | Bearer | Share with another user |
| PATCH | `/notes/{id}/archive` | Bearer | Archive / unarchive (owner) |
| GET | `/openapi.json` | No | OpenAPI 3.0 document |
| GET | `/about` | No | Author & features |

### Extra query parameters on `GET /notes`

- `search` — filter title/content
- `page`, `size` — pagination (returns paged JSON)
- `archived` — `true` / `false`

Without query parameters, returns a plain JSON array of notes.

## Example usage

```bash
# Register
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}'

# Login
TOKEN=$(curl -s -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}' \
  | jq -r .access_token)

# Create note
curl -X POST http://localhost:8080/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Groceries","content":"Milk, eggs"}'

# List notes
curl http://localhost:8080/notes -H "Authorization: Bearer $TOKEN"
```

## PostgreSQL profile

```bash
export SPRING_PROFILES_ACTIVE=postgres
export DATABASE_URL=jdbc:postgresql://localhost:5432/notes_db
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
mvn spring-boot:run
```

Reference SQL is in `src/main/resources/db/migration/V1__init_schema.sql`. Hibernate `ddl-auto=update` creates tables automatically.

## Deployment

### Render (recommended)

Deployment files are included:

- `Dockerfile` — production container build
- `render.yaml` — Blueprint (web service + PostgreSQL)
- `DEPLOY_RENDER.md` — step-by-step guide
- `.env.render.example` — env vars for Render dashboard

Quick start: push to GitHub → Render **New Blueprint** → select repo → set `ABOUT_NAME` / `ABOUT_EMAIL` → Deploy.

See **[DEPLOY_RENDER.md](DEPLOY_RENDER.md)** for full instructions.

### Other hosts (Railway / Heroku)

1. Build the JAR: `mvn clean package -DskipTests`
2. Set environment variables from `.env.example`
3. Use `SPRING_PROFILES_ACTIVE=postgres` and a managed PostgreSQL instance
4. Start: `java -jar target/notes-api-1.0.0.jar`

**Heroku:** `Procfile` and `system.properties` are included.

## Security

- Passwords hashed with BCrypt (strength 12)
- Stateless JWT authentication
- Notes are private by default; only owners and explicitly shared users can read
- Only owners can update, delete, share, or archive
- Input validation on all request bodies
- Generic login error message to avoid user enumeration

## Project structure

```
src/main/java/com/notesapp/
  config/          # App, OpenAPI, SQLite init
  controller/      # REST endpoints
  dto/             # Request/response models
  entity/          # JPA entities
  exception/       # Error handling
  repository/      # Data access
  security/        # JWT filter & config
  service/         # Business logic
```

## Tests

```bash
mvn test
```

## License

MIT
