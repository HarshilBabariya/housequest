# HomeQuest — the complete reference

Single source of truth for the product, the engineering handoff and the working context of the project. Updated with every release zip.

## 1. What HomeQuest is
A gamified household-expense manager for Android, built for Harshil (GitHub `HarshilBabariya/housequest`). Members of one household log spending on one phone, split it in groups, keep budgets, and earn points that unlock themes, avatar looks and features. Everything is local-first (Room + DataStore); the APK is produced by the repo's GitHub Actions workflow — the project has never been compiled on the authoring side, so every release is statically reviewed and then verified through the Actions build and on-device testing.

## 2. Working agreements (project memory)
- Deliverable is an installable build, not code: each release is handed over as a zip to push to `main`; the workflow in `.github/workflows/build-apk.yml` uploads `app-debug.apk`.
- Follow the coding standard and structure already in the project; no dead code, unused files or duplicated code; minimal comments.
- Every handover updates this document and the README version history in the same pass.
- Fixes are verified in a fresh review pass (not the session that wrote them) before a zip is handed over, and the user's screenshots/bug reports drive the next iteration.
- Known boundary: cross-device access for a member with the same mobile number needs a sync backend (e.g. Firebase phone auth + Firestore); it is intentionally out of scope and the app says so on the Family screen.
- Avatars are 2D Compose canvas art; the 3D-clay reference look was approximated (soft shading, circle frame), not replicated.
- Design references the user shared: a dark "cash-flow" style home with a centre **+** bottom bar, list-style add-transaction form with payment modes, playful mood-driven avatar.

## 3. Features
### Look & feel
- Adaptive launcher icon: coral-to-pink gradient, a large white house with pink door and windows filling the safe zone, gold star coin on the roof; round, squircle and Android 13+ themed (monochrome) variants are all supported.
- Material 3 throughout; each of the six themes defines its own surface, card and chip colours so the whole UI (not just the accent) changes when you apply one.
- Bottom navigation: four tabs (Home, Expenses, Groups, Me) on a rounded floating bar with a large centre **+** button that opens the add-transaction form from anywhere.
- Every text that can overflow is clipped with an ellipsis on one line; nothing wraps into a second line unexpectedly.
- Three languages — English, हिन्दी (Hindi) and ગુજરાતી (Gujarati) — switchable from onboarding or Me → Language; dates, badge names, reward names, mood lines and notifications all follow the chosen language.

### First launch & accounts
- A three-step onboarding (You → Money → PIN) asks for your language, name and mobile number (required, 10+ digits), currency and optional budget, then lets you create a 4-digit PIN or skip it. You become the household owner and start at Level 1 (Rookie). A daily "Log today's expenses" reminder at 20:30 is created for you.
- Every family member is an account: name, relation, mobile number (required), avatar and an optional 4-digit PIN. Weak PINs (1111, 1234…) are rejected and the PIN is confirmed twice.
- **Local-only**: all data lives in this phone's database. A member installing the app on another phone starts with an empty ledger — the number only identifies the profile on this device. Cloud sync is not part of this release (the Family screen says so).
- Member picker: after Log out you always land on the picker, even with a single profile; members with a PIN get a keypad. The PIN screen scrolls on small phones so the keypad is never cut off.
- App lock (Me → Security): switching it on creates a PIN on the spot if you don't have one, then asks for it every time the app opens.
- Log out / switch from the Me tab returns to the picker. Expenses, budgets and points are shared by the whole household — only avatar and greeting are per member.

### Your avatar
- Drawn as a head-and-shoulders bust inside a circular frame everywhere it appears (header, lists, chips, picker), with soft radial shading on the face, gradient hair and clothing, blush and nose highlight.
- Male or female (chosen at onboarding, in Family members or Edit profile, and in the studio): female avatars get lashes and fuller blush, male avatars broader shoulders and thicker brows; new members get a gender-appropriate random hairstyle.
- The mood you pick in the studio is saved and shown on Home, Me, lists and chips; choose Auto to let budget/streak decide.
- 9 hair styles (short, buzz, long, wavy, bob, ponytail, curly, top bun, braids, mohawk), 7 hair colours, 6 skin tones.
- 12 outfits: casual tee, shirt, hoodie, denim jacket, sweater, blazer, kurta, sherwani, saree, dress, spacesuit, hero suit.
- 6 accessories: bindi, earrings, glasses, cap, headphones, crown.
- Animated on Home and in the studio only: breathes, blinks at random, sways gently (no shaking) and waves when Home opens. Static everywhere else.
- **10 moods**, each with its own brows, eyes, mouth and a floating extra: Happy, In love (heart eyes + hearts), Excited (sparkles), Smart (raised brow, smirk), Chill, Confused (question mark), Sleepy (Zzz), Sad (tears), Worried (sweat drop), Angry (steam). Home picks the mood from budget progress, streak, days since your last log, time of day and day of month; the studio lets you preview all ten.
- A speech bubble greets you by time of day with a mood-specific line.
- New family members get a randomised look; those parts are free so households look varied from day one.

