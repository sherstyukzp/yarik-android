# Upstream mesh stack (Android)

Yarik Android vendors the full [permissionlesstech/bitchat-android](https://github.com/permissionlesstech/bitchat-android) tree at the repository root. Protocol wire format and Kotlin packages stay aligned with upstream for mesh interoperability.

## Pinned upstream commit

| Field | Value |
|-------|--------|
| Repository | `https://github.com/permissionlesstech/bitchat-android.git` |
| Branch | `main` |
| Commit | `dfc67dad41691128bc6e7425bd69f2f409cb5bbf` |
| Imported | 2026-10-23 |

## Strategy

**Root vendor copy with documented SHA + git remote for sync** (not a submodule).

- Remote name: `mesh-upstream-android` → upstream URL above (rename locally if you still have legacy `bitchat-android` remote).
- Initial import: checked out upstream `main` at the pinned SHA onto `develop`, replacing the empty Compose starter on `main`.
- Rationale: upstream is a multi-module Gradle app (`:app`, `:wear`); submodule paths would complicate CI.

### Syncing newer upstream

```bash
git fetch mesh-upstream-android main
git merge mesh-upstream-android/main -m "chore: sync mesh-upstream-android"
```

After merge, update the commit hash in this file.

## Yarik vs upstream identifiers

| Setting | Yarik value | Notes |
|---------|-------------|--------|
| `applicationId` | `com.yarik.android` | Yarik install identity; protocol unchanged |
| `namespace` | `com.bitchat.android` | Upstream package layout (kept for mesh code) |
| Display name | Yarik (`app_name` strings) | User-facing branding |

Deep links and Wi‑Fi Aware service identifiers that use the `bitchat` scheme/name are **protocol constants** and remain unchanged for peer compatibility.

## First mesh message (manual test)

Two physical Android devices (BLE cannot be emulated reliably):

1. Install debug APK on both: `./gradlew :app:installDebug`
2. Grant **Nearby devices / Bluetooth** and **Location** (required for BLE scan on Android).
3. Disable battery optimization for the app if mesh drops in background (Settings → App → Battery).
4. Open app on both; ensure mesh indicator shows peers (same room, within ~10 m, Bluetooth on).
5. Stay on default **`#bluetooth`** public mesh channel (IRC-style input at bottom).
6. On device A, type a message and send. Confirm it appears on device B (and vice versa).

**Mesh Lab:** Upstream documents mesh testing under `.agents/skills/mesh-lab/` for controlled multi-device scenarios.

Single-device smoke: `./gradlew :app:assembleDebug` and launch — UI loads; mesh send/receive requires a second peer.
