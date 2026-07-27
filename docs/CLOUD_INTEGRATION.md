# Cloud integration

How [aelion-cloud](https://github.com/Aelion-Solutions/aelion-cloud) delivers
JARs from this repo.

## Contract

| Item | Value |
|------|--------|
| GitHub repo | `Aelion-Solutions/aelion-cloud-plugins` |
| Tags | `vX.Y.Z` |
| NPCs asset | `aelion-npcs-X.Y.Z.jar` |
| Signs assets | `aelion-signs-<band>-X.Y.Z.jar` (see [COMPATIBILITY.md](COMPATIBILITY.md)) |
| Panel catalog ids | `npcs`, `signs` |
| Compat matrix | `compat/signs-compat.yml` → Cloud `signs-compat.data.ts` |
| Auth | Panel env `AERO_GITHUB_TOKEN` (org PAT with Releases read) |

One GitHub Release holds NPCs plus all Signs band JARs. The panel picks Signs
assets by **banded filename prefix** from the compat matrix.

## Panel side

- Catalog: `backend/src/services/plugins/catalog.ts`
- Compat: `backend/src/services/plugins/compat/matrix.ts` + `signs-compat.data.ts`
- Fetch/cache: `backend/src/services/plugins/fetch.ts` → `data/aero-plugins/<packageId>/`
- Admin UI: System Configuration → Plugins
- Prefetch: `POST /api/plugins/aelion/:packageId/prefetch`

Aero’s repo/pin settings (`aero.githubRepo`, `aero.pluginVersion`) apply only to
the **aero** package. This repo always uses `defaultGithubRepo` above unless
per-package settings are added later.

## Not in this repo

Aero (control API, `/ae`, proxy registry, **fleet bridge**) stays in **aelion-aero**.
Signs depends on Aero and must not call the panel with its own token.

## Signs runtime

- Config + layouts: shared `config.yml` (animation + forcefield)
- Persistence: `signs.yml`
- Fleet: `AeroFleetService` from the matching Aero band
- Join: Aero `connectPlayer` → BungeeCord `Connect` plugin message
- Build: `compileOnly("com.aelion.aero:aero-api")` via GitHub Packages