### Home dashboard
- Header: your animated avatar (tap for Me), name, a level · points · streak pill (tap for Rewards), a microphone button for voice entry, and the XP bar.
- Cash-flow card: budget ring, spent this month, amount left / over.
- Stat tiles: Today (tap to add), vs last month, Group balance (tap to open Groups), Top category.
- 7-day bar chart, category donut, "Who paid this month", "Upcoming bills" (next three) and recent transactions with "See all".
- "Insights" shortcut appears once the Advanced Insights feature is unlocked.

### Expenses & the add-transaction form
- The list opens with This month / vs last month tiles, a search field and category chips; entries are grouped by day with subtotals and show category · payer · payment mode.
- Add transaction (centre **+** or Today tile): a "Say it or type it" voice banner, an amount hero, a title field, then a list of picker rows — Date (with date picker), Category, Payment mode, Paid by, Split with a group — each opening a bottom sheet; a note under "Other details". Group splits (equal or custom) appear inline when a group is chosen.
- **Payment modes**: Cash, UPI, Card, Bank transfer — stored per expense and included in the CSV export.
- **Voice entry**: tap the banner or the Home microphone, say e.g. "250 groceries by UPI" or "paid 120 for pani puri cash"; the amount, category (built-in keywords in English/Hindi/Gujarati words such as sabzi, kiraya, dawa, plus your custom category names), payment mode and title are filled in for you to review and save. Uses the phone's speech recogniser in the app language.
- **Custom categories** (Expenses → tune icon, or Me → Categories): add your own with a name, one of 26 icons and 15 colours; built-in categories can be recoloured/re-iconed; deleting a custom category moves its expenses to Other.
- Titles, notes, names and reminder titles are saved with a capital first letter; text fields default to sentence capitalisation.
- Tap any expense to edit or delete it. Every new expense earns points (see Rewards).

### Budgets & alerts
- Budgets screen: a hero card with the budget ring, spent / left this month and a "Set monthly budget" button; each category is a card with icon, spent of limit and a coloured progress bar — tap it to set or remove a limit in a sheet with quick-amount chips.
- Push notifications when a budget crosses 80% and 100% (can be switched off in Me → Notifications).
- Finishing a month under the overall budget earns +100 points and the Budget Boss badge.

### Groups & splitting
- Create groups (home, trip, party…) from your family members with an emoji.
- Add an expense to a group and split it equally among chosen members or with custom amounts; the form shows live per-person shares and whether custom amounts add up.
- Group page shows total shared, who owes whom, a minimal "settle up" plan, and all shared expenses. "Mark paid" records a settlement and earns points.
- Share button sends a localised text summary of balances to WhatsApp, SMS, email, etc.

### Family members
- Add partner, kids, parents, roommates or friends with a relation label; each has their own avatar (tap to open the studio for them).
- Removing a member cleans up their expenses and splits.

### Avatar studio
- Skin tone (6) and hair colour (7) swatches, always free.
- Hair (9), outfits (12) and accessories (6): short/buzz/long/curly hair, tee and shirt are free; the rest unlock with points directly from the studio.
- Live preview on your own avatar, a "Say hi" wave and a chip for each of the ten moods.

### Rewards & gamification
**Earning points**
- +10 per expense logged, +5 for the first log of the day, +2 per streak day (max +20)
- +30 create a group, +15 settle a balance, +20 add a family member
- +100 finish a month under budget
- Bonus points for every badge earned

**Levels** — XP-based (Rookie → Saver → Planner → Tracker → Strategist → Accountant → Treasurer → Guardian → Master → Legend). Level-ups trigger confetti, an in-app toast and a notification.

