# HomeQuest — Engineering handoff

## Stack
| Concern | Choice |
|---|---|
| Language / UI | Kotlin 2.1, Jetpack Compose (BOM 2024.12), Material 3, material-icons-extended |
| Navigation | navigation-compose 2.8 with string routes (`ui/navigation/Routes.kt`) |
| Persistence | Room 2.6 (KSP, schema v3 with migrations) + DataStore Preferences |
| Background | `AlarmManager` exact alarms + `BroadcastReceiver`s for reminders (no WorkManager) |
| Localisation | `res/values`, `values-hi`, `values-gu`; in-app switch via `LocaleHelper` + `CompositionLocalProvider` |
| DI | Manual `AppContainer` held by `HomeQuestApp`; screens obtain view models through `appViewModel { AppContainer -> VM }` |
| Build | AGP 8.7, compileSdk 35, minSdk 26, JDK 17 |

## Build status
The source was written and statically reviewed but **not compiled** in the authoring environment (no Android SDK / Maven access there). Expect a first `assembleDebug` to surface only trivial issues such as a missing import or an API-signature mismatch for a Compose version. The `schemas/` directory for Room is generated on first build.

## CI
`.github/workflows/build-apk.yml` runs `assembleDebug` on Ubuntu with JDK 17 and the runner's preinstalled Android SDK (platform 35 / build-tools 35.0.0 installed via `sdkmanager`; the `setup-android` action is deliberately not used because it tries to install the retired `tools` package) and uploads `app-debug.apk` as an artifact. Trigger: push to `main` or manual dispatch.

## Package layout (`com.homequest.app`)
```
HomeQuestApp          Application: builds AppContainer, creates notification channels, runs bootstrap
AppContainer          wires DB, repositories, notification helpers; bootstrap() seeds profile/owner, closes previous budget month
MainActivity          edge-to-edge host; localised context, theme, confetti, reward toast, notification permission
data/local            Entities.kt, Daos.kt, HomeQuestDatabase.kt
data/repository       Expense, Member, Group, Reminder, Gamification, Settings, Category, SampleData repositories
domain                Models (Category, PaymentMode, Mood, AppLanguage, Relations, ids), Catalogs (rewards, badges — string resource ids), Gamification (points, levels, ledger reason codes, split maths)
notifications         NotificationHelper (localised, randomised copy), BudgetMonitor, ReminderScheduler (AlarmManager), ReminderReceiver + BootReceiver
util                  Dates, Sharing (localised summary, CSV), Pin, Text (titleCased), LocaleHelper, VoiceParser
ui/theme              Palettes (6 themes → light/dark ColorScheme), Theme (animated colour switch)
ui/components         Charts, Celebrations (counters, confetti, toast, ledger reason labels), AvatarView (Canvas avatar), ExpenseRow, Common (category/relation labels), DesignSystem (cards, fields, PickerRow, TimeWheel, PIN)
ui/navigation         Routes, HomeQuestNavHost (custom bottom bar with centre FAB + graph)
ui/screens/*          one package per feature, each with its screen and view model
```

