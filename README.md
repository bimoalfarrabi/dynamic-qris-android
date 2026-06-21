# 💳 Dynamic QRIS — Android

> Android app untuk generate & monitor QRIS dinamis. Kotlin + Jetpack Compose + Material 3.

```
┌─────────────────┐         ┌──────────────────────┐         ┌─────────────────┐
│  Android App    │◄───────►│  Laravel Backend      │◄───────►│  QRIS-ify API   │
│  (Compose)      │  REST   │  (API + Dashboard)    │  REST   │                 │
│                 │         │                       │◄────────│  (Webhook)      │
└─────────────────┘         └──────────────────────┘         └─────────────────┘
```

## ✨ Fitur

- **Biometric Lock** — fingerprint/PIN required saat buka app
- **Create Transaction** — input nominal + expiry, langsung generate QRIS
- **QR Display** — render lokal via ZXing, countdown timer real-time
- **Auto Polling** — cek status tiap 3 detik, auto-stop saat terminal (SUCCESS/EXPIRED/CANCELLED)
- **Cancel** — batalkan transaksi pending dengan confirm dialog
- **Transaction History** — filter chips, offline-first via Room
- **Dynamic Theme** — Material You (Android 12+), teal fallback di bawahnya

## 🛠 Tech Stack

| Layer | Tech |
|-------|------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Network | Retrofit + kotlinx.serialization |
| Local DB | Room |
| Navigation | Navigation Compose |
| Auth | Biometric API + device credential fallback |
| QR | ZXing Core (local render) |
| Image | Coil |

## 🚀 Quick Start

```bash
# Clone
git clone https://github.com/bimoalfarrabi/dynamic-qris-android.git
cd dynamic-qris-android
```

Buat/edit `local.properties`:

```properties
sdk.dir=/path/to/Android/Sdk
API_BASE_URL=http://10.0.2.2:8000/api/
API_TOKEN=your-sanctum-token-here
```

> `10.0.2.2` = localhost dari Android emulator.
> Untuk physical device, gunakan IP lokal mesin (misal `http://192.168.x.x:8000/api/`).

Buka di Android Studio → Sync Gradle → Run.

### Build via CLI

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## 🌐 Backend

App ini membutuhkan Laravel backend sebagai API proxy ke QRIS-ify:

👉 **[dynamic-qris-web](https://github.com/bimoalfarrabi/dynamic-qris-web)**

### Mendapatkan API Token

Di backend, jalankan:

```bash
php artisan tinker
>>> User::first()->createToken('android')->plainTextToken
```

Copy token ke `local.properties` → `API_TOKEN`.

## 📡 API yang Digunakan

App berkomunikasi **hanya** dengan Laravel backend, tidak langsung ke QRIS-ify.

| Action | Endpoint | Method |
|--------|----------|--------|
| List transaksi | `/api/transactions` | `GET` |
| Detail transaksi | `/api/transactions/{id}` | `GET` |
| Buat transaksi | `/api/transactions` | `POST` |
| Batalkan | `/api/transactions/{id}/cancel` | `POST` |

## 🏗 Arsitektur

MVVM + Repository pattern, offline-first.

```
app/src/main/java/id/viasco/dynamic_qris_android/
├── DynamicQrisApp.kt              # @HiltAndroidApp
├── MainActivity.kt                # Biometric gate → NavHost
├── domain/model/
│   ├── Transaction.kt             # Domain data class
│   └── TransactionStatus.kt      # Enum (PENDING, SUCCESS, EXPIRED, CANCELLED)
├── data/
│   ├── local/                     # Room: Database, DAO, Entity, Converters
│   ├── remote/                    # Retrofit: API interface, DTOs
│   ├── mapper/                    # DTO ↔ Entity ↔ Domain
│   └── repository/               # Single source of truth (Room + API sync)
├── di/                            # Hilt modules
│   ├── NetworkModule.kt           # OkHttp + Retrofit + auth interceptor
│   └── DatabaseModule.kt          # Room provider
└── ui/
    ├── auth/                      # BiometricAuthHelper
    ├── common/                    # StatusChip, Formatters, QrEncoder, UiState
    ├── create/                    # CreateTransactionScreen + ViewModel
    ├── qr/                        # QrDisplayScreen + polling + cancel
    ├── history/                   # HistoryScreen + filter chips
    ├── detail/                    # TransactionDetailScreen
    ├── navigation/                # Screen routes + AppNavHost
    └── theme/                     # Dynamic color + teal fallback palette
```

## 📋 Requirements

| | |
|---|---|
| Android Studio | Ladybug+ |
| JDK | 17+ |
| Compile SDK | 35 |
| Min SDK | 31 (Android 12) |
| Target SDK | 34 |
| AGP | 8.13.2 |
| Kotlin | 2.0.21 |

## 🔒 Security

- API token **tidak** di-hardcode di source — inject via `BuildConfig` dari `local.properties`
- `local.properties` di-gitignore, tidak masuk repo
- Cleartext traffic hanya diizinkan ke `10.0.2.2` (emulator loopback)
- Biometric/PIN wajib setiap kali app dibuka

## 📄 Lisensi

MIT
