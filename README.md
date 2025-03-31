# WobblyByte

An Android app that generates strong passwords, stores them in an encrypted vault, and teaches basic cybersecurity through short quizzes.

This is a 2026 rebuild. The original WobblyByte was a 2022 hackathon entry built in MIT App Inventor that won its event. This version is native Android (Java) with the generation logic in Python, written from scratch to match the project's description and to be runnable and reviewable.

## What it does

**Generate.** Choose a length from 8 to 64 and any mix of lowercase, capitals, numbers, and symbols. An option skips look-alike characters (`I l 1 O 0 o`). Every password is guaranteed to contain at least one character from each type you picked. Shake the phone to generate another one. Each result shows an estimated strength and how long the Python call took, measured around the Java-to-Python round trip on the device.

**Vault.** Save a password with a site name and username. Opening an entry asks for your fingerprint, face, or PIN first.

**Quiz.** Four modules (password habits, phishing, scams, safe browsing) with six questions each. Every attempt is saved, and the results screen shows your first score, latest score, best score, and the change since your first try.

**Learn.** Links to the Bitwarden strength tester, Have I Been Pwned, CISA, the FTC, and the EFF.

## How it is built

```
Java UI (fragments, Material 3)
   |
   |  PasswordBridge  ->  Chaquopy  ->  wobblebyte/generator.py
   |                                    (secrets module, returns JSON)
   |
   |  VaultAccess -> BiometricPrompt
   |  Vault -> VaultCrypto (AES-256-GCM) -> SQLite
   |             key lives in Android Keystore
   |
   |  QuizRepository (assets/quiz.json) -> QuizSession -> ScoreStore
```

The Python generator is pure standard library and has no Android dependency, so it is tested with plain `pytest` on a laptop. Chaquopy embeds CPython 3.12 in the APK and the Java side calls `generate_with_meta(...)`, which returns a JSON string. The app starts the Python runtime when it launches so the first tap does not pay the startup cost.

## Security design

- **Encryption.** Credentials are encrypted with AES-256 in GCM mode. Each entry gets a fresh random 12-byte nonce, and GCM authenticates the data, so a tampered row fails to decrypt instead of returning garbage. The username and password are sealed together as one blob.
- **Key protection.** The key is generated inside the Android Keystore, in the StrongBox chip on phones that have one, and never enters app memory. It is only usable within 30 seconds of a successful biometric or PIN check. The app refuses to open the vault if the phone has no screen lock.
- **Biometric authentication.** `BiometricPrompt` accepts Class 3 (strong) biometrics or the device PIN. If the key rejects an operation because the unlock window expired, the app prompts again and retries once.
- **Screen and clipboard.** The main activity and the reveal dialog set `FLAG_SECURE`, which blocks screenshots and hides the app in the recents view. Copied passwords are flagged as sensitive on Android 13 and later and cleared from the clipboard after 30 seconds.
- **Backups.** `allowBackup` is off, so the encrypted database is not uploaded to cloud backup, where it would be unreadable anyway.
- **Randomness.** Passwords come from Python's `secrets` module and the final character order is shuffled with `SystemRandom`.

## Run it

Requirements: JDK 17 and the Android SDK (or an Android Studio release that supports Android Gradle Plugin 8.13), Python 3.12 on your PATH for the Chaquopy build step, and a phone or emulator on Android 11 or later with a screen lock set.

```
./gradlew installDebug
```

Or open the folder in Android Studio and press Run. GitHub Actions also builds a debug APK on every push; download it from the workflow run's artifacts. The APK is about 48 MB because it bundles the Python runtime for both arm64 phones and x86_64 emulators.

## Tests

```
pip install -r requirements-dev.txt
pytest                              # 28 tests for the generator
./gradlew testDebugUnitTest         # 18 tests for quiz parsing, sessions, and score stats
```

The Python tests cover length limits, character-type guarantees, the ambiguous-character filter, entropy math, and the JSON contract with the Java side. The JUnit tests cover everything in the quiz package. The Keystore, biometric, and UI code needs a device and is not covered by automated tests.

## Limitations

- Site names are stored in plaintext so the vault list can show without prompting. Usernames and passwords are encrypted.
- The 30-second clipboard clear is a timer in the app. If Android kills the app first, the clipboard keeps the password until something else replaces it.
- If you remove your screen lock, Android deletes the Keystore key and saved passwords cannot be recovered.
- No cloud sync, no export, no autofill service.
- Android 11 or later only, because the Keystore authentication-window API used here starts at API 30.

## Layout

```
app/src/main/python/wobblebyte/   password generator (Python)
app/src/main/java/.../python/     Java bridge to the generator
app/src/main/java/.../crypto/     Keystore key and AES-GCM
app/src/main/java/.../auth/       biometric prompt and retry logic
app/src/main/java/.../data/       SQLite vault
app/src/main/java/.../quiz/       quiz model, parser, session, score history
app/src/main/java/.../ui/         screens
app/src/main/assets/quiz.json     quiz content
tests/                            Python tests
app/src/test/                     JUnit tests
```
