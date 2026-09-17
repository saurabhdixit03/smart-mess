# Smart Mess

Smart Mess is a full-stack meal planning and mess operations platform designed for local mess and tiffin services.

It provides separate Owner and Customer portals for publishing menus, collecting meal responses, recording meal collections, generating monthly bills, tracking payments, and delivering real-time operational updates.

## Features

### Owner Portal

- Dashboard with live meal-response summaries
- Kitchen preparation estimates
- Lunch and dinner menu publishing
- Customer registration and account management
- Customer meal-response monitoring
- Meal collection recording
- Monthly bill generation
- Cash payment collection
- UPI payment verification
- Billing and revenue insights
- Meal pricing configuration
- Response-window configuration
- Weekly schedule management
- Temporary mess closure management
- Real-time operational updates

### Customer Portal

- View published lunch and dinner menus
- Accept or decline meals
- Select full or half meals
- Request extra rotis
- Review collected meal history
- View monthly bills and bill details
- Pay through a UPI QR code or UPI application
- Submit UPI payment verification requests
- View mess schedules, pricing, and closure information
- Receive persistent real-time notifications
- View profile and account information
- Reset forgotten passwords through email

## System Workflow

```text
Menu Publishing
       |
       v
Customer Meal Response
       |
       v
Live Owner Dashboard
       |
       v
Meal Collection
       |
       v
Meal Records
       |
       v
Monthly Billing
       |
       v
Payment Tracking
```

## Architecture

```text
React and TypeScript Client
          |
          |-- REST API
          |
          |-- STOMP over WebSocket
          |
          v
Spring Boot Backend
          |
          v
MySQL Database
```

The frontend uses a feature-based structure and generally follows this application flow:

```text
Page -> Hook -> API Client -> Spring Boot API
```

The application uses JWT authentication with separate login flows and protected routes for owners and customers.

## Technology Stack

### Frontend

- React 19
- TypeScript
- Vite
- Tailwind CSS
- React Router
- Axios
- React Hook Form
- Zod
- STOMP and SockJS
- Framer Motion
- Sonner
- Lucide React
- React QR Code
- pnpm

### Backend

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT authentication
- WebSocket messaging
- Bean Validation
- MapStruct
- Maven

### Database and Supporting Services

- MySQL
- Brevo SMTP
- GitHub Actions

## Repository Structure

```text
smart-mess/
|-- .github/
|   `-- workflows/
|       `-- ci.yml
|
|-- backend/
|   |-- src/
|   |-- pom.xml
|   |-- mvnw
|   `-- mvnw.cmd
|
|-- client/
|   |-- public/
|   |-- src/
|   |-- package.json
|   `-- pnpm-lock.yaml
|
|-- postman/
|   |-- collections/
|   |   `-- Smart Mess API.postman_collection.json
|   `-- environments/
|       `-- Smart-Mess-Local.postman_environment.json
|
|-- .gitignore
`-- README.md
```

## Prerequisites

Install the following tools before running the project locally:

- Java 17 or later
- Node.js 22 or later
- pnpm
- MySQL
- Git
- Eclipse, IntelliJ IDEA, or another Java IDE (optional)

The Maven Wrapper is included, so Maven does not need to be installed globally.

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/saurabhdixit03/smart-mess.git
cd smart-mess
```

### 2. Create the local database

Create a MySQL database named `smart_mess`:

```sql
CREATE DATABASE smart_mess;
```

### 3. Configure the backend

Update the local database settings in:

```text
backend/src/main/resources/application.properties
```

Configure your local MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_mess
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

The local application configuration enables demo-data seeding:

```properties
app.seed-demo-data=true
```

Set it to `false` if you want to start with an empty database.

For email-based password reset, configure the following environment variables:

```text
BREVO_SMTP_KEY
MAIL_FROM
FRONTEND_URL
```

The application uses `Asia/Kolkata` as its default time zone.

## Running the Backend

You can run the backend through Eclipse or through the Maven Wrapper.

### Option 1: Run through Eclipse

1. Import the `backend` directory as an existing Maven project.
2. Allow Eclipse to download and update the Maven dependencies.
3. Locate the main class containing `@SpringBootApplication`.
4. Select **Run As -> Spring Boot App**.

If the Spring Boot option is unavailable, use:

```text
Run As -> Java Application
```

### Option 2: Run through PowerShell

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Option 3: Run on Linux or macOS

```bash
cd backend
./mvnw spring-boot:run
```

The backend runs by default at:

```text
http://localhost:8080
```

## Running the Frontend

### 1. Configure the frontend environment

Create `client/.env` using `client/.env.example` as the template:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=http://localhost:8080/ws
```

Update the values if your backend uses different URLs.

The local `.env` file is ignored by Git and must not contain production secrets.

### 2. Install dependencies

```powershell
cd client
pnpm install
```

### 3. Start the development server

```powershell
pnpm dev
```

The frontend runs by default at:

```text
http://localhost:5173
```

## Build and Verification

### Frontend production build

The frontend build runs TypeScript compilation followed by the Vite production build:

```powershell
cd client
pnpm build
```

### Backend compilation check

Use this command for a quick clean compilation without creating the final application package:

```powershell
cd backend
.\mvnw.cmd clean compile
```

### Backend package build

This command compiles the backend, runs the configured tests, and creates the application JAR under `backend/target`:

```powershell
cd backend
.\mvnw.cmd clean package
```

To create the package without running tests:

```powershell
.\mvnw.cmd clean package -DskipTests
```

On Linux or macOS, replace `.\mvnw.cmd` with `./mvnw`.

## Production Configuration

The production Spring profile is defined in:

```text
backend/src/main/resources/application-prod.properties
```

It uses environment variables instead of local credentials.

### Required production variables

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
BREVO_SMTP_KEY
FRONTEND_URL
```

### Optional production variables

```text
JWT_EXPIRATION
MAIL_FROM
PASSWORD_RESET_EXPIRATION_MINUTES
APP_TIME_ZONE
```

Activate the production profile with:

```text
SPRING_PROFILES_ACTIVE=prod
```

Demo-data seeding is disabled in the production configuration.

## Postman API Collection

The repository includes a Postman API collection and an empty local environment template:

```text
postman/collections/Smart Mess API.postman_collection.json
postman/environments/Smart-Mess-Local.postman_environment.json
```

To use the collection:

1. Import both JSON files into Postman.
2. Select the `Smart Mess - Local` environment.
3. Configure the local `baseUrl`.
4. Add the required test credentials.
5. Run individual requests or the complete API workflow.

Passwords, JWT tokens, IDs, and other runtime values are intentionally empty in the committed environment file.

Do not commit a populated environment containing real credentials or access tokens.

## Continuous Integration

GitHub Actions runs the Smart Mess CI workflow for:

- Pull requests targeting `main`
- Pushes to `main`

The workflow performs the following checks:

### Frontend

- Installs dependencies using pnpm
- Runs the TypeScript and Vite production build

### Backend

- Sets up Java 17
- Builds the Spring Boot application using Maven
- Skips test execution during the current CI package build

The workflow is configured in:

```text
.github/workflows/ci.yml
```

## Security Notes

- Never commit database passwords, SMTP keys, JWT secrets, or production credentials.
- Keep `client/.env` local.
- Use a strong, unique JWT secret in production.
- Configure production secrets through environment variables.
- Keep the committed Postman environment empty.
- Do not commit JWT tokens generated during API testing.