## Data model
- `members` — owner row seeded on first run (`isOwner = 1`); avatar fields stored inline (`skinTone`, `hairId`, `hairColor`, `outfitId`, `accessoryId`), plus `phone` and `pinHash` (`salt:sha256(salt:pin)`, see `util/Pin.kt`; PINs are exactly `Pin.LENGTH` = 4 digits, `Pin.isWeak` rejects repeats and sequences). `MemberEntity.avatar()` converts to `AvatarConfig`; `MemberEntity.hasPin` is derived. New members get a random avatar in `MemberRepository.add()`.
- Active account: `SettingsRepository.activeMemberId` (0 = nobody signed in). `MemberRepository.activeMember` is the flow screens use for "who am I"; `activeMemberId()` falls back to the owner. `MainViewModel.gate()` decides between onboarding, the lock screen (`ui/screens/profile/LockScreen.kt`) and the app; `sessionUnlocked` is in-memory so App lock re-prompts on every cold start.
- `categories` — `id` (built-ins keep the old enum names `GROCERIES`…`OTHER` so no expense rows had to be rewritten; custom ones are `custom_<millis>`), `name` (custom only; built-in names come from `BuiltInCategories.nameRes`), `icon` (key into `CategoryIcons`), `color` (ARGB), `sortOrder`, `builtIn`. `CategoryRepository.ensureDefaults()` seeds the built-ins at bootstrap (`INSERT OR IGNORE`). Deleting a custom category reassigns its expenses to `OTHER` and drops its budget. UI resolves ids with `List<Category>.byId()` / `Map.resolve(id)` and labels with `Category.label()` (composable) or `label(context)`.
- `expenses` — `category` is a category id string, `paymentMode` is `PaymentMode` (CASH/UPI/CARD/BANK, stored as text), `isSample` marks demo rows. `epochDay` (LocalDate.toEpochDay) is the only date used for grouping/queries; `createdAt` orders within a day. `paidByMemberId` cascades on member delete; `groupId` is set null on group delete.
- `expense_splits` — per-member share; present only for group expenses. Deleted with the expense.
- `groups`, `group_members` (junction), `settlements` — balances are computed in `GroupRepository.balances()` from expenses + splits + settlements; `SplitCalculator.settlements()` produces the minimal transfer list.
- `reminders` — `repeat` ONCE/DAILY/WEEKLY/MONTHLY/YEARLY with `weekday`, `dayOfMonth`, `month`, optional `amount` and `category`; `ReminderEntity.nextOccurrence()` (in `ReminderScheduler.kt`) computes the next fire time and clamps day-of-month to the month length.
- `budgets` (per category id; the overall monthly budget lives in DataStore), `profile` (single row: points, lifetime XP, streak, last log day, last budget month checked), `unlocks`, `earned_badges`, `points_ledger` (`reason` is a code from `LedgerReasons`, e.g. `badge:badge_century`, rendered by `reasonLabel()` in the current language).
- `members`, `groups`, `reminders` also carry `isSample`; `SampleDataRepository.load()` inserts demo rows directly through the DAOs (so no points are awarded) and `clear()` deletes by that flag (splits/settlements cascade).

Month/category aggregation for Insights uses `strftime('%Y-%m', epochDay * 86400, 'unixepoch')`.

## Database versions
- v1 → v2 (`MIGRATION_1_2`): adds `members.phone`, `members.pinHash`, `reminders.dayOfMonth/month/amount/category`.
- v2 → v3 (`MIGRATION_2_3`): creates `categories`, adds `expenses.paymentMode` (default `CASH`) and `isSample` to `expenses`, `members`, `groups`, `reminders`. The `groups` table name is quoted with backticks in every `@Query` because `GROUPS` became a SQLite keyword in 3.25.
- Add further migrations to `HomeQuestDatabase.build()`; never bump the version without one.

## Avatar & moods
`ui/components/AvatarView.kt` draws the character on a Canvas using a 100×100 coordinate space (`Pen` helper scales paths). The canvas is clipped to a circle with a themed `background`, so the bust is naturally cropped at the shoulders — never wrap it in another circle. Faces, hair and clothes use radial/vertical gradients for a soft 3D look; the outline is a faint 25% brown. `animated` enables breathing/gentle sway/blink only (the old angry/worried jitter is gone), `waveTrigger` plays a 3-wave arm animation whenever its value changes. `mood` (`domain.Mood`, 10 values) maps to a private `Face` spec (`faceFor`) — brow tilt/raise/asymmetry, eye openness/scale, heart eyes, mouth shape and a floating extra (hearts, sparkles, sweat, tears, question mark, Zzz, steam). `moodFor(progress, hasBudget, streak, daysSinceLastLog, hour, dayOfMonth)` picks the mood in priority order angry → worried → sleepy → sad → confused → love → excited → smart → neutral → happy; the greeting is `Mood.lineRes` prefixed by a time-of-day string.

