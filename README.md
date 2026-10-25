# Employee Portal

A full-stack employee directory built with **Spring Boot, MySQL and Angular**. It supports the
complete Employee CRUD flow: create an employee, browse and search the directory, fetch one
employee by ID, update a record and delete a record.

Department and Project are reference entities. They are seeded and exposed as read-only lookup
endpoints so the employee form can assign a department and an optional project.

## Stack

| Layer | Technology |
| --- | --- |
| Frontend | Angular 20 (standalone components, signals, reactive forms), HTML, CSS |
| HTTP | Angular `HttpClient` through a development proxy to `/api` |
| Backend | Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA, Bean Validation |
| Database | MySQL 8.4 (Docker), H2 in MySQL compatibility mode for quick local development |
| Build | Maven 3.9, npm / Angular CLI |
| Testing | JUnit 5 + MockMvc integration tests, Playwright end-to-end verification |

## Project structure

```
fullstack/
├── backend/                 Spring Boot REST API
│   ├── src/main/java/com/example/employeeportal/
│   │   ├── config/          CORS and sample-data seeding
│   │   ├── controller/      EmployeeController, CatalogController
│   │   ├── dto/             Request/response records and API error model
│   │   ├── entity/          Employee, Department, Project
│   │   ├── exception/       Not found, duplicate and validation handling
│   │   ├── repository/      Spring Data JPA repositories
│   │   └── service/         EmployeeService, CatalogService
│   └── src/test/            MockMvc API integration tests
├── frontend/                Angular application
│   └── src/app/
│       ├── core/            Models, API service, toast service
│       ├── pages/           Employee list, add/edit form
│       ├── shared/          Toast component
│       └── app.*            Shell, navigation and routes
├── database/schema.sql      MySQL schema
├── docker-compose.yml       MySQL 8.4 container
├── docs/
│   ├── screenshots/         Verified UI screenshots
│   ├── Employee-Portal-Project-Report.tex   LaTeX source for the report
│   └── Employee-Portal-Project-Report.pdf
└── README.md
```

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20.19+ (or 22.12+/24+) and npm
- Docker Desktop (only needed for the MySQL profile)

## Quick start (H2 development profile)

The default profile uses an in-memory H2 database in MySQL compatibility mode, so the project runs
without installing MySQL.

Terminal 1 - backend:

```powershell
cd backend
mvn spring-boot:run
```

The API starts on <http://localhost:8080>. Sample departments, projects and six employees are
inserted on first start. The H2 console is available at <http://localhost:8080/h2-console>
(JDBC URL: `jdbc:h2:mem:employee_portal`, user `sa`, empty password).

Terminal 2 - frontend:

```powershell
cd frontend
npm install
npm start
```

Open <http://localhost:4200>. Angular's development server proxies `/api` requests to port 8080
using `proxy.conf.json`.

## Run with MySQL

1. Start MySQL:

   ```powershell
   docker compose up -d
   ```

   The container creates the `employee_portal` database and schema from
   `database/schema.sql`. Demo credentials: database `employee_portal`, user `portal`,
   password `portal`.

2. Start the backend with the MySQL profile:

   ```powershell
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

   Equivalent JAR command:

   ```powershell
   java -jar target/employee-portal-1.0.0.jar --spring.profiles.active=mysql
   ```

   Override the connection with `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` environment variables if
   required.

3. Start the Angular app as described above.

## REST API

Base URL: `http://localhost:8080/api`

| Method | Endpoint | Purpose | Success |
| --- | --- | --- | --- |
| GET | `/employees?search=term` | List employees; optional search over code, name, email, designation and department | 200 |
| GET | `/employees/{id}` | Fetch one employee | 200 |
| POST | `/employees` | Create an employee | 201 + `Location` |
| PUT | `/employees/{id}` | Update an employee | 200 |
| DELETE | `/employees/{id}` | Delete an employee | 204 |
| GET | `/departments` | List departments (lookup data) | 200 |
| GET | `/projects` | List projects (lookup data) | 200 |

Validation failures return `400` with a `fieldErrors` map. Duplicate employee code/email returns
`409`; unknown IDs return `404`.

Example request:

```json
{
  "employeeCode": "EMP007",
  "firstName": "Ishaan",
  "lastName": "Verma",
  "email": "ishaan.verma@example.com",
  "phone": "+91 90000 12345",
  "designation": "Backend Developer",
  "salary": 78000,
  "dateOfJoining": "2024-08-19",
  "status": "ACTIVE",
  "departmentId": 1,
  "projectId": 1
}
```

## Tests

```powershell
cd backend
mvn test
```

The six integration tests cover list/search, create/read/update/delete, validation, duplicates,
missing records and lookup endpoints.

```powershell
cd frontend
npm run build
npm test
```

## Notes

- The demo credentials in `docker-compose.yml` are for local development only. Use environment
  variables or a secrets manager for any shared deployment.
- The backend exposes no authentication in this lab scope, as stated in the project report.
- The generated report is at `docs/Employee-Portal-Project-Report.pdf`; the LaTeX source is
  `docs/Employee-Portal-Project-Report.tex`.
