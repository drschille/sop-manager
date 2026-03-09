# SOP Manager (Compose Multiplatform + Convex + Vite)

Internal factory/warehouse SOP system with:

- `apps/mobile`: Compose Multiplatform app for Android and iOS
- `apps/web`: Vite + React + TypeScript browser UI
- `convex/`: backend schema, auth checks, queries/mutations, storage

## Setup

1. Install dependencies:

```bash
npm install
npm --workspace apps/web install
```

2. Configure environment values:

- Convex project URL for web (`VITE_CONVEX_URL`)
- Entra config for web (`VITE_ENTRA_TENANT_ID`, `VITE_ENTRA_CLIENT_ID`, `VITE_ENTRA_REDIRECT_URI`)
- Entra tenant ID for backend issuer check (`ENTRA_TENANT_ID`)

3. Run Convex backend:

```bash
npm run dev:backend
```

4. Run web app:

```bash
npm run dev:web
```

5. Run mobile app:

```bash
cd apps/mobile
./gradlew :androidApp:installDebug
```

For iOS, open `apps/mobile/iosApp/iosApp.xcodeproj` in Xcode and run.

## Auth configuration

Convex auth provider is scaffolded in [`convex/auth.config.ts`](/Users/danielrobertschille/Developer/convex/sop-manager/convex/auth.config.ts).

Replace placeholders:

- `ENTRA_TENANT_ID`
- `ENTRA_CLIENT_ID`
- issuer URL: `https://login.microsoftonline.com/<ENTRA_TENANT_ID>/v2.0`

Web MSAL setup is in [`apps/web/src/auth/msal.ts`](/Users/danielrobertschille/Developer/convex/sop-manager/apps/web/src/auth/msal.ts).

## What still needs real credentials / tenant config

Before real login works, replace all placeholder values with your tenant values:

- `convex/auth.config.ts` provider `domain` and `applicationID`
- web env values for `VITE_ENTRA_TENANT_ID`, `VITE_ENTRA_CLIENT_ID`, `VITE_ENTRA_REDIRECT_URI`
- backend env value `ENTRA_TENANT_ID` used by `requireTenant`
- any Convex deployment URL and keys needed for your environment

## Architecture summary

- Mobile app: shared Compose UI with SOP-oriented screens (sign-in, scan/search, detail, create/edit, history, settings), plus platform QR scanner abstractions.
- Web app: simple React app with login, SOP search/list, detail, create/edit, and version history flows.
- Convex backend: schema for `parts`, `procedures`, `procedureVersions`, and `auditLog`; auth/tenant validation helpers; SOP versioned create/edit APIs.
- Photos: uploaded to Convex storage using generated upload URLs; storage IDs saved in `procedureVersions.photoStorageIds`; URLs resolved for display.

## Backend functions

- `auth.getCurrentUser`
- `parts.findByQrOrPartNumber`
- `parts.search`
- `procedures.getByPartNumber`
- `procedures.getById`
- `procedures.create`
- `procedures.edit`
- `procedures.listVersions`
- `procedures.getVersion`
- `photos.generateUploadUrl`
- `photos.getPhotoUrl`
- `seed.seedSampleData`

## Seed data

Use `seed.seedSampleData` to add example parts/SOP data:

- part with SOP
- part with multiple SOP versions
- part with no SOP yet

Optionally pass uploaded photo storage IDs to include photos in seeded SOPs.
