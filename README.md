# Vault Service

A microservice for securely handling sensitive bank card data — generating and verifying **PIN**, **OTP (PIN2)**, and **CVV2** — that never stores sensitive values in plaintext and delegates all encryption/hashing to **HashiCorp Vault** (Transit Secrets Engine).

The project is a **multi-module Maven** build following **Hexagonal / Clean Architecture**, keeping the domain (business logic) fully isolated from framework, database, and infrastructure concerns.

---

## ⚠️ Important security note

> Some features, tokens, encryption keys, internal server addresses (Vault, Nexus, Eureka, etc.), and other sensitive configuration have been **intentionally removed or blanked out** in this repository to prevent leaking confidential information in a public/shared space.
>
> Values such as `app.vault.token`, `app.vault.roleId`, `app.vault.secretId`, encryption/HMAC keys (`pinKey`, `cvv2Key`, `otpHmacKey`, `masterEncryptionKey`, etc.), and internal Nexus/registry credentials **must be supplied via environment variables or a secret manager** before running or deploying this service — never commit real values into the configuration files.
>
> Some business logic, endpoints, or settings tied to internal organizational infrastructure may also have been removed or simplified in this shared/public version of the code.

---

## Architecture and modules

The project follows the Hexagonal (Ports & Adapters) pattern:

| Module | Responsibility |
|---|---|
| `vault-domain/vault-domain-core` | Base domain model, shared entities/value objects, and cross-cutting contracts (e.g. `VaultDomainException`) |
| `vault-domain/vault-application-service` | Business logic (use cases), domain services (`PinServiceImpl`, `OtpServiceImpl`, `CvvServiceImpl` — all implementing the generic `SecretService<GReq,GRes,VReq,VRes>` — plus `SessionService`, `StatusService`), commands/responses, and input/output ports (repository interfaces) |
| `vault-application` | Presentation layer (REST API) — controllers, global exception handler |
| `vault-infrastructure` | Output adapter implementations — database access, concrete repositories, mappers |
| `vault-container` | Application entry point (`VaultServiceApplication`), Spring Boot configuration (Vault, Security, OpenAPI, Flyway, logging, etc.), `application*.yml` files |

> The `vault-messaging` module (for Kafka-based messaging) is currently disabled in the root `pom.xml` because it hasn't been implemented yet.

---

## Tech stack

- **Language & framework:** Java 21, Spring Boot 3.5.3, Spring Cloud
- **Database:** H2 (file-based, for local dev/test, `MODE=Oracle`) + Flyway (Oracle schema migrations on the development/production/staging profiles)
- **Secrets & encryption:** HashiCorp Vault via `spring-vault-core` (Token or AppRole authentication, with a `LifecycleAwareSessionManager` that renews the session / re-authenticates automatically)
- **Resilience:** Resilience4j — circuit breaker + retry around every outbound Vault call (`ResilientVaultOperations`)
- **API authentication:** Spring Security as an OAuth2 Resource Server (JWT bearer token validation)
- **Cache/session:** Redis + Redisson
- **Service discovery:** Netflix Eureka Client (Spring Cloud)
- **API docs:** springdoc-openapi (Swagger UI) with a Bearer/JWT security scheme
- **Logging:** Log4j2 + an AOP-based logging aspect for request/response logging
- **Utilities:** Lombok, MapStruct, Vavr, Commons-Text
- **Monitoring:** Spring Boot Actuator (with a custom `VaultInfoContributor`; only `health`/`info` are public)
- **Transport security:** HTTPS/TLS via a JKS keystore

---

## Features & API

All endpoints live under `/api/v1/vault` and require an `Authorization: Bearer <token>` header:

| Method | Path | Description |
|---|---|---|
| `POST` | `/pin/generate` | Generate PIN1 for a card |
| `POST` | `/pin/verify` | Verify PIN1 against the stored hash |
| `POST` | `/otp/generate` | Generate an OTP (PIN2) for a card, TOTP-based with configurable step/digits/drift |
| `POST` | `/otp/verify` | Verify a submitted OTP |
| `POST` | `/cvv/generate` | Generate CVV2 for a card |
| `POST` | `/cvv/verify` | Verify CVV2 |

Other domain capabilities (service-level, not yet exposed as public endpoints): session management (`SessionService` — create, validate, and expire sessions) and service status/health lookups (`StatusService`).

Errors are handled centrally by `VaultGlobalExceptionHandler` and returned in the shared `BaseResponse` envelope.

---

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker and Docker Compose (to run Redis and a HashiCorp Vault dev instance)
- Access to the organization's internal Maven repository (Nexus) for private dependencies (e.g. `common-application` and internal banking libraries) — these dependencies and their addresses have been removed/restricted in this public version for security reasons.

