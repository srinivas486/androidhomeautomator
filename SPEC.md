# androidhomeautomator — SPEC.md

**Project:** Unified Home Network & Security Dashboard
**Repo:** srinivas486/androidhomeautomator
**Platform:** Native Android (Kotlin + Jetpack Compose)
**Issue:** Closes #1

---

## 1. Overview

An Android app that serves as a unified dashboard for Vasu's home network and security infrastructure. The app connects to existing APIs (Sophos XG firewall, Tailscale VPN, cameras) rather than directly monitoring — making it more reliable and easier to maintain.

**Target users:** Vasu (home network owner, technical)
**Remote access:** Works via Tailscale VPN (app connects to 192.168.40.x over the tailnet)

---

## 2. Architecture

### 2.1 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9+ |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Networking | Retrofit + OkHttp + Moshi |
| Async | Kotlin Coroutines + Flow |
| Navigation | Jetpack Navigation Compose |
| Local Storage | DataStore (preferences) |
| Image/Video | ExoPlayer (RTSP), Coil (thumbnails) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

### 2.2 Module Architecture

Each device/service type is a **plugin module** implementing a common interface:

```
app/
├── core/
│   ├── module/          # Plugin interface & base classes
│   ├── network/         # Retrofit setup, auth interceptors
│   ├── storage/         # DataStore for credentials & preferences
│   └── ui/              # Shared composables, theme, components
├── modules/
│   ├── firewall/        # Sophos XG module
│   ├── tailscale/       # Tailscale module
│   ├── camera/          # Camera module (RTSP)
│   ├── network/         # Network dashboard module
│   └── plugin-slot/     # Extensible plugin scaffold
└── MainActivity.kt
```

**Plugin Interface:**
```kotlin
interface HomeModule {
    val id: String
    val name: String
    val icon: ImageVector
    fun getDashboardTile(): @Composable () -> Unit
    fun getDetailScreen(): @Composable () -> Unit
    suspend fun refresh()
}
```

Modules are discovered via `META-INF/services` at compile time — new modules are added as dependencies without modifying core app code.

### 2.3 API Integration Architecture

Each module owns its API client:

