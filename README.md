# Aural Solfege

Monorepo for the Aural Solfege ear-training platform.

| Folder | What it is |
| --- | --- |
| [`aural-solfege-service`](aural-solfege-service/README.md) | Main Spring Boot service (REST :8080, gRPC :9090, PostgreSQL :5432) |
| [`auth-server`](auth-server/README.md) | Keycloak identity provider (:8081) with realm `aural-solfege` |

## Quick start

```bash
docker compose up -d                       # service DB + Keycloak (+ its DB)
mvn -pl aural-solfege-service spring-boot:run
```
