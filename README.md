# ApiGateway

Single entry point of the platform (port **8080**), built with Spring Cloud Gateway (WebFlux). It has **no database**:
it only routes requests to the microservices, and adds a correlation id (`X-Request-Id`) to every request.
The JWT is forwarded untouched: each service validates it itself.

| Path | Service | Port |
|---|---|---|
| /api/auth, /api/accounts, /api/roles, /api/permissions, /api/audit-logs | IdentityService | 8082 |
| /api/tenders, /api/documents, /api/statuses | TenderFlow | 8081 |
| /api/notifications | NotificationService | 8084 |
| /api/files | DocumentService | 8085 |
| /api/events, /api/reports | ReportingService | 8086 |
| /api/jobs | SchedulerService | 8087 |
| /api/search | SearchService | 8088 |

A service that is not running simply answers 503/502 on its routes; the others keep working.

## Run
Eclipse: run `GatewayApplication`, the default URLs are `http://localhost:<port>`; override with `*_SERVICE_URL`.
Docker: `docker network create platform-net` (once), then `docker compose up --build`.
Test: `POST http://localhost:8080/api/auth/login` (same body as on IdentityService), then call any route with the Bearer token.

CORS: allowed browser origins come from `CORS_ALLOWED_ORIGINS` (comma separated), by default the usual React / Vite dev ports, the Angular fronts 4200 to 4205 and TenderFlow_Front 58020.
