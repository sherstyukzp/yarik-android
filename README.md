# Yarik (Android)

**Yarik** is UkropSoft’s decentralized mesh messenger for Android. The product shell (`:app`) is a Jetpack Compose starter; BLE mesh, Nostr, and crypto run in the `:yarik-mesh` library (vendored upstream — see [docs/UPSTREAM.md](docs/UPSTREAM.md)).

- **Jira:** [YA backlog](https://ukropsoft.atlassian.net/jira/software/projects/YA/boards/246/backlog)
- **Architecture:** [docs/YARIK.md](docs/YARIK.md)

## Building

Requires JDK 21, Android SDK (see `gradle/libs.versions.toml`), and `local.properties` pointing at your SDK (gitignored).

```bash
git clone https://github.com/sherstyukzp/yarik-android.git
cd yarik-android
git checkout develop
./gradlew :app:assembleDebug
```

Install on a device:

```bash
./gradlew :app:installDebug
```

The starter home screen confirms the app shell; mesh background services start via `YarikMesh.initialize()` in `YarikApplication`. Full chat UX is wired in a follow-up on top of `:yarik-mesh`.

## License

See [LICENSE.md](LICENSE.md).
