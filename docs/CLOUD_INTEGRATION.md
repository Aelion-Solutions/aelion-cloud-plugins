# Cloud integration

How [aelion-cloud](https://github.com/Aelion-Solutions/aelion-cloud) delivers
JARs from this repo.

## Contract

| Item | Value |
|------|--------|
| GitHub repo | `Aelion-Solutions/aelion-cloud-plugins` |
| Tags | `vX.Y.Z` |
| NPCs asset | `aelion-npcs-X.Y.Z.jar` |
| Signs asset | `aelion-signs-X.Y.Z.jar` |
| Panel catalog ids | `npcs`, `signs` |
| Auth | Panel env `AERO_GITHUB_TOKEN` (org PAT with Releases read) |

One GitHub Release can hold both JARs. The panel picks assets by **filename
prefix** per catalog package, so NPCs and Signs share this monorepo safely.

## Panel side

- Catalog: `backend/src/services/plugins/catalog.ts`
- Fetch/cache: `backend/src/services/plugins/fetch.ts` → `data/aero-plugins/<packageId>/`
- Admin UI: System Configuration → Plugins
- Prefetch: `POST /api/plugins/aelion/:packageId/prefetch`

Aero’s repo/pin settings (`aero.githubRepo`, `aero.pluginVersion`) apply only to
the **aero** package. This repo always uses `defaultGithubRepo` above unless
per-package settings are added later.

## Not in this repo

Aero (control API, `/ae`, proxy registry, **fleet bridge**) stays in **aelion-aero**.
Signs SoftDepends Aero and must not call the panel with its own token.

## Signs runtime

- Config + layouts: `signs-paper` `config.yml`
- Persistence: `signs.yml`
- Fleet: `AeroFleetService` from Aero Paper
- Join: Aero `connectPlayer` → BungeeCord `Connect` plugin message
