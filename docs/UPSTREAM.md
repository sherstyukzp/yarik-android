# Upstream integration (Bitchat Android)

Yarik Android ships the full [permissionlesstech/bitchat-android](https://github.com/permissionlesstech/bitchat-android) tree at the repository root.

## Pinned upstream commit

| Field | Value |
|-------|--------|
| Repository | `https://github.com/permissionlesstech/bitchat-android.git` |
| Branch | `main` |
| Commit | `dfc67dad41691128bc6e7425bd69f2f409cb5bbf` |
| Imported | 2026-10-23 |

## Strategy

**Root vendor copy with documented SHA + git remote for sync** (not a submodule).

- Remote name: `bitchat-android` → upstream URL above.
- Initial import: checked out upstream `main` at the pinned SHA onto branch `cursor/integrate-bitchat-909b`, replacing the empty Yarik Compose starter.
- Rationale: upstream is a single Gradle multi-module app (`:app`, `:wear`); a submodule would complicate CI and local paths; subtree at root is equivalent to this import for a greenfield Yarik repo.

### Syncing newer upstream

```bash
git fetch bitchat-android main
# Review diff, then merge (resolve conflicts in branding/docs only if we diverge):
git merge bitchat-android/main -m "chore: sync bitchat-android upstream"
```

After merge, update the commit hash in this file.

## Branding (MVP)

| Setting | Value | Notes |
|---------|--------|--------|
| `applicationId` | `com.bitchat.droid` | **Unchanged** for MVP — matches official Play/protocol peers, zero Gradle churn. |
| `namespace` | `com.bitchat.android` | Upstream package layout. |
| Display name | Upstream strings | Custom Yarik UI deferred; transport proof uses upstream IRC-style UI. |

Future rebrand: change `applicationId` to `com.yarik.*` only when signing keys and side-by-side install with official Bitchat are required.

## First mesh message (manual test)

Two physical Android devices (BLE cannot be emulated reliably):

1. Install debug APK on both: `./gradlew :app:installDebug`
2. Grant **Nearby devices / Bluetooth** and **Location** (required for BLE scan on Android).
3. Disable battery optimization for the app if mesh drops in background (Settings → App → Battery).
4. Open app on both; ensure mesh indicator shows peers (same room, within ~10 m, Bluetooth on).
5. Stay on default **`#bluetooth`** public mesh channel (IRC-style input at bottom).
6. On device A, type a message and send. Confirm it appears on device B (and vice versa).

**Mesh Lab (official):** Upstream documents a mesh testing skill under `.agents/skills/mesh-lab/` for controlled multi-device scenarios; use when available in your environment.

Single-device smoke: `./gradlew :app:assembleDebug` and launch — UI loads; mesh send/receive requires a second peer.
