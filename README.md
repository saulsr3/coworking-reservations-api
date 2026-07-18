

# Coworking Reservations API

Microservicio de gestión de reservas de espacios de coworking (salas de reuniones, puestos de trabajo, etc.), construido con Spring Boot 3 sobre una arquitectura en capas.

## Stack
- Java 17 + Spring Boot 3.3
- Spring Data JPA + PostgreSQL
- Spring Security + JWT (jjwt)
- Resilience4j (Circuit Breaker)
- springdoc-openapi (Swagger UI)
- Lombok + MapStruct
- Testcontainers + Mockito + WireMock

## Cómo levantar el proyecto

```bash
docker compose up -d postgres
mvn spring-boot:run
```

La API queda en `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Arquitectura

Capas: `controller / service / repository / entity / dto / mapper / security / exception / config`. Los controllers nunca exponen entidades JPA directamente, siempre DTOs.

## Decisiones de diseño y trade-offs

- **Bloqueo pesimista para solapamientos de reservas.** `ReservationRepository.findOverlapping(...)` usa `@Lock(PESSIMISTIC_WRITE)` sobre las filas candidatas dentro de la transacción de creación, en vez de solo un `SELECT` + `INSERT`. Esto evita que dos solicitudes concurrentes para el mismo espacio/horario pasen ambas la validación antes de que la primera haga commit. Además `Reservation` tiene `@Version` (optimistic locking) como segunda línea de defensa. Se priorizó el bloqueo pesimista sobre un reintento optimista porque el negocio no puede tolerar dos reservas confirmadas simultáneas para el mismo horario.
- **`User` implementa `UserDetails` directamente** en vez de una clase adaptadora separada, para reducir boilerplate dado el alcance de la prueba. En un sistema más grande separaría el modelo de dominio del modelo de autenticación.
- **El registro público nunca acepta el rol desde el cliente**: siempre crea usuarios `USER`. Evita que cualquiera se autoasigne `ADMIN` en el payload de registro; la promoción de rol se hace por un endpoint protegido aparte.
- **Configuración por perfiles** (`application-dev.yml` / `application-prod.yml`) con `@ConfigurationProperties` en vez de `@Value` disperso, para centralizar y validar la config de seguridad en un solo lugar.
- **`ddl-auto: validate` en producción** (vs `update` en dev): en un proyecto real usaría Flyway/Liquibase para migraciones versionadas; fuera de alcance dado el límite de tiempo, se documenta como pendiente.

## Qué se dejó fuera de alcance

- Migraciones versionadas (Flyway/Liquibase) — se usa `ddl-auto` por simplicidad dado el límite de 4 días.
- Refresh tokens / revocación de JWT — solo expiración por tiempo.
- Paginación en algunos listados administrativos.

Con más tiempo, estas serían las siguientes prioridades.