**Badges (11)** — one horizontally scrolling row, earned first then locked ones with a progress bar. Tap any badge for an animated detail card with a numbered "How to earn it" list.

**Shop** — spend points on 6 colour themes, avatar outfits, hair styles and accessories, and two features (Advanced Insights, CSV export). Points history is listed at the bottom of Rewards. Rewards opens from the Home pill or Me → Rewards.

### Reminders & bills
- Repeat Once / Daily / Weekly (weekday) / Monthly (day of month) / Yearly (month + day); quick presets for rent, EMI, electricity, insurance, school fees and a daily nudge.
- Time is chosen on a scrolling **time wheel** (hour · minute · AM/PM) with the selected row highlighted — no more stock clock dialog.
- Optional amount and category; cards show the schedule, "Due today / tomorrow / in N days" and the amount, sorted by next due date.
- Notifications are delivered by exact alarms, so they arrive on time even when the app is closed or the phone is idle, and are re-registered after a reboot or time change.
- Notification copy is playful and varies: random titles such as "Your wallet called 📞" or "Streak check 🔥", bill reminders with the amount, budget warnings like "Whoa there, big spender 😅", badge and level-up cheers — all localised.

### Me tab & settings
- Profile header with avatar, level, points, streak; Edit profile (name, mobile, role, PIN) and Avatar studio.
- Household: Rewards, Family members, Categories, Budgets, Reminders & bills, Insights, CSV export (when unlocked).
- Security: App lock. Language: English / Hindi / Gujarati. Appearance: System / Light / Dark. Currency: ₹ INR, $ USD, € EUR.
- Notifications: three independent toggles.
- Account: Log out / switch. All data stays on the device and is included in Android backup.

## 4. Engineering handoff
### Stack
| Concern | Choice |
|---|---|
| Language / UI | Kotlin 2.1, Jetpack Compose (BOM 2024.12), Material 3, material-icons-extended |
| Navigation | navigation-compose 2.8 with string routes (`ui/navigation/Routes.kt`) |
| Persistence | Room 2.6 (KSP, schema v3 with migrations) + DataStore Preferences |
| Background | `AlarmManager` exact alarms + `BroadcastReceiver`s for reminders (no WorkManager) |
| Localisation | `res/values`, `values-hi`, `values-gu`; in-app switch via `LocaleHelper` + `CompositionLocalProvider` |
| DI | Manual `AppContainer` held by `HomeQuestApp`; screens obtain view models through `appViewModel { AppContainer -> VM }` |
| Build | AGP 8.7, compileSdk 35, minSdk 26, JDK 17 |

### Build status
The source was written and statically reviewed but **not compiled** in the authoring environment (no Android SDK / Maven access there). Expect a first `assembleDebug` to surface only trivial issues such as a missing import or an API-signature mismatch for a Compose version. The `schemas/` directory for Room is generated on first build.

### CI
`.github/workflows/build-apk.yml` runs `assembleDebug` on Ubuntu with JDK 17 and the runner's preinstalled Android SDK (platform 35 / build-tools 35.0.0 installed via `sdkmanager`; the `setup-android` action is deliberately not used because it tries to install the retired `tools` package) and uploads `app-debug.apk` as an artifact. Trigger: push to `main` or manual dispatch.

### Package layout (`com.homequest.app`)
```
HomeQuestApp          Application: builds AppContainer, creates notification channels, runs bootstrap
AppContainer          wires DB, repositories, notification helpers; bootstrap() seeds profile/owner, closes previous budget month
MainActivity          edge-to-edge host; localised context, theme, confetti, reward toast, notification permission
data/local            Entities.kt, Daos.kt, HomeQuestDatabase.kt
data/repository       Expense, Member, Group, Reminder, Gamification, Settings, Category repositories
domain                Models (Category, PaymentMode, Mood, AppLanguage, Relations, ids), Catalogs (rewards, badges — string resource ids), Gamification (points, levels, ledger reason codes, split maths)
notifications         NotificationHelper (localised, randomised copy), BudgetMonitor, ReminderScheduler (AlarmManager), ReminderReceiver + BootReceiver
util                  Dates, Sharing (localised summary, CSV), Pin, Text (titleCased), LocaleHelper, VoiceParser
ui/theme              Palettes (6 themes → light/dark ColorScheme), Theme (animated colour switch)
ui/components         Charts, Celebrations (counters, confetti, toast, ledger reason labels), AvatarView (Canvas avatar), ExpenseRow, Common (category/relation labels), DesignSystem (cards, fields, PickerRow, TimeWheel, PIN)
ui/navigation         Routes, HomeQuestNavHost (custom bottom bar with centre FAB + graph)
ui/screens/*          one package per feature, each with its screen and view model
```

