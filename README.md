# Yarik (Android)

**Yarik** is UkropSoft’s decentralized mesh messenger for Android: Bluetooth LE mesh for offline peer-to-peer chat, plus Nostr for internet reach. No accounts, no phone numbers, no central servers.

This repository vendors the [permissionlesstech mesh Android stack](https://github.com/permissionlesstech/bitchat-android) at a pinned commit (see [docs/UPSTREAM.md](docs/UPSTREAM.md)) and tracks Yarik-specific branding and release settings.

- **Jira:** [YA backlog](https://ukropsoft.atlassian.net/jira/software/projects/YA/boards/246/backlog)
- **Project docs:** [docs/YARIK.md](docs/YARIK.md)

## Features

- **Dual transport:** BLE mesh offline; Nostr relays for global / geohash channels
- **Mesh `#bluetooth`:** Default public channel for MVP transport proof
- **Noise Protocol** end-to-end encryption for private mesh messages
- **Wi‑Fi Aware** local mesh where supported
- **Wear OS companion** (`:wear` module)

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

Runtime permissions: Bluetooth / Nearby devices, Location (Android BLE scan requirement), Notifications.

## First mesh message (manual)

Two physical Android devices (BLE is unreliable on emulators):

1. Install debug APK on both phones.
2. Grant Bluetooth and Location; complete onboarding.
3. Stay on **`#bluetooth`** and send text from device A; confirm on device B.

Details: [docs/UPSTREAM.md](docs/UPSTREAM.md#first-mesh-message-manual-test).

## Testing

```bash
./gradlew test
./gradlew lint
./gradlew connectedAndroidTest   # device/emulator required
```

## License

Public domain — see [LICENSE.md](LICENSE.md).
