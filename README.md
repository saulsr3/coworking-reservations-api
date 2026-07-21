\# Coworking Reservations API



Microservicio de gestión de reservas de espacios de coworking (salas de reuniones, puestos de trabajo, etc.), construido con Spring Boot 3 sobre una arquitectura en capas.



\## Stack

\- Java 17 + Spring Boot 3.3

\- Spring Data JPA + PostgreSQL

\- Spring Security + JWT (jjwt)

\- Resilience4j (Circuit Breaker) + Spring Cloud Circuit Breaker

\- WebClient (cliente HTTP hacia el servicio de pago)

\- springdoc-openapi (Swagger UI)

\- Lombok + MapStruct

\- JUnit 5 + Mockito + H2 (tests unitarios y de integración)

\- Docker + Docker Compose



\## Cómo levantar el proyecto



\### Opción A: todo con Docker (recomendada, no requiere Java/Maven instalado)

```bash

docker compose up --build

```

Levanta Postgres y la aplicación juntos. La API queda en `http://localhost:8080`.



\### Opción B: desde el IDE (desarrollo local)

```bash

docker compose up -d postgres

```

Luego corre `CoworkingReservationsApplication` desde su IDE (perfil `dev` activo por defecto).



Swagger UI: `http://localhost:8080/swagger-ui.html`



\### Primer usuario ADMIN

El registro público siempre crea usuarios `USER` (ver "Decisiones de diseño"). Para tener el primer ADMIN del sistema:

```bash

docker exec -it coworking-postgres psql -U coworking -d coworking -c "UPDATE users SET role = 'ADMIN' WHERE email = 'su-email@example.com';"

```

A partir de ahí, ese ADMIN puede promover a otros usuarios vía `PUT /api/users/{id}/role`.



\## Arquitectura



Capas: `controller / service / repository / entity / dto / mapper / security / exception / config / state / client / mock`. Los controllers nunca exponen entidades JPA directamente, siempre DTOs.



\## Decisiones de diseño y trade-offs



\### Seguridad y autenticación

\- \*\*El registro público nunca acepta el rol desde el cliente\*\*: siempre crea usuarios `USER`. Evita que cualquiera se autoasigne `ADMIN` en el payload de registro. La promoción de rol se hace vía `PUT /api/users/{id}/role`, protegido con `hasRole("ADMIN")` — solo un admin existente puede promover a otros.

\- \*\*`User` implementa `UserDetails` directamente\*\* en vez de una clase adaptadora separada, para reducir boilerplate dado el alcance de la prueba.

\- \*\*401 vs 403 diferenciados explícitamente\*\* (`exceptionHandling` en `SecurityConfig`): 401 cuando no hay token o es inválido, 403 cuando el usuario está autenticado pero no tiene el rol requerido — corrige el comportamiento por defecto de Spring Security, que devuelve 403 para ambos casos.



\### Reservas y concurrencia

\- \*\*Bloqueo pesimista para solapamientos de reservas.\*\* `ReservationRepository.findOverlapping(...)` usa `@Lock(PESSIMISTIC\_WRITE)` sobre las filas candidatas dentro de la transacción de creación, evitando que dos solicitudes concurrentes para el mismo espacio/horario pasen ambas la validación. `Reservation` además tiene `@Version` (optimistic locking) como segunda línea de defensa ante actualizaciones concurrentes sobre una reserva ya existente.

\- \*\*Patrón State para el ciclo de vida de la reserva.\*\* Cada estado (`PendingState`, `PendingPaymentState`, `ConfirmedState`, `CancelledState`, `CompletedState`) implementa solo las transiciones que le son válidas; por defecto (`AbstractReservationState`) cualquier transición no sobrescrita lanza `InvalidReservationStateException`. Se prefirió sobre un `switch`/`if-else` centralizado porque agregar un estado nuevo no requiere tocar la lógica existente.



\### Resiliencia (pago externo)

\- \*\*Mock del gateway de pago vive en el mismo proceso\*\* (`/mock/payment-gateway/validate`), no como servicio separado ni con WireMock standalone, por simplicidad dado el límite de tiempo. Simula latencia aleatoria \~35% de fallos, suficiente para ejercitar el Circuit Breaker de forma realista y reproducible. En un entorno real sería un tercero externo.

\- \*\*Circuit Breaker vía anotación `@CircuitBreaker`\*\* de Resilience4j (no la API programática `CircuitBreakerFactory`), por ser más declarativo. Umbrales en `application.yml`: ventana de 10 llamadas, mínimo 5 para evaluar, 50% de tasa de fallo o de llamadas lentas (>2s) abre el circuito, 10s en estado abierto antes de HALF\_OPEN con 3 llamadas de prueba.

