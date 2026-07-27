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
| Compat matrix | `signs-compat.yml` on each GitHub Release (source: `compat/signs-compat.yml`) |
| Panel catalog ids | `npcs`, `signs` |
| Auth | Panel env `AERO_GITHUB_TOKEN` (org PAT with Releases read) |

One GitHub Release holds NPCs plus all Signs band JARs **and** `signs-compat.yml`.
The panel caches the matrix under `data/aero-plugins/signs/<productVer>/` and picks
Signs assets by **banded filename prefix**.

## Panel side

- Catalog: `backend/src/services/plugins/catalog.ts` (`compat` metadata)
- Compat resolve: `backend/src/services/plugins/compat/matrix.ts` + `parse.ts`
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
