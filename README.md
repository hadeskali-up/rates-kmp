# Rates KMP

Kotlin Multiplatform port of [rates-app](https://github.com/hadeskali-up/rates-app) (Flutter).

- **shared/** — common Kotlin module: FX data layer (Ktor + kotlinx-serialization), works on Android today, ready for iOS/desktop
- **composeApp/** — Android UI (Jetpack Compose, Material 3, AMOLED black theme)

Free key-less APIs: [open.er-api.com](https://open.er-api.com) (latest rates), [frankfurter](https://frankfurter.dev) (ECB timeseries), jsDelivr CDN (currency names).

## Build (Android only for now)

```bash
./gradlew :composeApp:assembleDebug
```

APK at `composeApp/build/outputs/apk/debug/`.
