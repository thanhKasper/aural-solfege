# auth-server

Keycloak identity provider for Aural Solfege. Runs in dev mode (`start-dev`) with its own PostgreSQL.

```bash
docker compose up -d     # Keycloak on http://localhost:8081
```

- Admin console: http://localhost:8081 (default `admin` / `admin`, override with `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD`).
- Realm `aural-solfege` is imported from `realm/aural-solfege-realm.json` on first start. Re-import only happens when the realm does not exist yet, so after editing the file, run `docker compose down -v` first.
- Issuer URI for resource servers: `http://localhost:8081/realms/aural-solfege`.

Dev settings only. Do not use `start-dev` or the default admin password in production.