| Module | Base URL | Auth |
|--------|----------|------|
| Firewall (Sophos XG) | `https://192.168.40.1:4444/api/` | Basic auth (admin:Hastala1234#) |
| Tailscale | `https://api.tailscale.com/api/v2/` | API key (stored in DataStore) |
| Camera | RTSP URLs configured per camera | None (local network) |

**Security note:** Credentials stored in Android EncryptedSharedPreferences (not plain DataStore). API key for Tailscale entered manually by user and stored securely.

---

## 3. Module Specifications

### 3.1 Firewall Module (Sophos XG)

**Target:** `192.168.40.1:4444`
**API:** Sophos XG REST API v2

**Features:**
- View list of firewall rules (name, action, status, source/dest)
- Enable/disable individual rules (toggle)
- View current firewall status (uptime, threat level)
- Refresh on pull-to-refresh

**API Endpoints (estimated):**
```
GET  /api/firewall/rules              → list rules
POST /api/firewall/rules/{id}/toggle  → enable/disable rule
GET  /api/system/status                → system health
```

**UI:** Card per rule with toggle switch. Green/amber/red status indicators.

---

### 3.2 Tailscale Module

**Target:** `192.168.40.6` (local Tailscale device)
**API:** `https://api.tailscale.com/api/v2/`
**Auth:** API key (stored in EncryptedSharedPreferences)

**Features:**
- View current ACL policy
- Show active routes and advertised subnets
- Enable/disable ACL tags or user permissions (read-only unless write key provided)
- View connected devices on tailnet
- Check Tailscale daemon status on 192.168.40.6

**API Endpoints:**
```
GET  /api/v2/acl                        → current ACL policy
GET  /api/v2/routes                     → advertised routes
GET  /api/v2/device/{id}               → device details
GET  /api/v2/acl/can-access?device=... → check access
```

**UI:** Tailscale-branded card grid. Devices list with online/offline status.

---

### 3.3 Camera Module

**Features:**
- Grid view of all cameras (configurable)
- Live video stream via ExoPlayer (RTSP → HLS or direct RTSP)
- Camera-specific URL configured per camera
- Support multiple brands via RTSP URL pattern
- Full-screen view on tap
- Refresh stream button

**Camera Configuration (stored in DataStore):**
```json
{
  "cameras": [
    {
      "id": "front-door",
      "name": "Front Door",
      "rtspUrl": "rtsp://user:pass@192.168.40.x:554/stream1",
      "brand": "generic"
    }
  ]
}
```

**RTSP Handling:**
- ExoPlayer with RTSP extension (`implementation 'androidx.media3:media3-exoplayer-rtsp'`)
- If RTSP fails, show last-known thumbnail (if available)
- Timeout: 10 seconds before showing error state

**UI:** 2-column grid (portrait), 3-column (landscape). Thumbnail + name overlay.

---

### 3.4 Network Dashboard Module

**Data Sources:**
- Primary: Sophos XG reports API (bandwidth, top sites)
- Fallback: ntopng API if Sophos not exposing reports

**Features:**
- Connected devices list (DHCP clients from Sophos)
- Bandwidth usage per device (upload/download, last 24h)
- Top visited domains (from DNS/proxy logs)
- Data refreshed every 60 seconds (configurable)

**API Endpoints (Sophos XG):**
```
GET /api/reports/bandwidth
GET /api/reports/dns-queries
GET /api/dhcp/leases
```

**UI:** Scrollable dashboard with cards:
- Bandwidth graph (line chart, last 24h)
- Top 5 devices by data usage (bar chart)
- Top 10 domains (list)
- Device list with online status

---

### 3.5 Plugin Slot (Extensible)

**Future modules can be added:**
- Robo vacuum (local API — brand TBD)
- Generic HTTP device
- Custom NAT service

**Scaffold provided:** Empty `GenericModule` with URL + auth config.

---

## 4. UI/UX Design

### 4.1 Visual Style

- **Design system:** Material Design 3
- **Theme:** Dark mode default (matches network/security dashboard vibe)
- **Colors:**
  - Background: #121212 (dark surface)
  - Primary: #00BCD4 (cyan — network/tech feel)
  - Secondary: #4CAF50 (green — online/success)
  - Error: #F44336 (red — alert/offline)
  - Surface: #1E1E1E
- **Typography:** System default (Roboto)
- **Icons:** Material Icons

### 4.2 Navigation

```
MainScreen (Dashboard)
├── Dashboard (home) — grid of module tiles
├── Firewall — full firewall management
├── Tailscale — VPN status & ACL
├── Cameras — camera grid
├── Network — bandwidth & devices
└── Settings — credentials, refresh intervals, plugin management
```

- Bottom navigation bar with 5 destinations
- Each module has its own back-stack detail screen
- Settings accessible from top app bar

### 4.3 Dashboard Home Screen

Grid of tiles (2 columns):
- Tile: icon + module name + status indicator
- Tap → navigate to module detail
- Pull-to-refresh all modules
- Status: green (OK), amber (warning), red (offline/error)

---

## 5. Data Flow

```
API Call (Coroutine)
    ↓
Repository (handles errors, maps to domain models)
    ↓
ViewModel (exposes StateFlow to UI)
    ↓
Compose UI (observes StateFlow)
```

- Each module has its own Repository + ViewModel
- Shared `NetworkModule` provides configured Retrofit instances
- Error states propagated as UI state (not exceptions)

---

## 6. Security Considerations

| Concern | Solution |
|---------|----------|
| Credentials on device | EncryptedSharedPreferences (AES-256) |
| Plaintext RTSP passwords | Stored securely, never in logs |
| Tailscale API key | User-provided, stored encrypted |
| Sophos credentials | User-provided on first launch, stored encrypted |
| Network interception | HTTPS only for remote APIs; local network uses self-signed cert trust |
| Certificate validation | Option to disable for local Sophos (user opts in) |

---

## 7. Project Setup

### 7.1 Build Configuration

- **Gradle:** Kotlin DSL
- **Version catalog:** `gradle/libs.versions.toml`
- **Min SDK:** 26, Target SDK: 34
- **Compose BOM:** 2024.02.00

### 7.2 Initial Dependencies

```
// UI
implementation(libs.androidx.compose.bom)
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.material3)
implementation(libs.androidx.navigation.compose)

// Architecture
implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation(libs.androidx.hilt)

// Networking
implementation(libs.retrofit)
implementation(libs.okhttp)
implementation(libs.moshi)
implementation(libs.media3.exoplayer.rtsp)

// Storage
implementation(libs.datastore.preferences)

// Coroutines
implementation(libs.kotlinx.coroutines)
```

### 7.3 GitHub Issue First

- Issue #1 created before any code
- Branch: `feature/androidhomeautomator/setup`
- PR: link to issue #1, mention "OpenClaw"

---

## 8. Implementation Order

### Epic 1: Project Shell + Core Infrastructure
1. Create repo, add README, create issue #1
2. Set up Gradle project with version catalog
3. Add core dependencies (Compose, Hilt, Retrofit, Navigation)
4. Set up EncryptedSharedPreferences for credential storage
5. Create base module interface + plugin discovery
6. Create shared UI theme + components
7. Create navigation structure
8. `.gitignore` before first `git add`

### Epic 2: Dashboard + Settings
1. Dashboard home screen (tile grid)
2. Bottom navigation
3. Settings screen (credential inputs per module)
4. Credential storage flow

### Epic 3: Firewall Module
1. Sophos XG API client (Retrofit)
2. FirewallRepository + data models
3. FirewallViewModel
4. Firewall rules list screen
5. Rule toggle functionality
6. Error handling + loading states

### Epic 4: Tailscale Module
1. Tailscale API client
2. TailscaleRepository + data models
3. TailscaleViewModel
4. Devices list screen
5. ACL view (read-only initially)
6. Status indicators

### Epic 5: Camera Module
1. Camera configuration (DataStore)
2. ExoPlayer RTSP setup
3. CameraRepository
4. CameraViewModel
5. Camera grid screen
6. Full-screen video player
7. Camera add/edit flow

### Epic 6: Network Dashboard Module
1. Network API client (Sophos reports)
2. NetworkRepository + data models
3. NetworkViewModel
4. Bandwidth chart (MPAndroidChart or custom Canvas)
5. Device list + top domains

### Epic 7: Plugin Slot + Polish
1. Generic plugin scaffold
2. Module enable/disable in settings
3. Pull-to-refresh all
4. Offline mode (show last-known data)
5. App icon + splash screen

---

## 9. Open Questions / Future

1. **Camera:** Hisense — RTSP URL format needs verification ( Hisense cameras often use standard RTSP streams via `rtsp://user:pass@ip:554/live`)
2. **Robo vacuum:** Ecovacs DEEBOT X8 — T30S Pro or X8 variant; Ecovacs API (Xavier protocol) or local HTTP control
3. **TV:** LG C9 (WebOS) — WebOS HTTP control API (`https://ip:3002/`), LG ThinQ integration possible
4. **Projector:** Hisense PT1 (Android TV) — ADB debugging, or HDMI-CEC via connected AVR
5. **AVR:** Denon X3800H — HTTP control API (Denon/Marantz HEOS protocol) on port 80/8080
6. **Media Players:** NVIDIA Shield — ADB, Kodi JSON-RPC, SmartTube
7. **Media Server:** Jellyfin — Jellyfin API (`/embyserver/`) for library status, now playing, etc.
8. **Tailscale write access:** Currently read-only; write requires additional API key permissions
9. **Sophos reports API:** Need to verify exact endpoint paths for bandwidth/dns data

## 10. Device Inventory (Vasu's Setup — March 2026)

| Device | Type | Control Method | Notes |
|--------|------|----------------|-------|
| Sophos XG (192.168.40.1) | Firewall | REST API (port 4444) | admin/Hastala1234# |
| Tailscale (192.168.40.6) | VPN | api.tailscale.com | API key auth |
| LG C9 | Smart TV | WebOS HTTP API | Control power, inputs, apps |
| Hisense PT1 | Projector | Android TV ADB / HDMI-CEC | Remote control app exists |
| Denon X3800H | AVR | HTTP control (port 8080) | Power, volume, inputs, zone control |
| NVIDIA Shield (x1+) | Media Player | ADB + Kodi JSON-RPC | Play/pause, volume, launch apps |
| Jellyfin | Media Server | Jellyfin HTTP API | Library stats, now playing |
| Hisense Camera | Security | RTSP | Live stream |
| Ecovacs DEEBOT X8 | Robot Vacuum | Ecovacs API (Xavier) | Clean, charge, status |
| Shield (x1+) | Media Player | ADB + Kodi JSON-RPC | Play/pause, volume, launch apps |
