# HomeQuest — Engineering handoff

## Stack
| Concern | Choice |
|---|---|
| Language / UI | Kotlin 2.1, Jetpack Compose (BOM 2024.12), Material 3, material-icons-extended |
| Navigation | navigation-compose 2.8 with string routes (`ui/navigation/Routes.kt`) |
| Persistence | Room 2.6 (KSP) + DataStore Preferences |
| Background | WorkManager 2.10 for reminders |
| DI | Manual `AppContainer` held by `HomeQuestApp`; screens obtain view models through `appViewModel { AppContainer -> VM }` |
| Build | AGP 8.7, compileSdk 35, minSdk 26, JDK 17 |

## Build status
The source was written and statically reviewed but **not compiled** in the authoring environment (no Android SDK / Maven access there). Expect a first `assembleDebug` to surface only trivial issues such as a missing import or an API-signature mismatch for a Compose version. The `schemas/` directory for Room is generated on first build.

## CI
`.github/workflows/build-apk.yml` runs `assembleDebug` on Ubuntu with JDK 17 + Android SDK and uploads `app-debug.apk` as an artifact. Trigger: push to `main` or manual dispatch.

## Package layout (`com.homequest.app`)
```
HomeQuestApp          Application: builds AppContainer, creates notification channels, runs bootstrap
AppContainer          wires DB, repositories, notification helpers; bootstrap() seeds profile/owner, closes previous budget month
MainActivity          edge-to-edge host; theme, confetti, reward toast, welcome dialog, notification permission
data/local            Entities.kt, Daos.kt, HomeQuestDatabase.kt
data/repository       Expense, Member, Group, Reminder, Gamification, Settings repositories
domain                Models (enums, AvatarConfig, ids), Catalogs (rewards, badges), Gamification (points, levels, split maths)
notifications         NotificationHelper, BudgetMonitor, ReminderScheduler, ReminderWorker
util                  Dates (epoch-day helpers, money formatting), Sharing (share text, CSV export via FileProvider)
ui/theme              Palettes (6 themes → light/dark ColorScheme), Theme (animated colour switch)
ui/components         Charts, Celebrations (counters, confetti, toast), AvatarView (Canvas avatar), ExpenseRow, WelcomeDialog, Common
ui/navigation         Routes, HomeQuestNavHost (bottom bar + graph)
ui/screens/*          one package per feature, each with its screen and view model
```

## Data model
- `members` — owner row seeded on first run (`isOwner = 1`); avatar fields stored inline (`skinTone`, `hairId`, `hairColor`, `outfitId`, `accessoryId`). `MemberEntity.avatar()` converts to `AvatarConfig`.
- `expenses` — `epochDay` (LocalDate.toEpochDay) is the only date used for grouping/queries; `createdAt` orders within a day. `paidByMemberId` cascades on member delete; `groupId` is set null on group delete.
- `expense_splits` — per-member share; present only for group expenses. Deleted with the expense.
- `groups`, `group_members` (junction), `settlements` — balances are computed in `GroupRepository.balances()` from expenses + splits + settlements; `SplitCalculator.settlements()` produces the minimal transfer list.
- `reminders`, `budgets` (per category; the overall monthly budget lives in DataStore), `profile` (single row: points, lifetime XP, streak, last log day, last budget month checked), `unlocks`, `earned_badges`, `points_ledger`.

Month/category aggregation for Insights uses `strftime('%Y-%m', epochDay * 86400, 'unixepoch')`.

## Gamification rules
All numeric rules live in `domain/Gamification.kt` (`PointsRules`, `Levels`) and `domain/Catalogs.kt` (`RewardCatalog`, `BadgeCatalog`). `GamificationRepository` is the only writer of points/badges/unlocks and emits `RewardEvent`s on a `SharedFlow`; `MainViewModel` turns those into the toast, confetti and reward notification.

- Streak logic: first log of a day increments the streak if the previous log was yesterday, otherwise resets to 1.
- Level = `floor(sqrt(lifetimeXP / 100)) + 1`; thresholds `(level-1)² · 100 → level² · 100`.
- Month-under-budget award is evaluated once per month in `BudgetMonitor.closePreviousMonthIfNeeded()` on app start, guarded by `profile.lastBudgetCheckMonth`.
- Free catalog items are inserted into `unlocks` at bootstrap, so "unlocked" is the single source of truth for both themes and avatar parts.

## Notifications
- Channels: `reminders` (default), `budget` (high), `rewards` (low).
- Reminders: `ReminderScheduler` enqueues a unique `OneTimeWorkRequest` per reminder with the delay to the next occurrence; `ReminderWorker` posts the notification and re-enqueues (daily/weekly) or disables the reminder (once). No exact-alarm permission is required; delivery may drift by minutes under Doze.
- Budget alerts fire from `BudgetMonitor.afterExpenseAdded()` when a total crosses 80% or 100% of a limit.
- `POST_NOTIFICATIONS` is requested once on launch (API 33+). `NotificationHelper.canPost()` guards every post.

## Launcher icon
`res/mipmap-anydpi-v26/ic_launcher.xml` (and `_round`) is an adaptive icon: `drawable/ic_launcher_background.xml` (gradient + soft circles), `drawable/ic_launcher_foreground.xml` (house, door, window, star coin, all inside the 66dp safe zone) and `drawable/ic_launcher_monochrome.xml` for themed icons. `drawable/ic_notification.xml` is the matching white-house silhouette used for all notifications. Source SVG of the design is reproduced in the foreground drawable paths.

## Theming
`Palettes.forId(themeId)` returns a `Palette`; `Theme.kt` builds light/dark `ColorScheme`s and animates primary/secondary/tertiary/background/surface/containers when the theme id changes so unlocking a theme visibly morphs the UI.

## Adding things
- **New category**: add to `ExpenseCategory` with a Material `ImageVector` and colour; `CategoryIcon`/`CategoryChip` in `ui/components/Common.kt` render it everywhere.
- **New reward**: add a `RewardItem` to `RewardCatalog.items` with a constant in `Models.kt`. Outfits/hair/accessories also need a drawing branch in `AvatarView.kt`; themes need a `Palette`.
- **New badge**: add to `BadgeCatalog.badges` and call `grantBadge(id)` from the relevant `GamificationRepository.record…()` method.
- **New screen**: add a route in `Routes`, a `composable` in `HomeQuestNavHost`, and a `ui/screens/<feature>` package.

## Known limits / next steps
- Local-only; no sync, auth or multi-device sharing (group sharing is via text summary).
- Expenses store a single currency symbol globally; no FX.
- Reminder times are device-local; a timezone change reschedules on next app start only when the reminder is edited.
- No unit tests yet; `SplitCalculator`, `Levels` and `GamificationRepository` are the first candidates.
