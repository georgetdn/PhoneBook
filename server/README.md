# PhoneBook interpretation service

1. Set `OPENAI_API_KEY` in the server process environment using your local secret manager. The service defaults to `gpt-4.1-mini`. Set `OPENAI_MODEL` on the server to use another model that supports Structured Outputs.
2. From the project root, run `node server/interpret.mjs` with Node.js 20 or later.
3. Run the Android debug build in the Android Emulator. The debug app uses `http://10.0.2.2:8787/interpret`, which reaches the service on the development machine.
4. Send `Add Jane Smith +1 202-555-0101`, then ask `What's Jane Smith's number?` The app should display the saved number and ID. Stop the service and repeat the question to check the fallback, then send `Find Jane Smith` to look up the entry offline.

## Test on a physical Android phone over USB

With USB debugging enabled and the development service running, forward port 8787 from the phone to the computer and install a debug build pointed at that port. Run these commands from the project root (use `./gradlew` on Linux or macOS).

```powershell
adb reverse tcp:8787 tcp:8787
.\gradlew.bat :app:installDebug "-PphoneBookInterpretationUrl=http://127.0.0.1:8787/interpret"
```

Repeat the forwarding command if the USB debugging connection is reset. To keep the endpoint when rebuilding from Android Studio, set `phoneBookInterpretationUrl=http://127.0.0.1:8787/interpret` in your user Gradle properties (`~/.gradle/gradle.properties`). Remove that setting to restore the emulator default.

## Connect to a hosted server

For a release build or a physical phone without USB forwarding, host the endpoint behind an HTTPS gateway that limits access and request rates. Build with `-PphoneBookInterpretationUrl=https://your-host.example/interpret`. Release builds default to offline commands until an HTTPS endpoint is configured.

The endpoint receives a single question and returns either a name or a clarification status. Room performs the search on the device.