### Data model
- `members` — owner row seeded on first run (`isOwner = 1`); avatar fields stored inline (`skinTone`, `hairId`, `hairColor`, `outfitId`, `accessoryId`), plus `phone` and `pinHash` (`salt:sha256(salt:pin)`, see `util/Pin.kt`; PINs are exactly `Pin.LENGTH` = 4 digits, `Pin.isWeak` rejects repeats and sequences). `MemberEntity.avatar()` converts to `AvatarConfig`; `MemberEntity.hasPin` is derived. New members get a random avatar in `MemberRepository.add()`.
- Active account: `SettingsRepository.activeMemberId` (0 = nobody signed in). `MemberRepository.activeMember` is the flow screens use for "who am I"; `activeMemberId()` falls back to the owner. `MainViewModel.gate()` decides between onboarding, the lock screen (`ui/screens/profile/LockScreen.kt`) and the app; `sessionUnlocked` is in-memory so App lock re-prompts on every cold start.
- `categories` — `id` (built-ins keep the old enum names `GROCERIES`…`OTHER` so no expense rows had to be rewritten; custom ones are `custom_<millis>`), `name` (custom only; built-in names come from `BuiltInCategories.nameRes`), `icon` (key into `CategoryIcons`), `color` (ARGB), `sortOrder`, `builtIn`. `CategoryRepository.ensureDefaults()` seeds the built-ins at bootstrap (`INSERT OR IGNORE`). Deleting a custom category reassigns its expenses to `OTHER` and drops its budget. UI resolves ids with `List<Category>.byId()` / `Map.resolve(id)` and labels with `Category.label()` (composable) or `label(context)`.
- `expenses` — `category` is a category id string, `paymentMode` is `PaymentMode` (CASH/UPI/CARD/BANK, stored as text). `epochDay` (LocalDate.toEpochDay) is the only date used for grouping/queries; `createdAt` orders within a day. `paidByMemberId` cascades on member delete; `groupId` is set null on group delete.
- `expense_splits` — per-member share; present only for group expenses. Deleted with the expense.
- `groups`, `group_members` (junction), `settlements` — balances are computed in `GroupRepository.balances()` from expenses + splits + settlements; `SplitCalculator.settlements()` produces the minimal transfer list.
- `reminders` — `repeat` ONCE/DAILY/WEEKLY/MONTHLY/YEARLY with `weekday`, `dayOfMonth`, `month`, optional `amount` and `category`; `ReminderEntity.nextOccurrence()` (in `ReminderScheduler.kt`) computes the next fire time and clamps day-of-month to the month length.
- `budgets` (per category id; the overall monthly budget lives in DataStore), `profile` (single row: points, lifetime XP, streak, last log day, last budget month checked), `unlocks`, `earned_badges`, `points_ledger` (`reason` is a code from `LedgerReasons`, e.g. `badge:badge_century`, rendered by `reasonLabel()` in the current language).
- `members`, `expenses`, `groups` and `reminders` still carry an `isSample` column (always 0). It was used by the sample-data switch removed in 1.3.2; it stays because SQLite on minSdk 26 cannot `DROP COLUMN` and recreating four tables with foreign keys is not worth the migration risk. Nothing reads or writes it.

Month/category aggregation for Insights uses `strftime('%Y-%m', epochDay * 86400, 'unixepoch')`.

### Database versions
- v1 → v2 (`MIGRATION_1_2`): adds `members.phone`, `members.pinHash`, `reminders.dayOfMonth/month/amount/category`.
- v2 → v3 (`MIGRATION_2_3`): creates `categories`, adds `expenses.paymentMode` (default `CASH`) and `isSample` to `expenses`, `members`, `groups`, `reminders` (unused since 1.3.2, see above). The `groups` table name is quoted with backticks in every `@Query` because `GROUPS` became a SQLite keyword in 3.25.
- v3 → v4 (`MIGRATION_3_4`): adds `members.gender` (MALE/FEMALE, default MALE) and `members.mood` (empty = automatic).
- Add further migrations to `HomeQuestDatabase.build()`; never bump the version without one.

