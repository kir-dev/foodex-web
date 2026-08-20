# FoodEx-Web

```console
    ______                ________         _       __     __
   / ____/___  ____  ____/ / ____/  __    | |     / /__  / /_
  / /_  / __ \/ __ \/ __  / __/ | |/_/____| | /| / / _ \/ __ \
 / __/ / /_/ / /_/ / /_/ / /____>  </_____/ |/ |/ /  __/ /_/ /
/_/    \____/\____/\__,_/_____/_/|_|      |__/|__/\___/_.___/

```

Full-stack FoodEx app: **Spring Boot** backend (`apps/backend`) + **Next.js** frontend (`apps/frontend`).

| Service   | Default URL              |
|-----------|--------------------------|
| Backend   | http://localhost:8080    |
| Frontend  | http://localhost:3000    |
| Swagger   | http://localhost:8080/swagger-ui/index.html |

---

## Prerequisites

- **JDK 25**
- **IntelliJ IDEA** (Ultimate recommended for Spring Boot run configs)
- **pnpm 10** (frontend)
- AuthSCH OAuth client ([auth.sch.bme.hu console](https://auth.sch.bme.hu/console/))

---

## Backend

Work from **`apps/backend`** (Gradle project root).

### 1. AuthSCH + local config

1. Create an OAuth client at [auth.sch.bme.hu](https://auth.sch.bme.hu/console/).
2. Set *Átirányítási cím* (redirect URI) to:
   `http://localhost:8080/login/oauth2/code/authsch`
3. Create **`apps/backend/src/main/resources/config/application-local.properties`**
using the config below and update it with your AuthSCH credentials:

```properties
### SPECIFY AUTHSCH
spring.security.oauth2.client.registration.authsch.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.authsch.client-secret=YOUR_SECRET_CLIENT_KEY

spring.jpa.show-sql=true
```

Optional: enable the H2 console only for local debugging:

```properties
spring.h2.console.enabled=true
```

### 2. Developer admin elevators (optional)

To grant **ADMIN** on login without being a FoodEx executive in AuthSCH, edit:

`apps/backend/src/main/resources/config/developer-admins.properties`

```properties
# key = AuthSCH internalId (OIDC subject)
# value = note (documentation only)
your-uuid-here=Your Name (backend)
```

Re-login after changing this file.

### 3. Run the backend

#### Option A — CLI (Gradle)

```bash
cd apps/backend

# local profile (loads application-local.properties)
./gradlew bootRun --args='--spring.profiles.active=local'

# local + demo seed data (TestConfig)
./gradlew bootRun --args='--spring.profiles.active=local,test'
```

API: http://localhost:8080
After AuthSCH login you are redirected to the SPA: http://localhost:3000

#### Option B — IntelliJ

1. Open the project from **`apps/backend`** (or open the monorepo and import the Gradle module).
2. Link Gradle: open `build.gradle.kts` and refresh (Gradle elephant icon).
3. **Run / Debug Configuration** → Spring Boot:
   - Main class: `hu.kirdev.foodex.FoodExApplication`
   - JDK: **25**
   - Active profiles: `local`
     For demo data: `local,test`
4. Debug / Run.

### 4. Sign in

Open:

http://localhost:8080/oauth2/authorization/authsch

On success the backend redirects to **`foodex.frontend-url`** (default `http://localhost:3000`).

Public smoke endpoint (no login): http://localhost:8080/api/homepage

Session user (login required): **`GET /api/users/me`** — returns the current user’s `DetailedUserDto` (`id`, `role`, profile, …). **401** if not logged in.

### 5. Run backend tests

From `apps/backend`:

```bash
# all tests
./gradlew test

# single class
./gradlew test --tests 'hu.kirdev.foodex.shift.ShiftServiceCapacityTest'

# single method
./gradlew test --tests 'hu.kirdev.foodex.security.SecuritySmokeTest.homepage is public'

# HTML report
# apps/backend/build/reports/tests/test/index.html
```

Tests use an in-memory H2 DB and do **not** need AuthSCH credentials or `application-local.properties`.

In IntelliJ: right-click a test class/method or `src/test/kotlin` → **Run**.

### 6. API notes (session + CSRF)

Most `/api/**` routes need a **logged-in Spring session**. Mutating methods also need **CSRF**.

| Status | Typical meaning |
|--------|-----------------|
| **401 Unauthorized** | Missing/invalid `JSESSIONID` (not logged in to the backend) |
| **403 Forbidden** | Often missing/wrong CSRF header; or authenticated but not allowed (e.g. not club leader / admin) |
| **201 / 200** | Success |

#### Which cookies matter?

After AuthSCH login, DevTools will show cookies on **several** domains. Only **`http://localhost:8080`** cookies are used by this API:

| Cookie | Domain | Use it? |
|--------|--------|---------|
| `JSESSIONID` | **localhost:8080** | **Yes** — backend session |
| `XSRF-TOKEN` | **localhost:8080** | **Yes** — CSRF (send again as header) |
| `PHPSESSID` | auth.sch.bme.hu | **No** — AuthSCH only; useless for `/api/**` |

How to copy them:

1. Sign in via http://localhost:8080/oauth2/authorization/authsch (must complete against the **backend**).
2. Browser DevTools → **Application** → **Cookies** → select **`http://localhost:8080`** (not `auth.sch.bme.hu`).
3. Copy `JSESSIONID` and `XSRF-TOKEN`.
   If `XSRF-TOKEN` is missing, open http://localhost:8080/api/homepage or Swagger once while logged in.

Restarting the backend invalidates in-memory sessions — log in again and copy new cookies.

#### Call mutating endpoints with curl

```bash
# Values from DevTools → Cookies → http://localhost:8080
SESSION='paste-JSESSIONID-here'
TOKEN='paste-XSRF-TOKEN-here'

curl -X POST 'http://localhost:8080/api/requests' \
  -H 'Content-Type: application/json' \
  -H "X-XSRF-TOKEN: $TOKEN" \
  -H "Cookie: JSESSIONID=$SESSION; XSRF-TOKEN=$TOKEN" \
  -d '{
    "cookingClubId": 403,
    "opening": "2026-08-03T13:00:00",
    "closing": "2026-08-03T18:00:00",
    "place": "nagykonyha",
    "description": "example"
  }'
```

Requirements for `POST` / `PUT` / `PATCH` / `DELETE`:

1. **`Cookie: JSESSIONID=...`** — real session from `localhost:8080` (not a placeholder, not AuthSCH’s `PHPSESSID`).
2. **`Cookie: XSRF-TOKEN=...`** and **`Header: X-XSRF-TOKEN: ...`** — same token value from the `XSRF-TOKEN` cookie.

#### Swagger UI

Swagger: http://localhost:8080/swagger-ui/index.html

- Use it in the **same browser** after backend login so `JSESSIONID` is sent automatically.
- Swagger does **not** add `X-XSRF-TOKEN` for you. Prefer curl (above) for POSTs, or inject the header via browser tooling.
- “Authorize” in Swagger does not replace session + CSRF for this cookie-based setup.

#### SPA (frontend on :3000)

- CORS allows `http://localhost:3000` with credentials.
- Frontend must send cookies (`credentials: 'include'`) and on mutating requests set header
  `X-XSRF-TOKEN` from the `XSRF-TOKEN` cookie.
- After login, call **`GET /api/users/me`** with `credentials: 'include'` to load the
  current user (`id`, `role`, profile fields). Use that instead of guessing the user id.
  Safe GETs only need the session cookie; CSRF is for mutating methods.

---


## Frontend

### Installation

From the **repository root**:

```bash
pnpm install
```

### Development

```bash
pnpm start:frontend
# → http://localhost:3000
```

Run the backend at the same time so the SPA can call the API.

### Lint & format

```bash
pnpm lint
# or
pnpm lint:fix
```

```bash
pnpm format:check
# or
pnpm format
```

### Production build

```bash
pnpm build:frontend
```

CI is expected to fail if `pnpm lint` or `pnpm format:check` fails.

---

## Typical local workflow

```bash
# terminal 1 — backend
cd apps/backend
./gradlew bootRun --args='--spring.profiles.active=local,test'

# terminal 2 — frontend (repo root)
pnpm install   # once
pnpm start:frontend
```

1. Visit http://localhost:3000
2. Sign in via http://localhost:8080/oauth2/authorization/authsch
3. Use the app; API session + CSRF apply as above

## Deploy

### Build with Docker

#### Backend latest

```bash
cd apps/backend
./gradlew bootBuildImage --imageName=harbor.sch.bme.hu/org-kir-dev/foodex-backend:latest
```

#### Frontend latest

```bash
cd apps/frontend
docker build . -t harbor.sch.bme.hu/org-kir-dev/foodex-frontend:latest
```

### Login with Docker

```bash
docker login harbor.sch.bme.hu
```

### Push with Docker

#### Push backend

```bash
docker push harbor.sch.bme.hu/org-kir-dev/foodex-backend:latest
```

#### Push frontend

```bash
docker push harbor.sch.bme.hu/org-kir-dev/foodex-frontend:latest
```

### Run with Docker

#### Run backend with port forwarding

```bash
docker run -p 8080:8080 harbor.sch.bme.hu/org-kir-dev/foodex-backend:latest
```

#### Run frontend with port forwarding

```bash
docker run -p 3000:3000 harbor.sch.bme.hu/org-kir-dev/foodex-frontend:latest
```

### Kubectl

```bash
kubectl config current-context
kubectl get pods -A
```

### K9S

```bash
k9s
```

---

## Happy coding :D

```console
 ________                   __  _______                    __      __          __
/\  _____\                 /\ \/\  ____\                  /\ \  __/\ \        /\ \
\ \ \_____  ____    ____   \_\ \ \ \____     __  _        \ \ \/\ \ \ \     __\ \ \____
 \ \  ____\/ __`\  / __`\  /'_` \ \  ___\   /\ \/ \ _______\ \ \ \ \ \ \  /'__`\ \ '__`\
  \ \ \   /\ \_\ \/\ \_\ \/\ \_\ \ \ \____  \/>  <//\______\\ \ \_/ \_\ \/\  __/\ \ \_\ \
   \ \_\  \ \____/\ \____/\ \___,_\ \_____\  /\_/\_\/______/ \ `\___x___/\ \____\\ \_,__/
    \/_/   \/___/  \/___/  \/__,_ /\/_____/ \/_/\/_/          '\/__//__/  \/____/ \/___/
```
