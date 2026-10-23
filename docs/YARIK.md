# Yarik Android

| Item | Value |
|------|--------|
| Display name | **Yarik** |
| Application ID | `com.yarik.android` |
| App namespace | `com.yarik.android` |
| Default branch | `develop` |
| Repository | https://github.com/sherstyukzp/yarik-android |
| Jira | [YA](https://ukropsoft.atlassian.net/jira/software/projects/YA/boards/246/backlog) |

## Module layout

| Module | Role |
|--------|------|
| `:app` | **Product shell** — Jetpack Compose starter UI from `main`, `YarikApplication`, launcher. User-facing Yarik branding only. |
| `:yarik-mesh` | **Mesh stack** — vendored upstream network/crypto/Nostr/BLE code. Internal packages remain mostly `com.bitchat.android.*` where renames would affect wire/interop; public hook is `com.yarik.mesh.YarikMesh`. |

Legacy starter on `main` used `com.ukropsoft.yarik`; **`develop` standardizes on `com.yarik.android`.**

## Calling mesh from the app

In `Application.onCreate()` (see `YarikApplication`):

```kotlin
import com.yarik.mesh.YarikMesh

YarikMesh.initialize(this)
```

That starts Tor (if available), Nostr background runtime, and `MeshForegroundService`. Future chat UI can depend on `:yarik-mesh` and reuse upstream `MainActivity` / view models, or new screens under `com.yarik.android`.

## MVP acceptance

- `./gradlew :app:assembleDebug` succeeds on `develop`
- App shows starter home screen; mesh runs in background after init
- Two-device mesh messaging: wire a chat screen to `:yarik-mesh` (not in shell MVP)

## Related docs

- [UPSTREAM.md](UPSTREAM.md) — pinned upstream SHA and protocol identifiers