## Gamification rules
All numeric rules live in `domain/Gamification.kt` (`PointsRules`, `Levels`) and `domain/Catalogs.kt` (`RewardCatalog`, `BadgeCatalog`). `GamificationRepository` is the only writer of points/badges/unlocks and emits `RewardEvent`s on a `SharedFlow`; `MainViewModel` turns those into the toast, confetti and reward notification.

- Streak logic: first log of a day increments the streak if the previous log was yesterday, otherwise resets to 1.
- Level = `floor(sqrt(lifetimeXP / 100)) + 1`; thresholds `(level-1)² · 100 → level² · 100`.
- Month-under-budget award is evaluated once per month in `BudgetMonitor.closePreviousMonthIfNeeded()` on app start, guarded by `profile.lastBudgetCheckMonth`.
- Free catalog items are inserted into `unlocks` at bootstrap, so "unlocked" is the single source of truth for both themes and avatar parts.

## Notifications
- Channels: `reminders` (high), `budget` (high), `rewards` (default).
- Reminders: `ReminderScheduler.schedule()` sets one `AlarmManager` alarm per reminder (`setExactAndAllowWhileIdle`, falling back to `setAndAllowWhileIdle` when exact alarms are not permitted on API 31–32) with a broadcast `PendingIntent` keyed by reminder id. `ReminderReceiver` posts the notification and reschedules (or disables a ONCE reminder); `BootReceiver` calls `ReminderRepository.rescheduleAll()` on `BOOT_COMPLETED`, `TIME_SET` and `TIMEZONE_CHANGED`, and `AppContainer.bootstrap()` does the same on every cold start. Manifest: `SCHEDULE_EXACT_ALARM` (≤32), `USE_EXACT_ALARM` (33+), `RECEIVE_BOOT_COMPLETED`.
- Copy: `NotificationHelper` resolves strings through `LocaleHelper.localized(context, settings.language)` and picks a random title/body from the `notif_*` string arrays, so wording follows the in-app language, not the system one.
- Budget alerts fire from `BudgetMonitor.afterExpenseAdded()` when a total crosses 80% or 100% of a limit.
- `POST_NOTIFICATIONS` is requested once on launch (API 33+). `NotificationHelper.canPost()` guards every post.

## Design system
`ui/components/DesignSystem.kt` holds the shared building blocks: `HqCard` (22dp radius, `surfaceContainerLowest`, hairline border), `SectionCard`, `StatTile`, `PickerRow` (icon tile + label/value + chevron, the row style of the add-transaction form), `HqTextField` (label above the box, soft filled container, border only on focus/error, sentence capitalisation by default), `PrimaryButton`, `TimeWheel` (three snap-scrolling `LazyColumn` wheels with a highlighted centre row and fade overlays; the centre item is derived from `layoutInfo`), and the PIN components (`PinDots`, `PinPad`, `PinEntry`, `PinSetupSheet`). `OptionSheet`, `CategoryGrid`, `MemberChip` and `MemberFlow` live in `AddExpenseScreen.kt` and are reused by the group, family, reminder and category sheets. All `Text` that can grow is given `maxLines` + `TextOverflow.Ellipsis`. New screens should compose these rather than raw Material `Card`/`OutlinedTextField`.

## Session & navigation
`MainActivity` switches between `OnboardingScreen`, `LockScreen` and `HomeQuestNavHost` via `MainViewModel.gate()`. The `NavHostController` is created inside `HomeQuestNavHost`, so each unlock gets a fresh controller — reusing one across the lock screen caused the "ViewModelStore should be set before setGraph" crash seen in 1.1. Sign-out goes through `MainViewModel.signOut()` (clears `sessionUnlocked` and `activeMemberId`).

`LockScreen` only preselects a member when `preselectedId` matches one (app-lock re-prompt); after sign-out `activeMemberId` is 0 so the picker is always shown — auto-selecting a lone PIN-less profile was what made Log out appear to do nothing in 1.2.