### Avatar & moods
`ui/components/AvatarView.kt` draws the character on a Canvas using a 100×100 coordinate space (`Pen` helper scales paths). The canvas is clipped to a circle with a themed `background`, so the bust is naturally cropped at the shoulders — never wrap it in another circle. Faces, hair and clothes use radial/vertical gradients for a soft 3D look; the outline is a faint 25% brown. `animated` enables breathing/gentle sway/blink only (the old angry/worried jitter is gone), `waveTrigger` plays a 3-wave arm animation whenever its value changes. `AvatarConfig.gender` (from `members.gender`) widens the shoulders and drops the lashes for MALE, and `AvatarConfig.mood` is the mood saved from the studio (`members.mood`, null = automatic) — `AvatarView` defaults its `mood` parameter to it so every call site shows the saved mood, and the dashboard only computes a mood when none is saved. `mood` (`domain.Mood`, 10 values) maps to a private `Face` spec (`faceFor`) — brow tilt/raise/asymmetry, eye openness/scale, heart eyes, mouth shape and a floating extra (hearts, sparkles, sweat, tears, question mark, Zzz, steam). `moodFor(progress, hasBudget, streak, daysSinceLastLog, hour, dayOfMonth)` picks the mood in priority order angry → worried → sleepy → sad → confused → love → excited → smart → neutral → happy; the greeting is `Mood.lineRes` prefixed by a time-of-day string.

### Gamification rules
All numeric rules live in `domain/Gamification.kt` (`PointsRules`, `Levels`) and `domain/Catalogs.kt` (`RewardCatalog`, `BadgeCatalog`). `GamificationRepository` is the only writer of points/badges/unlocks and emits `RewardEvent`s on a `SharedFlow`; `MainViewModel` turns those into the toast, confetti and reward notification.

- Streak logic: first log of a day increments the streak if the previous log was yesterday, otherwise resets to 1.
- Level = `floor(sqrt(lifetimeXP / 100)) + 1`; thresholds `(level-1)² · 100 → level² · 100`.
- Month-under-budget award is evaluated once per month in `BudgetMonitor.closePreviousMonthIfNeeded()` on app start, guarded by `profile.lastBudgetCheckMonth`.
- Free catalog items are inserted into `unlocks` at bootstrap, so "unlocked" is the single source of truth for both themes and avatar parts.

### Notifications
- Channels: `reminders` (high), `budget` (high), `rewards` (default).
- Reminders: `ReminderScheduler.schedule()` sets one `AlarmManager` alarm per reminder (`setExactAndAllowWhileIdle`, falling back to `setAndAllowWhileIdle` when exact alarms are not permitted on API 31–32) with a broadcast `PendingIntent` keyed by reminder id. `ReminderReceiver` posts the notification and reschedules (or disables a ONCE reminder); `BootReceiver` calls `ReminderRepository.rescheduleAll()` on `BOOT_COMPLETED`, `TIME_SET` and `TIMEZONE_CHANGED`, and `AppContainer.bootstrap()` does the same on every cold start. Manifest: `SCHEDULE_EXACT_ALARM` (≤32), `USE_EXACT_ALARM` (33+), `RECEIVE_BOOT_COMPLETED`.
- Copy: `NotificationHelper` resolves strings through `LocaleHelper.localized(context, settings.language)` and picks a random title/body from the `notif_*` string arrays, so wording follows the in-app language, not the system one.
- Budget alerts fire from `BudgetMonitor.afterExpenseAdded()` when a total crosses 80% or 100% of a limit.
- `POST_NOTIFICATIONS` is requested once on launch (API 33+). `NotificationHelper.canPost()` guards every post.

