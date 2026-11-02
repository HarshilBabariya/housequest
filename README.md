# HomeQuest

Gamified household expense manager for Android. Log day-to-day spending, split it with family or friends, keep budgets, and earn points that unlock themes, avatar outfits and features.

- Native Android · Kotlin · Jetpack Compose · Material 3
- Local-first: Room database + DataStore, no backend or account required (data never leaves the phone)
- English, Hindi and Gujarati
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

- [docs/previews/](docs/previews/) — rendered screen mockups of the 1.2 design (`v12-*.png`), the avatar set (`avatar-sheet.png`) and the launcher icon

- [docs/HOMEQUEST.md](docs/HOMEQUEST.md) — the complete reference: features, engineering handoff, working agreements and release history

## Version history

| Version | Date | Notes |
|---|---|---|
| 1.7.1 | 2026-11-02 | Credits that subtract from totals (form, voice, capture); stricter capture rules; shares add up with credits; tint-only bottom tabs; calendar icon tile; date chips fit; points and streak below the level on Me |
| 1.7.0 | 2026-11-02 | Kirana khata, milk/maid/services tally, gold and chit funds (DB v7); payment auto-capture from notifications; auto-logging reminders; budget forecast; weekly summary card; PDF export |
| 1.6.0 | 2026-11-02 | Avatar actions, face shapes, beards and 19 new looks (DB v6); labelled XP bar; tappable charts; voice note; Calendar view; Pro plans sheet; More options; editable reminders; custom group emoji; rotating sassy notifications; Glass theme; notched 3D bottom bar; top-bar inset fix |
| 1.5.0 | 2026-11-01 | Insights Week/Month/Year with charts and totals; Home month-pace chart; CSV import + per-month export; Erase all data; date quick chips; one-screen PIN step; logout-after-switch and square-ripple fixes; UPI id hidden |
| 1.4.1 | 2026-11-01 | UPI top-up trial (point packs via upi:// deep link, trust-based confirmation) |
| 1.4.0 | 2026-10-31 | 33 badges with retroactive engine (DB v5), Badges page, animated Rewards; member add/remove re-split choice; inline Paid by / payment mode; sheets no longer close on scroll; midnight rollover fix; flicker fix; icon within safe zone |
| 1.3.2 | 2026-10-31 | Sample-data switch removed; docs consolidated into docs/HOMEQUEST.md |
| 1.3.1 | 2026-10-31 | Fix: add/voice screens crashed (ActivityResult launcher could not find the Activity through the localised context); new launcher icon; equal badge tile heights; male/female option per member (DB v4) with gender-aware avatar; mood chosen in the studio is saved and shown everywhere (Auto = budget-driven); budgets redesigned with a hero ring and tap-to-edit limit sheets |
| 1.3.0 | 2026-10-31 | Custom categories + payment modes (DB v3), redesigned add-transaction form with voice entry, bottom bar with centre +, 10-mood avatar in a circle frame with softer shading, exact-alarm notifications with playful localised copy, time wheel, sample-data switch, Hindi/Gujarati, currencies trimmed to INR/USD/EUR, text ellipsis everywhere; fixes: logout, group crash (duplicate list keys), PIN screen overflow, avatar shaking |
| 1.2.3 | 2026-10-31 | Bottom nav restyled (flat surface, divider, tinted indicator), Home tab pops back to the dashboard so it always responds, inner screens no longer double-count system insets (gap above the nav bar removed), badges in one horizontal scrolling row, static avatar on Profile/Edit profile |
| 1.2.2 | 2026-10-31 | Badge grid (earned first, progress on locked) with animated detail card and how-to steps; text fields: label above, white/near-black box with hairline border |
| 1.2.1 | 2026-10-31 | Avatar v3 (Bitmoji-style face with outlines, big eyes, bold brows), 9 hair styles, 12 outfits, 6 accessories, refreshed catalog prices; screen previews regenerated for the 1.2 design |
| 1.2.0 | 2026-10-31 | Redrawn Bitmoji-style avatar; 3-step onboarding with required mobile and PIN; 4-digit PIN with keypad, inline App-lock PIN setup; design system (cards, tiles, fields), refined light/dark palettes, rebuilt Add-expense form, bottom sheets for groups/members; dashboard tiles (today, vs last month, group balance, top category, who paid, upcoming bills); fixes: Groups crash after profile switch, logout, dark-mode text |
| 1.1.0 | 2026-10-31 | Member accounts (phone + PIN, member picker, app lock, log out), expressive animated avatar with budget moods and greetings, randomised avatars per member, themed surface colours, reminders with monthly/yearly bills, notification toggles, currency list, edit-profile screen |
| 1.0.1 | 2026-10-31 | Adaptive launcher icon (gradient + house/coin, themed-icon support), Material category icons replacing emoji, GitHub Actions APK build |
| 1.0.0 | 2026-10-31 | Initial build: expenses, budgets, groups & splits, family members, avatar studio, rewards shop, badges, reminders, notifications, insights, CSV export |
