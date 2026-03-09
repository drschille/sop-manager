# Convex Backend

This folder contains schema and function modules for SOP management.

- `schema.ts`: tables and indexes
- `auth.config.ts`: Entra OIDC provider scaffolding
- `lib/auth.ts`: shared `requireUser` + `requireTenant` checks
- `auth.ts`: current user query
- `parts.ts`: part lookup and search
- `procedures.ts`: SOP create/edit/version/history APIs
- `photos.ts`: Convex storage upload/display helpers
- `seed.ts`: sample data seeding