\- \*\*Si el pago no se aprueba (rechazo o circuito abierto), la reserva queda en `PENDING\_PAYMENT`\*\* en vez de fallar la petición completa — comportamiento de fallback pedido explícitamente por el PDF.

\- \*\*`WebClient` (reactivo) usado de forma bloqueante (`.block()`)\*\* dentro de un proyecto MVC tradicional: aprovecha el cliente HTTP más moderno de Spring sin necesidad de migrar todo el proyecto a programación reactiva.



\### Rendimiento

\- \*\*Notificación asíncrona con un `ThreadPoolTaskExecutor` dedicado\*\* (`AsyncConfig`), no el `SimpleAsyncTaskExecutor` por defecto de Spring, que crea un hilo nuevo sin límite en cada llamada.

\- \*\*Reporte de ocupación cacheado con `@Cacheable`\*\*, invalidado (`@CacheEvict`, `allEntries = true`) desde `ReservationService` en cada creación/cancelación de reserva. Se usa el `CacheManager` en memoria por defecto de Spring Boot; en producción con múltiples instancias, debería migrarse a un caché distribuido (Redis) para consistencia entre instancias.



\### Infraestructura

\- \*\*Puerto de Postgres expuesto en `5434` en vez de `5432`\*\* para desarrollo local, evitando choques con instalaciones nativas de Postgres. Solo afecta el mapeo del puerto en el \*host\*; dentro de la red de Docker el contenedor sigue escuchando en `5432`, y el servicio de la app se conecta por nombre de servicio (`postgres:5432`), no por puerto de host.

\- \*\*`Dockerfile` multi-stage\*\* (build con Maven+JDK, runtime solo con JRE): la imagen final no arrastra Maven ni el código fuente, solo el `.jar` compilado.

\- \*\*`ddl-auto: update` en dev/docker, `validate` en producción\*\*: en un proyecto real usaría Flyway/Liquibase para migraciones versionadas; fuera de alcance dado el límite de tiempo.



\### Testing

\- \*\*H2 en memoria para tests\*\* (en vez de Testcontainers) por velocidad y simplicidad dado el límite de tiempo — los tests corren sin necesidad de Docker ni Postgres real, en cualquier máquina. Se configura con `MODE=PostgreSQL` para aproximar el comportamiento real.

\- \*\*`@MockBean` sobre `PaymentValidationService`\*\* en los tests de integración de reservas: evita que el \~35% de fallo aleatorio del mock de pago haga los tests no determinísticos (flaky) — los tests de integración validan lógica de negocio (solapamiento, roles, estados), no el comportamiento aleatorio del Circuit Breaker (eso se prueba manualmente y queda documentado en la demo).



\## Qué se dejó fuera de alcance



\- Migraciones versionadas (Flyway/Liquibase) — se usa `ddl-auto` por simplicidad dado el límite de días.

\- Refresh tokens / revocación de JWT — solo expiración por tiempo.

\- Paginación en listados administrativos (`GET /api/reservations/admin/all`, `GET /api/users`).

\- Caché distribuida (Redis) para el reporte de ocupación en escenarios multi-instancia.

\- WireMock standalone para el mock de pago (se implementó como endpoint interno del mismo proceso).

\- Tests de integración con Testcontainers (se usó H2 por velocidad dado el tiempo disponible).



Con más tiempo, estas serían las siguientes prioridades, en ese orden.



\## Endpoints principales



| Método | Ruta | Rol requerido |

|---|---|---|

| POST | `/api/auth/register` | Público |

| POST | `/api/auth/login` | Público |

| GET/POST | `/api/spaces` | Autenticado / ADMIN (POST) |

| GET/PUT/DELETE | `/api/spaces/{id}` | Autenticado / ADMIN (PUT, DELETE) |

| POST | `/api/reservations` | Autenticado |

| GET | `/api/reservations/me` | Autenticado |

| GET | `/api/reservations/admin/all` | ADMIN |

| DELETE | `/api/reservations/{id}` | Autenticado (dueño) / ADMIN |

| GET | `/api/reports/occupancy` | ADMIN |

| GET | `/api/users` | ADMIN |

| PUT | `/api/users/{id}/role` | ADMIN |



\## Testing



```bash

mvn test

```

13 tests unitarios (patrón State + `ReservationService` con Mockito) + 6 tests de integración (`@SpringBootTest` con H2: autenticación, autorización por roles, flujo completo de reservas con solapamiento).



\## Colección Postman



`Coworking-Reservations-API.postman\_collection.json` + `Coworking-Local.postman\_environment.json` en la raíz del repositorio.

