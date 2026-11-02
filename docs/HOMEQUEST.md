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
- Material 3 throughout; each of the seven themes defines its own surface, card and chip colours so the whole UI (not just the accent) changes when you apply one. **Glass** (600 pts) puts frosted, translucent cards and bars over a glowing three-colour gradient backdrop, in light and dark.
- Icons are Material *Rounded* throughout (every `Icons.Filled` / `AutoMirrored.Filled` use was moved to the Rounded set): tab bar, stat tiles (sun, trend up/down, handshake, the top category's own icon), and every Me-tab row sits in a tinted icon tile instead of an emoji.
- Bottom navigation: four tabs (Home, Expenses, Groups, Me) on a rounded bar with a notch that cradles a raised, glossy 3D **+** button (gradient, highlight, coloured shadow, press-to-squish). The strip above the bar around the button is transparent — lists scroll underneath it instead of stopping at an empty band. The selected tab gets a tinted pill behind its icon.
- Every text that can overflow is clipped with an ellipsis on one line; nothing wraps into a second line unexpectedly.
- Three languages — English, हिन्दी (Hindi) and ગુજરાતી (Gujarati) — switchable from onboarding or Me → Language; dates, badge names, reward names, mood lines and notifications all follow the chosen language.

### First launch & accounts
- A three-step onboarding (You → Money → PIN) asks for your language, name and mobile number (required, 10+ digits), currency and optional budget, then lets you create a 4-digit PIN or skip it. The PIN step fits on one screen (the avatar shrinks, keypad centred, Skip pinned at the bottom) — it no longer scrolls; only on very short screens (under 560dp for the step) does it fall back to scrolling so Skip stays reachable. You become the household owner and start at Level 1 (Rookie). A daily "Log today's expenses" reminder at 20:30 is created for you.
- Every family member is an account: name, relation, mobile number (required), avatar and an optional 4-digit PIN. Weak PINs (1111, 1234…) are rejected and the PIN is confirmed twice.
- **Local-only**: all data lives in this phone's database. A member installing the app on another phone starts with an empty ledger — the number only identifies the profile on this device. Cloud sync is not part of this release (the Family screen says so).
- Member picker: after Log out you always land on the picker, even with a single profile; members with a PIN get a keypad. The PIN screen scrolls on small phones so the keypad is never cut off.
- App lock (Me → Security): switching it on creates a PIN on the spot if you don't have one, then asks for it every time the app opens.
- Log out / switch from the Me tab returns to the picker — also after switching to a member without a PIN (1.5.0 fix). Expenses, budgets and points are shared by the whole household — only avatar and greeting are per member.

### Your avatar
- Drawn as a head-and-shoulders bust inside a circular frame everywhere it appears (header, lists, chips, picker), with soft radial shading on the face, gradient hair and clothing, blush and nose highlight.
- Male or female (chosen at onboarding, in Family members or Edit profile, and in the studio): female avatars get lashes and fuller blush, male avatars broader shoulders and thicker brows; new members get a gender-appropriate random hairstyle.
- The mood you pick in the studio is saved and shown on Home, Me, lists and chips; choose Auto to let budget/streak decide.
- 16 hair styles (short, buzz, long, wavy, bob, ponytail, curly, top bun, braids, mohawk, side part, pixie, spiky, quiff, man bun, afro), 7 hair colours, 6 skin tones.
- 5 face shapes (oval, round, square, heart, long) and, for male avatars, 6 beard styles (none, stubble, mustache, goatee, chin strap, full beard) drawn in the hair colour — both free.
- 18 outfits: casual tee, shirt, polo, hoodie, denim jacket, sweater, blazer, kurta, sherwani, saree, dress, spacesuit, hero suit, cricket jersey, leather jacket, Nehru jacket, lehenga, lab coat.
- 13 accessories: bindi, earrings, glasses, cap, headphones, crown, hair flower, nose ring, bow tie, beanie, sunglasses, gold chain, pagdi.
- Animated on Home, onboarding, the member picker and in the studio: breathes, blinks and sways. Home plays a random **action** when it opens, and on Home and in the studio the avatar also does an idle gesture on its own every 9–15 s. Seven actions: Wave, Jump (squash and stretch), Spin, Dance (arms and hips), Wink (one eye + sparkle), Cheer (both arms up + confetti ring), Laugh (happy-closed eyes, open mouth, giggle shake). Static everywhere else (lists, chips, Me header).
- **10 moods**, each with its own brows, eyes, mouth and a floating extra: Happy, In love (heart eyes + hearts), Excited (sparkles), Smart (raised brow, smirk), Chill, Confused (question mark), Sleepy (Zzz), Sad (tears), Worried (sweat drop), Angry (steam). Home picks the mood from budget progress, streak, days since your last log, time of day and day of month; the studio lets you preview all ten.
- A speech bubble greets you by time of day with a mood-specific line; each mood has six lines and the line changes every day.
- New family members get a randomised look; those parts are free so households look varied from day one.

### Home dashboard
- Header (compact): your animated avatar (tap for Me), name, a points · streak pill (tap for Rewards), a microphone button for voice entry, (the pill shows ⭐ points · 🔥 streak; the level lives on the bar below), then a labelled XP bar — "Level 6 · Accountant" on the left, "66 XP to Level 7" on the right, gradient fill — that also opens Rewards.
- All month/week figures follow the real calendar: the app re-evaluates "this month" at midnight (see `Dates.days()` in the handoff), so a month boundary never shows last month's totals.
- Cash-flow card: budget ring, spent this month, amount left / over.
- Stat tiles: Today (tap to add), vs last month, Group balance (tap to open Groups), Top category.
- **Month so far**: projected month-end spend ("on pace to spend", based on spending up to today — future-dated entries don't inflate it) with a cumulative area chart for the month and, when a budget is set, a dashed budget-pace line.
- **Last 7 days**: week total and daily average above gradient bars; today's bar is highlighted with its amount.
- **Charts are tappable**: tap any bar to highlight it and show its amount; tap (or drag across) the month area chart for a bubble with that day's date and running total; tap a donut slice to enlarge it and show the category, amount and share in the centre (tap again or tap outside to reset). The same applies on Insights.
- Category donut, "Who paid this month", "Upcoming bills" (next three) and recent transactions with "See all".
- "Insights" shortcut appears once the Advanced Insights feature is unlocked.

### Insights (Advanced Insights unlock)
- **Week / Month / Year** tabs and a `‹ period ›` navigator with the transaction count; you can page back through any past week, month or year (not into the future).
- Spending hero: total for the period, ▲/▼ % vs the previous period, daily average, number of entries and the biggest expense.
- One-line insight (which category took the biggest share, or "nice and quiet" when nothing was spent).
- Spending trend: daily area chart for a month; bars per day for a week and per month for a year, with the current (or busiest) bar highlighted and labelled.
- Categories: donut plus a ranked list with share bars and ▲/▼ change vs the previous period per category.
- Payment modes: a split bar (Cash green, UPI violet, Card orange, Bank blue) with % and amounts; Who paid (when more than one member paid).

### Expenses & the add-transaction form
- The list opens with This month / vs last month tiles, a search field and category chips; entries are grouped by day with subtotals and show category · payer · payment mode. The calendar icon in its top bar opens the Calendar.
- **Calendar** (Expenses → calendar icon, or Me → Views → Calendar): a month grid (first day of week follows the phone's locale) with each day's total; with a monthly budget set, days are green when within the average daily budget (monthly budget ÷ days in month) and red when over, and the footer shows that daily figure and a legend. Month arrows and a month dropdown (last 12 months); future days are greyed and not tappable. Tap a day to list its expenses underneath and tap one to edit it.
- Add transaction (centre **+** or Today tile): a "Say it or type it" voice banner, an amount hero, a title field, a Date row (date picker) with **Yesterday / Today / Tomorrow** quick chips and a Category row (sheet), then an inline card with **Paid by** member chips and the four **payment-mode** tiles — no sheet needed for either; a "Split with a group" row (sheet) and a note under "Other details". Group splits (equal or custom) appear inline when a group is chosen.
- Form sheets (new reminder, category, group, member, budget limit) close only with the X button or a tap outside — dragging while scrolling their content no longer dismisses them.
- **Payment modes**: Cash, UPI, Card, Bank (labelled "Bank" so the tile never truncates) — stored per expense and included in the CSV export. The Yesterday / Today / Tomorrow chips share the row equally so no label is cut off.
- **Voice entry**: tap the banner or the Home microphone, say e.g. "250 groceries by UPI" or "paid 120 for pani puri cash"; the amount, category (built-in keywords in English/Hindi/Gujarati words such as sabzi, kiraya, dawa, plus your custom category names), payment mode and title are filled in for you to review and save. The **Note** is filled too: say "note …" (or नोट / નોંધ) and everything after it becomes the note; otherwise the whole sentence you said is kept as the note. A typed note is never overwritten; a note that came from an earlier voice capture is replaced by the new one. "note" only counts as the marker when a word comes before and after it, and not after ka/ke/ki/no/ni/na, so "500 ka note" or "200 stationery notes" parse normally. Uses the phone's speech recogniser in the app language.
- **Custom categories** (Expenses → tune icon, or Me → Categories): add your own with a name, one of 26 icons and 40 preset colours plus a **custom colour** hue slider; built-in categories can be recoloured/re-iconed; deleting a custom category moves its expenses to Other.
- Titles, notes, names and reminder titles are saved with a capital first letter; text fields default to sentence capitalisation.
- Tap any expense to edit or delete it. Every new expense earns points (see Rewards).

### Budgets & alerts
- Budgets screen: a hero card with the budget ring, spent / left this month and a "Set monthly budget" button; each category is a card with icon, name and progress bar on the left and the spent amount (with "of limit") centred next to the chevron — tap it to set or remove a limit in a sheet with quick-amount chips.
- Push notifications when a budget crosses 80% and 100% (can be switched off in Me → Notifications).
- Finishing a month under the overall budget earns +100 points and the Budget Boss badge.

### Groups & splitting
- Create groups (home, trip, party…) from your family members with an emoji — pick one of eight, or tap **+** to type/paste any emoji as a custom icon.
- Add an expense to a group and split it equally among chosen members or with custom amounts; the form shows live per-person shares and whether custom amounts add up.
- Group page shows total shared, who owes whom, a minimal "settle up" plan, and all shared expenses. "Mark paid" records a settlement and earns points.
- Share button sends a localised text summary of balances to WhatsApp, SMS, email, etc.

### Family members
- Add partner, kids, parents, roommates or friends with a relation label; each has their own avatar (tap to open the studio for them).
- Male/female per member; new members join as "from now on" or can be added to every existing group with past equal splits recomputed to include them (a choice sheet appears when groups exist).
- Removing a member who is part of shared expenses asks whether to re-split those past expenses between the remaining members or keep the old records; their own expenses and splits are then removed.

### Avatar studio
- Skin tone (6), hair colour (7), face shape (5) and beard (6, male only), always free.
- Hair (16), outfits (18) and accessories (13): short/buzz/long/curly/side part/pixie hair, tee/shirt/polo and no accessory are free; the rest unlock with points directly from the studio.
- Live preview on your own avatar (tap it for a random action), a row of seven action chips (Wave, Jump, Spin, Dance, Wink, Cheer, Laugh), a chip for each of the ten moods and Auto. The hero area is compact so more of the wardrobe is visible.

### Rewards & gamification
**Earning points**
- +10 per expense logged, +5 for the first log of the day, +2 per streak day (max +20)
- +30 create a group, +15 settle a balance, +20 add a family member
- +100 finish a month under budget
- Bonus points for every badge earned

**Levels** — XP-based (Rookie → Saver → Planner → Tracker → Strategist → Accountant → Treasurer → Guardian → Master → Legend). Level-ups trigger confetti, an in-app toast and a notification.

**Badges (33, easy and hard)** — bonus points total 6,130, more than the whole shop (≈6,650 pts) once logging points are added. Rewards shows the first eight (earned first, then closest to completion) in a row with staggered pop-in and a pulsing emblem on earned ones; "See all" opens the Badges page: a 3-column grid grouped into Earned / Easy / Hard with the points available in each group. Tap any badge for an animated detail card with a numbered "How to earn it" list.

Badge list: First step (1 expense), Getting serious (10), Century (100), Half a thousand (500), Week warrior / Month master / Iron streak / Legendary streak (7/30/60/100-day streaks), Early bird (log before 9 AM), Night owl (after 10 PM), Say it out loud (voice entry), Note taker (10 notes), Explorer (5 categories), Make it yours (custom category), Payment pro (all 4 modes), Big spender (one expense ≥ 10,000), Penny counter (20 under 100), Team player / Group guru (1/3 groups), Fair share / Settler (1/10 settlements), Family first / Full house (3/5 members), Bill planner (5 reminders), Budget boss / Budget hat-trick (1/3 months under budget), Stylist (outfit), Accessorized (3 accessories), New look (theme), Collector (5 paid unlocks), Polyglot (language switch), Rising star / Legend (level 5/10).

The Rewards hero card has a slow light sweep and a bobbing star; shop rows animate when the filter changes.

**Shop** — spend points on 7 colour themes, avatar outfits, hair styles and accessories, and two features (Advanced Insights, CSV export). **Unlock everything with Pro** — a gold Pro card ("grab a points pack and unlock themes, looks and features faster"; a pack speeds unlocking up, it does not unlock the whole catalogue) (on Me and under the shop) opens a sheet with three plans: Starter ₹49 → 500 pts, Plus ₹99 → 1,200 pts, Pro ₹199 → 3,000 pts (marked *Best value*). Coming from Me opens Rewards with the sheet already up. Tapping a plan opens the system chooser with a `upi://pay` deep link to the configured UPI id (no SDK, no backend); the id itself is never shown in the app. If the UPI app returns `Status=SUCCESS` the points are credited at once; `FAILURE` shows an error; anything else (most apps return nothing useful) asks "Did the payment go through?" and credits on the user's word. Bought points go through the normal ledger (`purchase:<pack>`) and count toward levels. Points history is listed at the bottom of Rewards. Rewards opens from the Home pill or Me → Rewards.

### Reminders & bills
- Repeat Once / Daily / Weekly (weekday) / Monthly (day of month) / Yearly (month + day); quick presets for rent, EMI, electricity, insurance, school fees and a daily nudge.
- Time is chosen on a scrolling **time wheel** (hour · minute · AM/PM) with the selected row highlighted — no more stock clock dialog.
- Optional amount and category; cards show the schedule, "Due today / tomorrow / in N days" and the amount, sorted by next due date.
- Notifications are delivered by exact alarms, so they arrive on time even when the app is closed or the phone is idle, and are re-registered after a reboot or time change.
- Reminders can be **edited**: tap the card (or its pencil) to reopen the form pre-filled; it saves over the same reminder and reschedules it. Delete is a red tinted round button with a confirmation dialog.
- Notification copy is sassy and rotates instead of being random: daily nudges pair 20 titles with 31 bodies (no title+body repeat for 620 days), bill reminders 12 × 13, budget warnings and over-budget alerts 12 × 11 each, badges 8 × 9 and level-ups 8 × 9 — all in English, Hindi and Gujarati.

### Me tab & settings
- Compact profile header: avatar on the left (tap for the studio), name, relation · mobile and level · points · streak on the right, Edit profile and Avatar buttons side by side below.
- **Views**: Day (Expenses list), Calendar, Insights.
- **Unlock everything with Pro** card (opens the Pro plans, see Shop).
- Household: Rewards, Family members, Categories, Budgets, Reminders & bills, Insights.
- **Your data**: **Import CSV** (always available) and **Export CSV** (when unlocked). Export opens a sheet: *All expenses* (`homequest-expenses.csv`) or any month that has expenses (`HomeQuest_November_2026.csv`). Import reads the same format: categories are matched by name in any app language (unknown names become custom categories), the payer by member name (otherwise you), rows already in the app are skipped, and a summary shows added / skipped / unreadable rows. Imported expenses earn no points (badges are re-checked). Group links and splits are not part of the CSV round trip — rows come in as personal expenses. An import keeps running if you leave the Me tab.
- Security: App lock. Language: English / Hindi / Gujarati. Appearance: System / Light / Dark. Currency: ₹ INR, $ USD, € EUR.
- Notifications: three independent toggles.
- **More options**: Invite a friend (share sheet with a link to the project), Rate app (1–5 stars; 4–5 opens the GitHub project, lower opens its issues page), Query / feedback (GitHub issues), FAQs (sheet with six Q&As), About app (version from the installed package and how data is stored).
- Account: Log out / switch, and — for the household owner only — **Erase all data**: one confirmation wipes every member, expense, group, reminder, budget, point, badge and setting, cancels scheduled alarms and notifications, and returns to the welcome screen.
- All data stays on the device and is included in Android backup.

## 4. Engineering handoff
### Stack
| Concern | Choice |
|---|---|
| Language / UI | Kotlin 2.1, Jetpack Compose (BOM 2024.12), Material 3, material-icons-extended |
| Navigation | navigation-compose 2.8 with string routes (`ui/navigation/Routes.kt`) |
| Persistence | Room 2.6 (KSP, schema v6 with migrations) + DataStore Preferences |
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
data/repository       Expense, Member, Group, Reminder, Gamification, Settings, Category repositories; BadgeTracker (badge progress snapshot + live statuses)
domain                Models (Category, PaymentMode, Mood, Gender, AppLanguage, Relations, ids), Catalogs (rewards; BadgeCatalog with BadgeSnapshot/BadgeStatus/BadgeTier and progress rules), Gamification (points, levels, ledger reason codes, split maths)
notifications         NotificationHelper (localised copy rotated by day), BudgetMonitor, ReminderScheduler (AlarmManager), ReminderReceiver + BootReceiver
util                  Dates, Sharing (localised summary, CSV export), Csv (parser + cell escaping + column names), Pin, Text (titleCased), LocaleHelper, VoiceParser, Upi
ui/theme              Palettes (7 themes → light/dark ColorScheme; ColorScheme.glassy for Glass), Theme (animated colour switch, GlassBackdrop)
ui/components         Charts (ProgressRing; tappable DonutChart, BarChart and AreaChart with optional dashed pace line; ShareBar), Celebrations (counters, confetti, toast, ledger reason labels), AvatarView (Canvas avatar + AvatarPlay actions), ExpenseRow, Common (category/relation labels, moodLine), DesignSystem (cards, fields, StatTile, PickerRow, LevelProgress, ProCard, TimeWheel, PIN)
ui/navigation         Routes, HomeQuestNavHost (notched bottom bar with the 3D centre add button + graph)
ui/screens/*          one package per feature, each with its screen and view model (rewards/ also holds BadgeComponents — tile + detail sheet shared by Rewards and the Badges page)
```

### Data model
- `members` — owner row seeded on first run (`isOwner = 1`); avatar fields stored inline (`skinTone`, `hairId`, `hairColor`, `outfitId`, `accessoryId`, `faceShape`, `beard`), plus `phone` and `pinHash` (`salt:sha256(salt:pin)`, see `util/Pin.kt`; PINs are exactly `Pin.LENGTH` = 4 digits, `Pin.isWeak` rejects repeats and sequences). `MemberEntity.avatar()` converts to `AvatarConfig`; `MemberEntity.hasPin` is derived. New members get a random avatar in `MemberRepository.add()`.
- Active account: `SettingsRepository.activeMemberId` (0 = nobody signed in). `MemberRepository.activeMember` is the flow screens use for "who am I"; `activeMemberId()` falls back to the owner. `MainViewModel.gate()` decides between onboarding, the lock screen (`ui/screens/profile/LockScreen.kt`) and the app; `sessionUnlocked` is in-memory so App lock re-prompts on every cold start.
- `categories` — `id` (built-ins keep the old enum names `GROCERIES`…`OTHER` so no expense rows had to be rewritten; custom ones are `custom_<millis>`), `name` (custom only; built-in names come from `BuiltInCategories.nameRes`), `icon` (key into `CategoryIcons`), `color` (ARGB), `sortOrder`, `builtIn`. `CategoryRepository.ensureDefaults()` seeds the built-ins at bootstrap (`INSERT OR IGNORE`). Deleting a custom category reassigns its expenses to `OTHER` and drops its budget. UI resolves ids with `List<Category>.byId()` / `Map.resolve(id)` and labels with `Category.label()` (composable) or `label(context)`.
- `expenses` — `category` is a category id string, `paymentMode` is `PaymentMode` (CASH/UPI/CARD/BANK, stored as text). `epochDay` (LocalDate.toEpochDay) is the only date used for grouping/queries; `createdAt` orders within a day. `paidByMemberId` cascades on member delete; `groupId` is set null on group delete.
- `expense_splits` — per-member share; present only for group expenses. Deleted with the expense.
- `groups`, `group_members` (junction), `settlements` — balances are computed in `GroupRepository.balances()` from expenses + splits + settlements; `SplitCalculator.settlements()` produces the minimal transfer list.
- `reminders` — `repeat` ONCE/DAILY/WEEKLY/MONTHLY/YEARLY with `weekday`, `dayOfMonth`, `month`, optional `amount` and `category`; `ReminderEntity.nextOccurrence()` (in `ReminderScheduler.kt`) computes the next fire time and clamps day-of-month to the month length.
- `budgets` (per category id; the overall monthly budget lives in DataStore), `profile` (single row: points, lifetime XP, streak, last log day, last budget month checked, `voiceLogs`), `unlocks`, `earned_badges`, `points_ledger` (`reason` is a code from `LedgerReasons`, e.g. `badge:badge_century`, rendered by `reasonLabel()` in the current language).
- `members`, `expenses`, `groups` and `reminders` still carry an `isSample` column (always 0). It was used by the sample-data switch removed in 1.3.2; it stays because SQLite on minSdk 26 cannot `DROP COLUMN` and recreating four tables with foreign keys is not worth the migration risk. Nothing reads or writes it.

Insights figures and the dashboard's month-pace series are computed in memory from `ExpenseRepository.all` (the whole ledger is small enough), so period switching never needs a new query. The old `observeMonthCategoryTotals` SQL path was removed with the 6-month stacked chart in 1.5.0.

### CSV import / export & erase
- Export (`Sharing.exportCsv(context, expenses, categories, fileName)`) writes `CsvColumns.HEADER` = `date,title,category,payment_mode,amount,paid_by,group_id,note`. Since 1.5.0 `date` is ISO `yyyy-MM-dd` (earlier builds wrote the localised long label, which could not be read back); `category` is the display label.
- `data/repository/CsvImporter` parses with `Csv.parse` (quoted cells, `""` escapes, CRLF, BOM; a `"` only opens a quoted cell at the start of a cell, so `5" TV` stays literal), requires `date`, `title`, `amount`, and accepts ISO, `d/M/yyyy`, `d-M-yyyy`, and the pre-1.5 long label (`EEE, d MMM yyyy` / `d MMM yyyy`) in English, Hindi and Gujarati. Amounts may carry ₹/$/€ and grouping commas (`1,25,000`, `1,250.50`); anything else (negative, `1.234,50`) is counted as unreadable rather than guessed. Titles and notes are title-cased like a normal save. Duplicates are detected against existing rows on `(epochDay, lower(title), amount, category)`. New rows go in with one `ExpenseDao.insertAll` (single transaction), bypassing `ExpenseRepository.save`, so no points or budget alerts fire; `BadgeTracker.refresh()` runs once at the end. `ProfileViewModel.importCsv` runs it on IO under `NonCancellable`. Custom category ids are `custom_<UUID>` (was `custom_<millis>`, which could collide when an import created several in one millisecond).
- `AppContainer.eraseAll()` cancels enabled reminder alarms, runs `db.clearAllTables()` and deletes the export cache on IO, cancels all notifications, clears DataStore last, then re-runs `bootstrap()`. Settings go last so an interrupted erase can never leave `onboarded = false` on top of the old database (onboarding would then just rename the old owner). `MainViewModel.eraseAll()` sets `erasing`, which holds the gate on Onboarding for the whole wipe so no screen reads a half-empty DB, and runs the erase under `NonCancellable` because the Profile screen's view model is destroyed as soon as the gate changes.

### Database versions
- v1 → v2 (`MIGRATION_1_2`): adds `members.phone`, `members.pinHash`, `reminders.dayOfMonth/month/amount/category`.
- v2 → v3 (`MIGRATION_2_3`): creates `categories`, adds `expenses.paymentMode` (default `CASH`) and `isSample` to `expenses`, `members`, `groups`, `reminders` (unused since 1.3.2, see above). The `groups` table name is quoted with backticks in every `@Query` because `GROUPS` became a SQLite keyword in 3.25.
- v3 → v4 (`MIGRATION_3_4`): adds `members.gender` (MALE/FEMALE, default MALE) and `members.mood` (empty = automatic).
- v4 → v5 (`MIGRATION_4_5`): adds `profile.voiceLogs` (count of expenses saved from voice entry, for the voice badge).
- v5 → v6 (`MIGRATION_5_6`): adds `members.faceShape` (`FaceShape` name, default `OVAL`) and `members.beard` (`Beard` name, default `NONE`). Both are Room-native enum columns like `gender`.
- Add further migrations to `HomeQuestDatabase.build()`; never bump the version without one.

### Avatar & moods
`ui/components/AvatarView.kt` draws the character on a Canvas using a 100×100 coordinate space (`Pen` helper scales paths). The canvas is clipped to a circle with a themed `background`, so the bust is naturally cropped at the shoulders — never wrap it in another circle. Faces, hair and clothes use radial/vertical gradients for a soft 3D look; the outline is a faint 25% brown. `animated` enables breathing/gentle sway/blink, idle gestures and actions. Actions: pass `play = AvatarPlay(action, nonce)` (`domain.AvatarAction`: WAVE, JUMP, SPIN, DANCE, WINK, CHEER, LAUGH, each with its own `durationMs`); a new nonce replays. One `LaunchedEffect(animated, play, idleActions)` runs the requested action and then, when `idleActions = true` (Home and studio only), loops idle gestures (`IdleGestures`, every 9–15 s), so a new `play` cleanly cancels whatever was running — do not add a second effect that animates `progress`. `poseFor(action, t)` is evaluated inside the Canvas draw block (so actions only redraw, never recompose) and turns progress into a `Pose` (translate, rotation, scale, left/right arm raise, wink, laugh, burst), applied around the bottom-centre pivot; arms are drawn by `drawArm` from the shoulders, `drawBurst` paints the cheer confetti ring and the wink sparkle. `AvatarConfig.faceShape` selects the jaw curve in `facePath()` (the same path clips stubble and chin-strap beards); `AvatarConfig.beard` is drawn only for MALE (`drawBeard` before the mouth, the mustache after it). `AvatarConfig.gender` (from `members.gender`) widens the shoulders and drops the lashes for MALE, and `AvatarConfig.mood` is the mood saved from the studio (`members.mood`, null = automatic) — `AvatarView` defaults its `mood` parameter to it so every call site shows the saved mood, and the dashboard only computes a mood when none is saved. `mood` (`domain.Mood`, 10 values) maps to a private `Face` spec (`faceFor`) — brow tilt/raise/asymmetry, eye openness/scale, heart eyes, mouth shape and a floating extra (hearts, sparkles, sweat, tears, question mark, Zzz, steam). `moodFor(progress, hasBudget, streak, daysSinceLastLog, hour, dayOfMonth)` picks the mood in priority order angry → worried → sleepy → sad → confused → love → excited → smart → neutral → happy; the greeting is `moodLine(mood, today, name)` — a line from the mood's `linesRes` string-array chosen by epoch day, so it changes daily — prefixed by a time-of-day string.

### Gamification rules
All numeric rules live in `domain/Gamification.kt` (`PointsRules`, `Levels`) and `domain/Catalogs.kt` (`RewardCatalog`, `BadgeCatalog`). `GamificationRepository` is the only writer of points/badges/unlocks and emits `RewardEvent`s on a `SharedFlow`; `MainViewModel` turns those into the toast, confetti and reward notification.

- Streak logic: first log of a day increments the streak if the previous log was yesterday, otherwise resets to 1.
- Level = `floor(sqrt(lifetimeXP / 100)) + 1`; thresholds `(level-1)² · 100 → level² · 100`.
- Month-under-budget award is evaluated once per month in `BudgetMonitor.closePreviousMonthIfNeeded()` on app start, guarded by `profile.lastBudgetCheckMonth`.
- Free catalog items are inserted into `unlocks` at bootstrap, so "unlocked" is the single source of truth for both themes and avatar parts.
- **Badges** are not granted from individual `record…()` calls. `BadgeCatalog.progress(id, snapshot)` maps every badge to a number from a `BadgeSnapshot` (expense counts, hours of day, notes, voice logs, streak, groups, settlements, members, reminders, custom categories, paid unlocks by kind, months under budget, level, language). `BadgeTracker.refresh()` builds the snapshot from the DAOs and calls `GamificationRepository.checkBadges()`, which grants every badge whose progress reached its target (one ledger line + event each). Repositories call `badges.refresh()` after anything that can move a counter (expense, group, settlement, member, reminder, category, unlock, language) and `AppContainer.bootstrap()` runs it on start, so badges added in an update are awarded retroactively. `BadgeTracker.statuses` is the live `Flow<List<BadgeStatus>>` behind Rewards and the Badges page; `sortedForDisplay()` orders earned first, then by completion.
- **Re-splitting**: `GroupRepository.addMemberToAllGroups(memberId, resplitPast)` and `resplitWithout(memberId)` recompute `SplitCalculator.equalShares` for every EQUAL-split group expense (custom splits are left untouched) through `ExpenseDao.upsertWithSplits`, so balances update consistently.

### Notifications
- Channels: `reminders` (high), `budget` (high), `rewards` (default).
- Reminders: `ReminderScheduler.schedule()` sets one `AlarmManager` alarm per reminder (`setExactAndAllowWhileIdle`, falling back to `setAndAllowWhileIdle` when exact alarms are not permitted on API 31–32) with a broadcast `PendingIntent` keyed by reminder id. `ReminderReceiver` posts the notification and reschedules (or disables a ONCE reminder); `BootReceiver` calls `ReminderRepository.rescheduleAll()` on `BOOT_COMPLETED`, `TIME_SET` and `TIMEZONE_CHANGED`, and `AppContainer.bootstrap()` does the same on every cold start. Manifest: `SCHEDULE_EXACT_ALARM` (≤32), `USE_EXACT_ALARM` (33+), `RECEIVE_BOOT_COMPLETED`.
- Copy: `NotificationHelper` resolves strings through `LocaleHelper.localized(context, settings.language)`, so wording follows the in-app language. Titles and bodies come from `notif_*` string arrays via `Resources.rotate(array, seed)` with `seed = epochDay + something stable` (reminder id, label hash, badge id hash, level). Title and body use the same seed against arrays of coprime length, so the pair only repeats after length × length days (nudges 20 × 31 = 620 days). Keep each pair coprime when adding lines, and every placeholder set identical across languages.
- Budget alerts fire from `BudgetMonitor.afterExpenseAdded()` when a total crosses 80% or 100% of a limit.
- `POST_NOTIFICATIONS` is requested once on launch (API 33+). `NotificationHelper.canPost()` guards every post.

### Design system
`ui/components/DesignSystem.kt` holds the shared building blocks: `HqCard` (22dp radius, `surfaceContainerLowest`, hairline border), `SectionCard`, `StatTile` (label, value, a Material icon in an accent-tinted tile, hint), `LevelProgress` (labelled XP bar), `ProCard` (gold Pro upsell), `PickerRow` (icon tile + label/value + chevron, the row style of the add-transaction form), `HqTextField` (label above the box, soft filled container, border only on focus/error, sentence capitalisation by default), `PrimaryButton`, `TimeWheel` (three snap-scrolling `LazyColumn` wheels with a highlighted centre row and fade overlays; the centre item is derived from `layoutInfo`), and the PIN components (`PinDots`, `PinPad`, `PinEntry`, `PinSetupSheet`). `OptionSheet`, `CategoryGrid`, `MemberChip` and `MemberFlow` live in `AddExpenseScreen.kt` and are reused by the group, family, reminder and category sheets. All `Text` that can grow is given `maxLines` + `TextOverflow.Ellipsis`. New screens should compose these rather than raw Material `Card`/`OutlinedTextField`.

### Session & navigation
`MainActivity` switches between `OnboardingScreen`, `LockScreen` and `HomeQuestNavHost` via `MainViewModel.gate()`. `MainViewModel.settings` is `null` until DataStore has loaded and `MainActivity` renders nothing until then, with a `Crossfade` between gates — rendering the default settings first flashed Onboarding/Lock for a frame on launch and sign-out (1.3.x flicker). The `NavHostController` is created inside `HomeQuestNavHost`, so each unlock gets a fresh controller — reusing one across the lock screen caused the "ViewModelStore should be set before setGraph" crash seen in 1.1. Sign-out goes through `MainViewModel.signOut()` (clears `sessionUnlocked` and `activeMemberId`).

`MainViewModel.signOut()` writes `activeMemberId = 0` and waits for the settings flow to show it before clearing `sessionUnlocked`. Clearing it first (≤1.4) briefly produced LOCKED with the old member preselected; with App lock on and a PIN-less member that member was auto-unlocked again, so Log out appeared to do nothing after switching profiles. `LockScreen` only preselects a member when `preselectedId` matches one (app-lock re-prompt); after sign-out `activeMemberId` is 0 so the picker is always shown — auto-selecting a lone PIN-less profile was what made Log out appear to do nothing in 1.2.

Bottom tabs (`HqBottomBar`): Home, Expenses, Groups, Me plus a centre FAB that opens `Routes.addExpense()`; Rewards is a normal pushed route reachable from the Home pill and Me. The Home tab calls `popBackStack(Routes.DASHBOARD, inclusive = false)` instead of `navigate` — Dashboard is the graph's start destination and re-navigating to it with `restoreState` could leave the tab unresponsive after visiting other tabs. The other tabs `navigate` with `popUpTo(Routes.DASHBOARD) { saveState = true }`. The outer `Scaffold` in `HomeQuestNavHost` uses `contentWindowInsets = WindowInsets.navigationBars`: the status-bar inset is left to each screen (its `TopAppBar`, or `statusBarsPadding()` on Home and Me). Until 1.6.0 the outer scaffold also applied the status-bar inset, which doubled the gap above every top bar. Every screen-level `Scaffold` passes `contentWindowInsets = WindowInsets(0, 0, 0, 0)`, otherwise the navigation-bar inset is applied twice and a blank band appears above the bottom bar. The bar reserves `FabOverhang` (30dp) above itself for the raised add button. The NavHost's bottom padding subtracts that overhang while the bar is shown, so content scrolls under the transparent strip. Tab lists therefore keep about 40dp of bottom content padding. `NotchedBarShape` is the rounded bar minus a circle of `FabSize / 2 + 6dp` at the top centre (`Path.combine(Difference)`). Concave outlines only cast shadows from Android 10, so on API 26–28 the bar falls back to the plain rounded-top shape.

Routes with arguments: `Routes.rewards(pro)` (`rewards?pro=` — `true` opens the Pro plans sheet on arrival; the sheet's open state is `rememberSaveable`), `Routes.CALENDAR`.

### Launcher icon
`res/mipmap-anydpi-v26/ic_launcher.xml` (and `_round`) is an adaptive icon: `drawable/ic_launcher_background.xml` (gradient + soft circles), `drawable/ic_launcher_foreground.xml` (house, door, window, star coin, drawn small enough to sit inside the 33-radius safe circle so no launcher mask clips it — 1.4.0 shrank it after the 1.3.1 version was cut at the edges) and `drawable/ic_launcher_monochrome.xml` for themed icons. `drawable/ic_notification.xml` is the matching white-house silhouette used for all notifications. Source SVG of the design is reproduced in the foreground drawable paths.

### Theming
`Palettes.forId(themeId)` returns a `Palette`; `Theme.kt` builds light/dark `ColorScheme`s and animates primary/secondary/tertiary/background/surface/containers when the theme id changes so unlocking a theme visibly morphs the UI. A palette with `glass = true` (Glass) goes through `ColorScheme.glassy(dark)`. That makes `background`/`surface` transparent and `surfaceContainerLowest`, `surfaceContainerHigh`, `surfaceVariant` and `outlineVariant` translucent white, so cards, fields and the bottom bar frost. Sheet, menu and dialog containers (`surfaceContainer`, `surfaceContainerLow`, `surfaceContainerHigh`, `surfaceContainerHighest`) stay 94–96% opaque, because dialogs open in their own window without the backdrop and would otherwise be see-through. Chart point markers use `onPrimary` rather than `surface`, which is transparent under Glass. `HomeQuestTheme` then wraps content in `GlassBackdrop` (the base colour plus three radial glows in primary, tertiary and secondary). Anything that paints `colorScheme.background` as a solid fill shows the backdrop under Glass, which is intended.

### Localisation
All user-facing text is in `res/values/strings.xml` (English, the source of truth), `values-hi` and `values-gu`; every key exists in all three and placeholder sets are identical. Enums/catalogs carry `@StringRes` ids (`Mood`, `PaymentMode`, `RepeatRule`, `RewardKind`, `Relations`, `RewardItem`, `Badge`, `LevelInfo`). The language is a DataStore setting; `MainActivity` wraps the whole tree in `CompositionLocalProvider(LocalContext provides LocaleHelper.localized(...), LocalConfiguration ...)`. `LocaleHelper.localized` returns a `ContextWrapper` around the Activity whose `getResources()` is the localised one — it must stay a wrapper of the Activity, because `rememberLauncherForActivityResult` finds its `ActivityResultRegistryOwner` by walking `LocalContext` up to the Activity (a bare `createConfigurationContext` result crashed the add-expense and voice screens in 1.3.0) and sets `Locale.setDefault`, so `stringResource`, `java.time` formatters and the speech recogniser all follow it without an Activity restart. Relation values are stored as lowercase keys (`me`, `partner`…) and resolved with `relationLabel()`; rows written by older versions (`Me`, `Partner`) still match case-insensitively.

### Voice entry
`AddExpenseScreen` launches `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` (no RECORD_AUDIO permission needed; the system recogniser handles it) in the app language and feeds the first result to `VoiceParser.parse(text, customCategories)`, which extracts the first number as the amount, matches payment-mode and category keyword lists (English plus common Hindi/Gujarati words) and custom category names, strips filler words and title-cases the rest. A `note`/`नोट`/`નોંધ`/`નોટ` marker splits the sentence: text after it becomes `Result.note` and is excluded from parsing. `AddExpenseViewModel.applyVoice` sets the note to that part, or to the whole transcript when no marker was said and the note is still empty. The result pre-fills the form for review; nothing is saved automatically.

### Adding things
- **New built-in category**: add a constant and `Seed` to `BuiltInCategories` (plus `cat_*` strings in all three languages); `ensureDefaults()` inserts it on next start. Custom categories need no code.
- **New reward**: add a `RewardItem` to `RewardCatalog.items` with a constant in `Models.kt`. Outfits/hair/accessories also need a drawing branch in `AvatarView.kt`; themes need a `Palette`.
- **New badge**: add a constant and `Badge` to `BadgeCatalog.badges` (tier, `target`, `unitRes`, `howTo` steps; strings `b_<id>`, `b_<id>_d`, `b_<id>_1..3` in all three languages) and a branch in `BadgeCatalog.progress()`. If it needs a new counter, add it to `BadgeSnapshot` and `BadgeTracker.build()`. Nothing else changes — it is awarded on the next `refresh()`.
- **Tappable shapes**: put `.clip(shape)` before `.clickable` (or pass the shape to the background after a clip) so the ripple follows the shape; `HqCard` clips to `CardShape` when it has `onClick`. Unclipped clickables showed square ripples on round buttons until 1.5.0.
- **Anything that depends on "today"**: collect `Dates.days()` and `flatMapLatest` over it (see `DashboardViewModel`, `ExpensesViewModel`, `BudgetsViewModel`, `InsightsViewModel`) instead of capturing `LocalDate.now()` in a constructor — that is what showed October totals under "Spent in November" at midnight.
- **New screen**: add a route in `Routes`, a `composable` in `HomeQuestNavHost`, and a `ui/screens/<feature>` package.

### Known limits / next steps
- Local-only; no sync, auth or multi-device sharing (group sharing is via text summary). Giving a member access from another phone needs a backend (e.g. Firebase phone auth + Firestore) — a separate project.
- Expenses store a single currency symbol globally; no FX.
- Voice parsing is keyword based; amounts spoken as words ("two hundred") are not recognised.
- Auto-capturing payments from GPay/PhonePe/CRED or bank SMS is not built. The realistic route for a sideloaded APK is a `NotificationListenerService` (user grants notification access) parsing UPI and bank notifications into draft expenses. `READ_SMS` works for sideloading; on the Play Store it is a restricted permission that needs a permissions-declaration approval (SMS-based money management is one of the allowed exception categories). The regulated alternative for bank data is the RBI Account Aggregator framework, which needs a licensed AA partner and a backend. None of these apps offer a public transaction API.
- UPI top-up is trust-based: `util/Upi.kt` builds the deep link (private `ID`, amount, note) and parses the optional `response` extra, but there is no way to prove a payment happened without a payment gateway (Razorpay, Cashfree, PhonePe PG) and server-side verification. Treat it as a demo, not a store. The manifest declares a `<queries>` entry for the `upi` scheme so the chooser can see UPI apps on Android 11+.
- No unit tests yet; `SplitCalculator`, `Levels` and `GamificationRepository` are the first candidates.

## 5. Release history
| Version | Date | Notes |
|---|---|---|
| 1.6.0 | 2026-11-02 | Avatar: 7 actions (wave, jump, spin, dance, wink, cheer, laugh) plus idle gestures, 5 face shapes, 6 beards (DB v6), 6 new hair styles, 6 outfits, 7 accessories; Home: labelled XP bar, compact header, daily-rotating mood lines; tap-to-inspect bar, donut and area charts; voice entry fills the note; "Bank" label and full-width date chips; notched bottom bar with a 3D add button and content under the overhang; double status-bar inset removed (top bars sit higher); Calendar view with over/within-budget days; compact Me header, Views, Pro card with three plans, More options (invite, rate, feedback, FAQs, about); "trial" wording removed; 40 category colours plus a hue slider; reminders editable with a confirm-delete; custom group emoji; sassy notifications rotating without repeats; Glass theme; Material Rounded icon refresh |
| 1.5.0 | 2026-11-01 | Insights rebuilt (Week/Month/Year with period navigation, totals vs previous period, trend charts, category changes, payment-mode split); Home gets a month-pace area chart and richer week card; CSV import + per-month export (ISO dates); owner-only Erase all data; Yesterday/Today/Tomorrow chips in the add form; one-screen onboarding PIN step; fixes: Log out after switching to a PIN-less member, square ripples on round controls; UPI id no longer shown |
| 1.4.1 | 2026-11-01 | UPI top-up trial on Rewards: point packs open any UPI app via deep link, credited on SUCCESS or user confirmation; `purchase` ledger reason |
| 1.4.0 | 2026-10-31 | 33 badges (easy + hard, worth more than the whole shop) with a snapshot-based engine that awards retroactively (DB v5), Badges page + animated Rewards; add/remove member asks whether to re-split past group expenses; Paid by and payment mode inline in the add form; form sheets no longer close on scroll; month/week figures roll over at midnight; launch/logout flicker removed; launcher icon fits the safe zone; budget amounts centred beside the chevron |
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
