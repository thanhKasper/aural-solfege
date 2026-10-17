# CLAUDE.md

Monorepo with two independent sub-projects. Read the CLAUDE.md inside the one you are working on.

- `aural-solfege-service/`: Spring Boot main service (JDK 25). Build and architecture notes in its own `CLAUDE.md`.
- `auth-server/`: Keycloak (Docker, realm import in `realm/`). No build step.
- Root `pom.xml` (`aural-solfege-parent`) is the Maven parent + aggregator: shared Spring Boot parent, Java version and JDK enforcer. Run Maven from the root (`mvn test`, `mvn -pl aural-solfege-service spring-boot:run`) or from inside the service folder.
- Root `docker-compose.yml` `include`s both sub-projects' compose files (service DB on :5432, Keycloak on :8081).