Bottom tabs (`HqBottomBar`): Home, Expenses, Groups, Me plus a centre FAB that opens `Routes.addExpense()`; Rewards is a normal pushed route reachable from the Home pill and Me. The Home tab calls `popBackStack(Routes.DASHBOARD, inclusive = false)` instead of `navigate` — Dashboard is the graph's start destination and re-navigating to it with `restoreState` could leave the tab unresponsive after visiting other tabs. The other tabs `navigate` with `popUpTo(Routes.DASHBOARD) { saveState = true }`. Only the outer `Scaffold` in `HomeQuestNavHost` handles system insets; every screen-level `Scaffold` passes `contentWindowInsets = WindowInsets(0, 0, 0, 0)`, otherwise the navigation-bar inset is applied twice and a blank band appears above the bottom bar.

## Launcher icon
`res/mipmap-anydpi-v26/ic_launcher.xml` (and `_round`) is an adaptive icon: `drawable/ic_launcher_background.xml` (gradient + soft circles), `drawable/ic_launcher_foreground.xml` (house, door, window, star coin, all inside the 66dp safe zone) and `drawable/ic_launcher_monochrome.xml` for themed icons. `drawable/ic_notification.xml` is the matching white-house silhouette used for all notifications. Source SVG of the design is reproduced in the foreground drawable paths.

## Theming
`Palettes.forId(themeId)` returns a `Palette`; `Theme.kt` builds light/dark `ColorScheme`s and animates primary/secondary/tertiary/background/surface/containers when the theme id changes so unlocking a theme visibly morphs the UI.

## Localisation
All user-facing text is in `res/values/strings.xml` (English, the source of truth), `values-hi` and `values-gu`; every key exists in all three and placeholder sets are identical. Enums/catalogs carry `@StringRes` ids (`Mood`, `PaymentMode`, `RepeatRule`, `RewardKind`, `Relations`, `RewardItem`, `Badge`, `LevelInfo`). The language is a DataStore setting; `MainActivity` wraps the whole tree in `CompositionLocalProvider(LocalContext provides LocaleHelper.localized(...), LocalConfiguration ...)` and sets `Locale.setDefault`, so `stringResource`, `java.time` formatters and the speech recogniser all follow it without an Activity restart. Relation values are stored as lowercase keys (`me`, `partner`…) and resolved with `relationLabel()`; rows written by older versions (`Me`, `Partner`) still match case-insensitively.

## Voice entry
`AddExpenseScreen` launches `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` (no RECORD_AUDIO permission needed; the system recogniser handles it) in the app language and feeds the first result to `VoiceParser.parse(text, customCategories)`, which extracts the first number as the amount, matches payment-mode and category keyword lists (English plus common Hindi/Gujarati words) and custom category names, strips filler words and title-cases the rest. The result pre-fills the form for review; nothing is saved automatically.

## Adding things
- **New built-in category**: add a constant and `Seed` to `BuiltInCategories` (plus `cat_*` strings in all three languages); `ensureDefaults()` inserts it on next start. Custom categories need no code.
- **New reward**: add a `RewardItem` to `RewardCatalog.items` with a constant in `Models.kt`. Outfits/hair/accessories also need a drawing branch in `AvatarView.kt`; themes need a `Palette`.
- **New badge**: add to `BadgeCatalog.badges` (with `target`, `unit` and `howTo` steps), call `grantBadge(id)` from the relevant `GamificationRepository.record…()` method, and map its live progress in `RewardsViewModel` (the `when (b.id)` block that builds `BadgeStatus`).
- **New screen**: add a route in `Routes`, a `composable` in `HomeQuestNavHost`, and a `ui/screens/<feature>` package.

## Known limits / next steps
- Local-only; no sync, auth or multi-device sharing (group sharing is via text summary). Giving a member access from another phone needs a backend (e.g. Firebase phone auth + Firestore) — a separate project.
- Expenses store a single currency symbol globally; no FX.
- Voice parsing is keyword based; amounts spoken as words ("two hundred") are not recognised.
- No unit tests yet; `SplitCalculator`, `Levels` and `GamificationRepository` are the first candidates.
