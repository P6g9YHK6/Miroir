# Miroir

> *Miroir* because your screen deserves a reflection, not a copy.
> 
<p align="center">
  <img src="graphics/icon.svg" width="120" height="120" alt="Miroir logo" />
</p>

Stream your Android screen to any browser on your local Wi-Fi network. No accounts, no cloud, no cables. Open the app, tap Start, and share the URL—anyone on the same network can watch immediately in Chrome, Firefox, Safari, or any modern browser.


<img width="2076" height="2152" alt="Screenshot" src="https://github.com/user-attachments/assets/5012f099-c2f5-4e8c-b504-1d371f19165c" />

---

## Features

- **Live video**: MJPEG stream viewable in any browser, no plugins required
- **Audio streaming**: PCM audio capture (16/22/44.1/48 kHz, mono/stereo, 8/16/32-bit)
- **Adjustable frame rate**: 5, 10, 15, 24, 30, or 60 fps
- **JPEG quality slider**: trade sharpness for bandwidth (10–100%)
- **Configurable port**: 1–65535 (default 8080; ports < 1024 need root)
- **Authentication**: optional PIN or Basic Auth (off by default)
- **Self-signed HTTPS**: optional TLS with browser warning
- **Auto-restart / Auto-reconnect**: stream restarts on client/server interruption
- **App shortcuts**: 6 ADB shortcuts (start/stop, 4 presets)
- **No internet required**: local network only

---

## Why a browser instead of Miracast or Chromecast

Miracast and Chromecast are standard ways to mirror an Android screen, but both have many failure modes:

- **Miracast** requires Wi-Fi Direct, which many routers, corporate networks, and hotel networks block. Some manufacturers ship broken implementations. Compatibility is inconsistent; connections often fail silently.
- **Chromecast** requires a Google account, the Google Home app, and same network segment. It fails on networks that isolate clients (offices, hotels, schools, hotspots). Requires a Chromecast device on the display.
- Both have **codec negotiation** that fails depending on device/driver/firmware, often with no useful error message.

Miroir sidesteps all of this. It runs a plain HTTP server on your phone serving MJPEG over standard browser requests. Any device with a browser and network access can view: laptop, desktop, smart TV, another phone. No pairing, no viewer app install, no codec negotiation, no protocol handshake. If the browser loads a webpage, it displays the stream.

Useful where casting fails: presentations to a projector via laptop, streaming to a car browser, demos on networks blocking Wi-Fi Direct.

### Projector / presentation
Connect laptop to projector, open stream URL in browser. Present from phone wirelessly, no HDMI dongle.

### Mobile game on TV
Stream game with audio to smart TV browser or laptop connected to TV.

### Car screen
Open stream URL in car's built-in browser. Note for Tesla: browser refuses RFC 1918 private IPs (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), so phone IP must fall outside those ranges.

### Demo or support
Show exactly what's on your screen over local network. Useful for tech support, app demos, or walkthroughs.

### Home dashboard
Leave stream open on wall-mounted screen/tablet for home automation dashboard, live camera feed, or any phone app.

---

## Requirements

- Android 6.0+ (API 23+)
- Wi-Fi connection shared with viewing device
- Audio capture requires Android 10+ (API 29+)

---

## Setup

```bash
git clone https://github.com/P6g9YHK6/Miroir.git
cd Miroir
```

## Build

### Prerequisites
- Android Studio Hedgehog (2023.1) or newer
- JDK 21
- Android SDK with API 36

### From Android Studio
1. Open the `Miroir` folder
2. Let Gradle sync complete
3. Connect a device with USB debugging enabled
4. Run → Run 'app'

### From the command line
```bash
cd Miroir
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions
Every push builds debug + unsigned release APKs, available as workflow artifacts for 30 days.

---

## Usage

1. Open **Miroir** on your Android device
2. Configure frame rate, quality, audio, port, authentication, HTTPS as needed
3. Tap **Start Streaming** and accept screen capture prompt
4. App shows stream URL, e.g. `http://192.168.1.42:8080` (or `https://...` if HTTPS on)
5. Open that URL in any browser on same Wi-Fi (accept cert warning if HTTPS)

---

## Permissions

| Permission | Purpose |
| --- | --- |
| `FOREGROUND_SERVICE` / `MEDIA_PROJECTION` | Capture screen in background |
| `INTERNET` | Serve HTTP stream on local network |
| `RECORD_AUDIO` | Audio capture (Android 10+, optional) |
| `ACCESS_WIFI_STATE` | Read local IP for stream URL |
| `POST_NOTIFICATIONS` | Error notifications (Android 13+) |

---

## Known limitations

- Self-signed HTTPS cert lacks Subject Alternative Name → browser shows extra warning (cosmetic)
- Audio is PCM over raw HTTP; some browsers need user gesture for auto-play
- Max capture resolution capped at 1280px on long edge for performance
- Not tested on all Android versions/device configurations