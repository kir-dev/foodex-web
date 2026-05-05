# FoodEx-Web

This is a starter project for a fullstack application using React and Spring Boot.

## Getting Started - Backend

### Running/debugging locally

- Open the repository in IntelliJ IDEA (Ultimate) in the **`apps/backend`** directory!!!
- **Go to** [auth.sch.bme.hu](https://auth.sch.bme.hu/console/) and **create a new** OAuth client. Set the *Átirányítási cím* to `http://localhost:8080/login/oauth2/code/authsch`
- In the `src/main/resources/config/` directory, **create a new file** `application-local.properties` using the config below and update it with your AuthSCH credentials:

```properties
### SPECIFY AUTHSCH
spring.security.oauth2.client.registration.authsch.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.authsch.client-secret=YOUR_SECRET_CLIENT_KEY

spring.jpa.show-sql=true
```

- Create new ***Run / Debug Configuration*** or update the existing one
  - Click on the button (left of the green run triangle and debug icons), then **click *Edit configurations...***
  - Click on the **+** icon (*Add New Configuration*), then choose *Spring Boot*. If you don't see this option, then activate IntelliJ Ultimate with your *@edu.bme.hu* email - [JetBrains student pack](https://www.jetbrains.com/academy/student-pack/)
  - Let's name it `FoodEx-Web`
  - **Select the main class** *hu.kirdev.foodex.FoodExApplication*
  - Ensure that the **`local`** profile is set in the *Active profiles*. ***TODO:*** To insert demo data, also enalbe the `test` profile.
- **Press** *Debug 'FoodEx-Web'*🪲, then **open** http://localhost:8080

## Getting Started - Frontend

### Prerequisites

- Pnpm 10

### Installation

You only need to install dependencies in the root directory.

```bash
pnpm install
```

### Linter and Formatter Configuration

It is a must to use ESLint and Prettier in this project.

Set up ESLint and Prettier in your IDE and check `fix on save` or `format on save` options.

You can run the following commands to check linting and formatting issues.

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

### Development

You can run the frontend by the following command:

```bash
pnpm start:frontend # Starts on http://localhost:3000
```

### After Development

You can build the frontend and run the application.

```bash
pnpm build:frontend
```

There are recommended GitHub Actions workflows for this setup, which will fail if one of the following commands fails:

```bash
pnpm lint
```

```bash
pnpm format:check
```