### Design system
`ui/components/DesignSystem.kt` holds the shared building blocks: `HqCard` (22dp radius, `surfaceContainerLowest`, hairline border), `SectionCard`, `StatTile`, `PickerRow` (icon tile + label/value + chevron, the row style of the add-transaction form), `HqTextField` (label above the box, soft filled container, border only on focus/error, sentence capitalisation by default), `PrimaryButton`, `TimeWheel` (three snap-scrolling `LazyColumn` wheels with a highlighted centre row and fade overlays; the centre item is derived from `layoutInfo`), and the PIN components (`PinDots`, `PinPad`, `PinEntry`, `PinSetupSheet`). `OptionSheet`, `CategoryGrid`, `MemberChip` and `MemberFlow` live in `AddExpenseScreen.kt` and are reused by the group, family, reminder and category sheets. All `Text` that can grow is given `maxLines` + `TextOverflow.Ellipsis`. New screens should compose these rather than raw Material `Card`/`OutlinedTextField`.

### Session & navigation
`MainActivity` switches between `OnboardingScreen`, `LockScreen` and `HomeQuestNavHost` via `MainViewModel.gate()`. The `NavHostController` is created inside `HomeQuestNavHost`, so each unlock gets a fresh controller — reusing one across the lock screen caused the "ViewModelStore should be set before setGraph" crash seen in 1.1. Sign-out goes through `MainViewModel.signOut()` (clears `sessionUnlocked` and `activeMemberId`).

`LockScreen` only preselects a member when `preselectedId` matches one (app-lock re-prompt); after sign-out `activeMemberId` is 0 so the picker is always shown — auto-selecting a lone PIN-less profile was what made Log out appear to do nothing in 1.2.

Bottom tabs (`HqBottomBar`): Home, Expenses, Groups, Me plus a centre FAB that opens `Routes.addExpense()`; Rewards is a normal pushed route reachable from the Home pill and Me. The Home tab calls `popBackStack(Routes.DASHBOARD, inclusive = false)` instead of `navigate` — Dashboard is the graph's start destination and re-navigating to it with `restoreState` could leave the tab unresponsive after visiting other tabs. The other tabs `navigate` with `popUpTo(Routes.DASHBOARD) { saveState = true }`. Only the outer `Scaffold` in `HomeQuestNavHost` handles system insets; every screen-level `Scaffold` passes `contentWindowInsets = WindowInsets(0, 0, 0, 0)`, otherwise the navigation-bar inset is applied twice and a blank band appears above the bottom bar.

### Launcher icon
`res/mipmap-anydpi-v26/ic_launcher.xml` (and `_round`) is an adaptive icon: `drawable/ic_launcher_background.xml` (gradient + soft circles), `drawable/ic_launcher_foreground.xml` (house, door, window, star coin, all inside the 66dp safe zone) and `drawable/ic_launcher_monochrome.xml` for themed icons. `drawable/ic_notification.xml` is the matching white-house silhouette used for all notifications. Source SVG of the design is reproduced in the foreground drawable paths.

### Theming
`Palettes.forId(themeId)` returns a `Palette`; `Theme.kt` builds light/dark `ColorScheme`s and animates primary/secondary/tertiary/background/surface/containers when the theme id changes so unlocking a theme visibly morphs the UI.

### Localisation
All user-facing text is in `res/values/strings.xml` (English, the source of truth), `values-hi` and `values-gu`; every key exists in all three and placeholder sets are identical. Enums/catalogs carry `@StringRes` ids (`Mood`, `PaymentMode`, `RepeatRule`, `RewardKind`, `Relations`, `RewardItem`, `Badge`, `LevelInfo`). The language is a DataStore setting; `MainActivity` wraps the whole tree in `CompositionLocalProvider(LocalContext provides LocaleHelper.localized(...), LocalConfiguration ...)`. `LocaleHelper.localized` returns a `ContextWrapper` around the Activity whose `getResources()` is the localised one — it must stay a wrapper of the Activity, because `rememberLauncherForActivityResult` finds its `ActivityResultRegistryOwner` by walking `LocalContext` up to the Activity (a bare `createConfigurationContext` result crashed the add-expense and voice screens in 1.3.0) and sets `Locale.setDefault`, so `stringResource`, `java.time` formatters and the speech recogniser all follow it without an Activity restart. Relation values are stored as lowercase keys (`me`, `partner`…) and resolved with `relationLabel()`; rows written by older versions (`Me`, `Partner`) still match case-insensitively.

