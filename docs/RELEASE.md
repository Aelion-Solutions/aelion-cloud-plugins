# Release flow

Same model as [aelion-aero](https://github.com/Aelion-Solutions/aelion-aero):
[release-please](https://github.com/googleapis/release-please) on `main`.
Nothing is tagged until you approve and merge the Release PR.

```text
feature PR --> main --> Release Please PR (review) --> merge --> tag vX.Y.Z + GitHub Release + JARs
```

## Prerequisites

CI/Release run on the org self-hosted runners (`self-hosted`, `linux`, `aelion`).

On `main`, enable branch protection:

- Require a pull request before merging
- Require at least one approving review
- Optionally require status checks (`CI`)

## Day to day

1. Merge work to `main` with [Conventional Commits](https://www.conventionalcommits.org/).
2. The **Release** workflow runs release-please.
3. If there are releasable commits, it opens/updates a PR like `chore(main): release 0.1.0`.
4. Review changelog + version bumps (`gradle.properties`, `PluginsVersion.java`, manifest).
5. **Approve and merge** the release PR.
6. Release-please creates `vX.Y.Z` and the GitHub Release.
7. The same workflow builds and uploads:
   - `signs-compat.yml`
   - `aelion-npcs-X.Y.Z.jar`
   - `aelion-signs-bukkit-1_8-X.Y.Z.jar`
   - `aelion-signs-bukkit-1_13-X.Y.Z.jar`
   - `aelion-signs-paper-1_17-X.Y.Z.jar`
   - `aelion-signs-paper-1_21-X.Y.Z.jar`
   - `aelion-signs-paper-26-X.Y.Z.jar`

## Manual re-run

**Actions → Release → Run workflow** to refresh the release PR without a new push.

## Version files (bumped by release-please)

| File | Field |
|------|--------|
| `.github/release-please-manifest.json` | Manifest version |
| `gradle.properties` | `version=` (`# x-release-please-version`) |
| `plugins-common/.../PluginsVersion.java` | `VERSION` (`// x-release-please-version`) |
| `CHANGELOG.md` | Generated notes |

Publish builds with `-Pversion=<tag>` so JAR names always match the Git tag.
