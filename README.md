# HomeQuest

Gamified household expense manager for Android. Log day-to-day spending, split it with family or friends, keep budgets, and earn points that unlock themes, avatar outfits and features.

- Native Android · Kotlin · Jetpack Compose · Material 3
- Local-first: Room database + DataStore, no backend or account required
- minSdk 26 · targetSdk 35

## Build

Open the folder in Android Studio (Ladybug or newer) and run the `app` configuration, or from a terminal with the Android SDK installed:

```bash
./gradlew :app:assembleDebug
```

The repo ships without a Gradle wrapper binary; Android Studio generates one, or run `gradle wrapper --gradle-version 8.9` once.

## Get an APK without a local setup

Push this folder to a GitHub repository (branch `main`). The included workflow `.github/workflows/build-apk.yml` builds a debug APK on every push; download it from the run's **Artifacts** section under Actions, copy it to the phone and install (allow "unknown sources" when prompted).

## Docs

- [docs/previews/](docs/previews/) — rendered screen mockups (from 1.0.1) and the launcher icon

- [docs/FEATURES.md](docs/FEATURES.md) — what the app does, written for users and product
- [docs/HANDOFF.md](docs/HANDOFF.md) — architecture, data model, gamification rules, and how to extend it

## Version history

| Version | Date | Notes |
|---|---|---|
| 1.1.0 | 2026-10-31 | Member accounts (phone + PIN, member picker, app lock, log out), expressive animated avatar with budget moods and greetings, randomised avatars per member, themed surface colours, reminders with monthly/yearly bills, notification toggles, currency list, edit-profile screen |
| 1.0.1 | 2026-10-31 | Adaptive launcher icon (gradient + house/coin, themed-icon support), Material category icons replacing emoji, GitHub Actions APK build |
| 1.0.0 | 2026-10-31 | Initial build: expenses, budgets, groups & splits, family members, avatar studio, rewards shop, badges, reminders, notifications, insights, CSV export |
