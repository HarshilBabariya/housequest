# HomeQuest — Features

## Look & feel
- Adaptive launcher icon: coral-to-pink gradient, a large white house with pink door and windows filling the safe zone, gold star coin on the roof; round, squircle and Android 13+ themed (monochrome) variants are all supported.
- Material 3 throughout; each of the six themes defines its own surface, card and chip colours so the whole UI (not just the accent) changes when you apply one.
- Bottom navigation: four tabs (Home, Expenses, Groups, Me) on a rounded floating bar with a large centre **+** button that opens the add-transaction form from anywhere.
- Every text that can overflow is clipped with an ellipsis on one line; nothing wraps into a second line unexpectedly.
- Three languages — English, हिन्दी (Hindi) and ગુજરાતી (Gujarati) — switchable from onboarding or Me → Language; dates, badge names, reward names, mood lines and notifications all follow the chosen language.

## First launch & accounts
- A three-step onboarding (You → Money → PIN) asks for your language, name and mobile number (required, 10+ digits), currency and optional budget, then lets you create a 4-digit PIN or skip it. You become the household owner and start at Level 1 (Rookie). A daily "Log today's expenses" reminder at 20:30 is created for you.
- Every family member is an account: name, relation, mobile number (required), avatar and an optional 4-digit PIN. Weak PINs (1111, 1234…) are rejected and the PIN is confirmed twice.
- **Local-only**: all data lives in this phone's database. A member installing the app on another phone starts with an empty ledger — the number only identifies the profile on this device. Cloud sync is not part of this release (the Family screen says so).
- Member picker: after Log out you always land on the picker, even with a single profile; members with a PIN get a keypad. The PIN screen scrolls on small phones so the keypad is never cut off.
- App lock (Me → Security): switching it on creates a PIN on the spot if you don't have one, then asks for it every time the app opens.
- Log out / switch from the Me tab returns to the picker. Expenses, budgets and points are shared by the whole household — only avatar and greeting are per member.

## Your avatar
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

## Home dashboard
- Header: your animated avatar (tap for Me), name, a level · points · streak pill (tap for Rewards), a microphone button for voice entry, and the XP bar.
- Cash-flow card: budget ring, spent this month, amount left / over.
- Stat tiles: Today (tap to add), vs last month, Group balance (tap to open Groups), Top category.
- 7-day bar chart, category donut, "Who paid this month", "Upcoming bills" (next three) and recent transactions with "See all".
- "Insights" shortcut appears once the Advanced Insights feature is unlocked.

## Expenses & the add-transaction form
- The list opens with This month / vs last month tiles, a search field and category chips; entries are grouped by day with subtotals and show category · payer · payment mode.
- Add transaction (centre **+** or Today tile): a "Say it or type it" voice banner, an amount hero, a title field, then a list of picker rows — Date (with date picker), Category, Payment mode, Paid by, Split with a group — each opening a bottom sheet; a note under "Other details". Group splits (equal or custom) appear inline when a group is chosen.
- **Payment modes**: Cash, UPI, Card, Bank transfer — stored per expense and included in the CSV export.
- **Voice entry**: tap the banner or the Home microphone, say e.g. "250 groceries by UPI" or "paid 120 for pani puri cash"; the amount, category (built-in keywords in English/Hindi/Gujarati words such as sabzi, kiraya, dawa, plus your custom category names), payment mode and title are filled in for you to review and save. Uses the phone's speech recogniser in the app language.
- **Custom categories** (Expenses → tune icon, or Me → Categories): add your own with a name, one of 26 icons and 15 colours; built-in categories can be recoloured/re-iconed; deleting a custom category moves its expenses to Other.
- Titles, notes, names and reminder titles are saved with a capital first letter; text fields default to sentence capitalisation.
- Tap any expense to edit or delete it. Every new expense earns points (see Rewards).

## Budgets & alerts
- Budgets screen: a hero card with the budget ring, spent / left this month and a "Set monthly budget" button; each category is a card with icon, spent of limit and a coloured progress bar — tap it to set or remove a limit in a sheet with quick-amount chips.
- Push notifications when a budget crosses 80% and 100% (can be switched off in Me → Notifications).
- Finishing a month under the overall budget earns +100 points and the Budget Boss badge.

## Groups & splitting
- Create groups (home, trip, party…) from your family members with an emoji.
- Add an expense to a group and split it equally among chosen members or with custom amounts; the form shows live per-person shares and whether custom amounts add up.
- Group page shows total shared, who owes whom, a minimal "settle up" plan, and all shared expenses. "Mark paid" records a settlement and earns points.
- Share button sends a localised text summary of balances to WhatsApp, SMS, email, etc.

## Family members
- Add partner, kids, parents, roommates or friends with a relation label; each has their own avatar (tap to open the studio for them).
- Removing a member cleans up their expenses and splits.

## Avatar studio
- Skin tone (6) and hair colour (7) swatches, always free.
- Hair (9), outfits (12) and accessories (6): short/buzz/long/curly hair, tee and shirt are free; the rest unlock with points directly from the studio.
- Live preview on your own avatar, a "Say hi" wave and a chip for each of the ten moods.

## Rewards & gamification
**Earning points**
- +10 per expense logged, +5 for the first log of the day, +2 per streak day (max +20)
- +30 create a group, +15 settle a balance, +20 add a family member
- +100 finish a month under budget
- Bonus points for every badge earned

**Levels** — XP-based (Rookie → Saver → Planner → Tracker → Strategist → Accountant → Treasurer → Guardian → Master → Legend). Level-ups trigger confetti, an in-app toast and a notification.

**Badges (11)** — one horizontally scrolling row, earned first then locked ones with a progress bar. Tap any badge for an animated detail card with a numbered "How to earn it" list.

**Shop** — spend points on 6 colour themes, avatar outfits, hair styles and accessories, and two features (Advanced Insights, CSV export). Points history is listed at the bottom of Rewards. Rewards opens from the Home pill or Me → Rewards.

## Reminders & bills
- Repeat Once / Daily / Weekly (weekday) / Monthly (day of month) / Yearly (month + day); quick presets for rent, EMI, electricity, insurance, school fees and a daily nudge.
- Time is chosen on a scrolling **time wheel** (hour · minute · AM/PM) with the selected row highlighted — no more stock clock dialog.
- Optional amount and category; cards show the schedule, "Due today / tomorrow / in N days" and the amount, sorted by next due date.
- Notifications are delivered by exact alarms, so they arrive on time even when the app is closed or the phone is idle, and are re-registered after a reboot or time change.
- Notification copy is playful and varies: random titles such as "Your wallet called 📞" or "Streak check 🔥", bill reminders with the amount, budget warnings like "Whoa there, big spender 😅", badge and level-up cheers — all localised.

## Me tab & settings
- Profile header with avatar, level, points, streak; Edit profile (name, mobile, role, PIN) and Avatar studio.
- Household: Rewards, Family members, Categories, Budgets, Reminders & bills, Insights, CSV export (when unlocked).
- Security: App lock. Language: English / Hindi / Gujarati. Appearance: System / Light / Dark. Currency: ₹ INR, $ USD, € EUR.
- Notifications: three independent toggles.
- **Test data**: a "Sample data" switch loads 45 days of demo expenses across all categories and payment modes, two extra members (Priya, Arjun), a "Flat 4B" group with shared bills and three monthly reminders, so every screen can be reviewed with realistic content; switching it off removes exactly those rows and nothing else (points are not affected).
- Account: Log out / switch. All data stays on the device and is included in Android backup.