### Voice entry
`AddExpenseScreen` launches `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` (no RECORD_AUDIO permission needed; the system recogniser handles it) in the app language and feeds the first result to `VoiceParser.parse(text, customCategories)`, which extracts the first number as the amount, matches payment-mode and category keyword lists (English plus common Hindi/Gujarati words) and custom category names, strips filler words and title-cases the rest. The result pre-fills the form for review; nothing is saved automatically.

### Adding things
- **New built-in category**: add a constant and `Seed` to `BuiltInCategories` (plus `cat_*` strings in all three languages); `ensureDefaults()` inserts it on next start. Custom categories need no code.
- **New reward**: add a `RewardItem` to `RewardCatalog.items` with a constant in `Models.kt`. Outfits/hair/accessories also need a drawing branch in `AvatarView.kt`; themes need a `Palette`.
- **New badge**: add to `BadgeCatalog.badges` (with `target`, `unit` and `howTo` steps), call `grantBadge(id)` from the relevant `GamificationRepository.record…()` method, and map its live progress in `RewardsViewModel` (the `when (b.id)` block that builds `BadgeStatus`).
- **New screen**: add a route in `Routes`, a `composable` in `HomeQuestNavHost`, and a `ui/screens/<feature>` package.

### Known limits / next steps
- Local-only; no sync, auth or multi-device sharing (group sharing is via text summary). Giving a member access from another phone needs a backend (e.g. Firebase phone auth + Firestore) — a separate project.
- Expenses store a single currency symbol globally; no FX.
- Voice parsing is keyword based; amounts spoken as words ("two hundred") are not recognised.
- No unit tests yet; `SplitCalculator`, `Levels` and `GamificationRepository` are the first candidates.

## 5. Release history
| Version | Date | Notes |
|---|---|---|
| 1.3.1 | 2026-10-31 | Fix: add/voice screens crashed (ActivityResult launcher could not find the Activity through the localised context); new launcher icon; equal badge tile heights; male/female option per member (DB v4) with gender-aware avatar; mood chosen in the studio is saved and shown everywhere (Auto = budget-driven); budgets redesigned with a hero ring and tap-to-edit limit sheets |
| 1.3.0 | 2026-10-31 | Custom categories + payment modes (DB v3), redesigned add-transaction form with voice entry, bottom bar with centre +, 10-mood avatar in a circle frame with softer shading, exact-alarm notifications with playful localised copy, time wheel, sample-data switch, Hindi/Gujarati, currencies trimmed to INR/USD/EUR, text ellipsis everywhere; fixes: logout, group crash (duplicate list keys), PIN screen overflow, avatar shaking |
| 1.2.3 | 2026-10-31 | Bottom nav restyled (flat surface, divider, tinted indicator), Home tab pops back to the dashboard so it always responds, inner screens no longer double-count system insets (gap above the nav bar removed), badges in one horizontal scrolling row, static avatar on Profile/Edit profile |
| 1.2.2 | 2026-10-31 | Badge grid (earned first, progress on locked) with animated detail card and how-to steps; text fields: label above, white/near-black box with hairline border |
| 1.2.1 | 2026-10-31 | Avatar v3 (Bitmoji-style face with outlines, big eyes, bold brows), 9 hair styles, 12 outfits, 6 accessories, refreshed catalog prices; screen previews regenerated for the 1.2 design |
| 1.2.0 | 2026-10-31 | Redrawn Bitmoji-style avatar; 3-step onboarding with required mobile and PIN; 4-digit PIN with keypad, inline App-lock PIN setup; design system (cards, tiles, fields), refined light/dark palettes, rebuilt Add-expense form, bottom sheets for groups/members; dashboard tiles (today, vs last month, group balance, top category, who paid, upcoming bills); fixes: Groups crash after profile switch, logout, dark-mode text |
| 1.1.0 | 2026-10-31 | Member accounts (phone + PIN, member picker, app lock, log out), expressive animated avatar with budget moods and greetings, randomised avatars per member, themed surface colours, reminders with monthly/yearly bills, notification toggles, currency list, edit-profile screen |
| 1.0.1 | 2026-10-31 | Adaptive launcher icon (gradient + house/coin, themed-icon support), Material category icons replacing emoji, GitHub Actions APK build |
| 1.0.0 | 2026-10-31 | Initial build: expenses, budgets, groups & splits, family members, avatar studio, rewards shop, badges, reminders, notifications, insights, CSV export |
