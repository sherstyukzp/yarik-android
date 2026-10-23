# Yarik Android

| Item | Value |
|------|--------|
| Display name | **Yarik** |
| Application ID | `com.yarik.android` |
| Default branch | `develop` |
| Repository | https://github.com/sherstyukzp/yarik-android |
| Jira | [YA](https://ukropsoft.atlassian.net/jira/software/projects/YA/boards/246/backlog) |

## Scope

Yarik ships the full upstream mesh messenger tree (see [UPSTREAM.md](UPSTREAM.md)) with Yarik branding. Kotlin package namespaces remain upstream (`com.bitchat.android`, etc.) so mesh protocol and shared test code stay compatible without a large refactor.

## MVP acceptance

- `./gradlew :app:assembleDebug` succeeds on `develop`
- Two phones: send/receive text on mesh channel `#bluetooth`
- Optional later: cross-platform with [yarik-ios](https://github.com/sherstyukzp/yarik-ios)

## Starter template

`main` holds a minimal Jetpack Compose starter (`com.ukropsoft.yarik`). **`develop` replaces that template** with the integrated mesh codebase; new work targets `develop`.