> The Java and Maven/build-tool versions pinned in `pom.xml` (Java 21 / Spring Boot 3.5.3) are intentionally left as-is and should not be changed without coordination.

---

## Running locally

1. **Start the dependent services (Redis and Vault dev-mode):**

   ```bash
   docker compose up -d
   ```

   This starts a Redis instance on port `6379` and a HashiCorp Vault dev instance on port `8200` (Vault runs in `-dev` mode with a root token, for local testing only — **not suitable for production**).

2. **Configure the Vault secrets:**

   The following properties (under `app.vault.*`) must be set for your environment (via environment variables or a local, non-committed config file), since they're blank/removed in this repository's `application*.yml` files:

   `app.vault.uri`, `app.vault.token` or (`app.vault.roleId` + `app.vault.secretId`), `app.vault.transitMount`, `app.vault.pinKey`, `app.vault.pinEncryptKey`, `app.vault.cvv2Key`, `app.vault.cvv2EncryptKey`, `app.vault.otpHmacKey`, `app.vault.otpEncryptKey`, `app.vault.otpSharedSecretPath`, `app.vault.totpStepSeconds`, `app.vault.totpDigits`, `app.vault.totpAllowedDriftSteps`

3. **Configure JWT authentication (required):**

   `SecurityConfig` is a real OAuth2 Resource Server, and the application will not start without this configured:

   ```bash
   export SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://<your-identity-provider>/
   # or, instead of issuer-uri:
   export SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://<your-identity-provider>/.well-known/jwks.json
   ```

   This project does not run its own identity provider; point it at a real IdP (Keycloak, Auth0, Azure AD, etc.) or a test JWKS endpoint for local development.

4. **Build:**

   ```bash
   mvn clean install -Pdevelopment
   ```

5. **Run:**

   ```bash
   mvn spring-boot:run -pl vault-container -Pdevelopment
   ```

   or run the built jar in `vault-container/target` directly.

6. The service runs over HTTPS by default (`server.ssl.enabled=true`), and Swagger UI is available at `/swagger-ui.html`.

---

## Profiles

Three profiles are defined at both the Maven and Spring level:

| Profile | Activation | Config file |
|---|---|---|
| `development` | default (`activeByDefault`) | `application-development.yml` |
| `staging` | `-Pstaging` | `application-staging.yml` |
| `production` | `-Pproduction` | `application-production.yml` |

---

## Security & production readiness

- **API authentication:** `SecurityConfig` is an OAuth2 Resource Server requiring a JWT bearer token on every endpoint except Swagger/OpenAPI, `/actuator/health`, `/actuator/info`, and (outside production) `/h2-console`. Authentication/authorization failures are returned in the same `BaseResponse`/`ErrorDetail` shape as the rest of the API (see `VaultSecurityResponses`). No identity provider is bundled with this project — `issuer-uri`/`jwk-set-uri` must point at a real one, or the app will refuse to start (see "Running locally" above).
- **Actuator exposure** is limited to `health` and `info` across all profiles; `health` only shows full details to authenticated callers (`show-details: when-authorized`).
- **Vault session handling:** `VaultConfig` uses a `LifecycleAwareSessionManager`, which renews the Vault session/token (or re-authenticates via AppRole) automatically instead of relying on a single static token for the process lifetime.
- **Vault call resilience:** outbound calls to Vault go through a circuit breaker + retry (Resilience4j, instance name `vault`, tunable in `application.yml`); see `ResilientVaultOperations`.
- **H2 console** is disabled on the `production` profile and only available on `development`/`staging`.
- **Redis in production:** `application-production.yml` documents a `clusterServersConfig`/`sentinelServersConfig` template (commented out) alongside the default `singleServerConfig`, which is only appropriate for local/dev use.
- The local `docker-compose.yml` Vault container runs in `-dev` mode (no HA, no auto-unseal) and is for local development only; a production deployment needs a real Vault cluster.
- The sample keystore password (`changeit`) is a local-dev placeholder and must be replaced with a real keystore and password in any real environment.

### Known limitations

- The `AuthenticationException` handler in `VaultGlobalExceptionHandler` catches `javax.naming.AuthenticationException`, not `org.springframework.security.core.AuthenticationException` — so Spring Security authentication failures don't actually reach that handler (which is why `VaultSecurityResponses` is wired independently for that case).
- All `target/` build output directories are currently committed to this repository, and there is no `.gitignore`. This unnecessarily bloats the repository and should be cleaned up.

---

## Tests

Run the unit tests:

```bash
mvn test
```
