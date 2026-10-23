# Upstream mesh stack (Android)

Network and protocol code lives in the **`:yarik-mesh`** Gradle module, vendored from [permissionlesstech/bitchat-android](https://github.com/permissionlesstech/bitchat-android). The `:app` module is Yarik’s product shell only.

## Pinned upstream commit

| Field | Value |
|-------|--------|
| Repository | `https://github.com/permissionlesstech/bitchat-android.git` |
| Branch | `main` |
| Commit | `dfc67dad41691128bc6e7425bd69f2f409cb5bbf` |
| Imported | 2026-10-23 |

## Strategy

**Library module (`:yarik-mesh`) with documented SHA + git remote for sync** (not a submodule).

- Remote name: `mesh-upstream-android` → upstream URL above.
- Import path: upstream tree at the pinned SHA; mesh sources under `yarik-mesh/`, not the Yarik app shell.
- Sync by merging upstream into `yarik-mesh/` and updating this commit hash.

### Syncing newer upstream

```bash
git fetch mesh-upstream-android main
# merge or cherry-pick into yarik-mesh, resolve conflicts
```

After merge, update the commit hash in this file.

## Wire / protocol identifiers (unchanged for interop)

These stay as upstream defines them; they are **not** shown in the Yarik app UI:

| Kind | Example | Notes |
|------|---------|--------|
| Deep link scheme | `bitchat://verify` | Re-enable in `:app` manifest when verification UX ships |
| Bluetooth / Wi‑Fi Aware service names | upstream constants in mesh code | Required for peer discovery |
| Kotlin packages | `com.bitchat.android.*` | Internal to `:yarik-mesh`; prefer new API under `com.yarik.mesh.*` |

| Setting | Yarik value | Notes |
|---------|-------------|--------|
| `:app` `applicationId` | `com.yarik.android` | Install identity |
| `:yarik-mesh` `namespace` | `com.bitchat.android` | Matches upstream layout |

## Manual mesh smoke (after chat UI wired)

Two physical Android devices with BLE:

1. `./gradlew :app:installDebug` on both
2. Grant Bluetooth / Nearby devices, Location, Notifications
3. Confirm mesh peers on the chat screen (future UI)
4. Send on `#bluetooth` and verify receive

Shell-only build today: `./gradlew :app:assembleDebug` and launch — starter home + background mesh init.
