# HomeQuest — Features

## Look & feel
- Adaptive launcher icon: coral-to-pink gradient, white house with a gold star coin; round, squircle and Android 13+ themed (monochrome) variants are all supported.
- Material 3 throughout; each of the six themes defines its own surface, card and chip colours so the whole UI (not just the accent) changes when you apply one.
- Material icons for navigation, categories and actions; emoji are reserved for badges, rewards and group avatars.

## First launch & accounts
- A three-step onboarding (You → Money → PIN) asks for your name and mobile number (required, 10+ digits), currency and optional budget, then lets you create a 4-digit PIN or skip it. You become the household owner and start at Level 1 (Rookie).
- Every family member is an account: name, relation, mobile number (required), avatar and an optional 4-digit PIN. Weak PINs (1111, 1234…) are rejected and the PIN is confirmed twice.
- Note: accounts live on this phone only. Cross-device access needs a sync backend, which is not part of this release.
- Member picker: on launch (or after Log out) you pick your avatar; members with a PIN get a keypad. One lone member without a PIN is signed in automatically.
- App lock (Me → Security): switching it on creates a PIN on the spot if you don't have one, then asks for it every time the app opens.
- Log out / switch from the Me tab returns to the picker. Expenses, budgets and points are shared by the whole household — only avatar and greeting are per member.

## Your avatar
- Bitmoji-style illustration: egg-shaped head with a thin dark outline, large eyes with lash line and two-tone iris, bold shaped brows, small nose, wide toothy smile, rounded blush.
- 9 hair styles (short, buzz, long, wavy, bob, ponytail, curly, top bun, braids, mohawk), 7 hair colours, 6 skin tones.
- 12 outfits: casual tee, shirt, hoodie, denim jacket, sweater, blazer, kurta, sherwani, saree, dress, spacesuit, hero suit.
- 6 accessories: bindi, earrings, glasses, cap, headphones, crown.
- Animated: breathes, blinks at random, sways, and waves when you open Home (tap it in the studio for another wave).
- Moods react to the monthly budget on Home: happy under 60%, neutral to 85%, worried up to 100% (sweat drop), angry when over (furrowed brows, red cheeks, jitter).
- A speech bubble greets you by time of day and comments on spending.
- New family members get a randomised look (skin tone, hair colour, short/buzz/long/curly hair, tee or shirt); those parts are free so households look varied from day one.

## Home dashboard
- Gradient hero card with your animated avatar, level, XP progress bar, spendable points, streak and the mood bubble.
- Stat tiles: Today, vs last month, Group balance (owed / you owe, tap to open Groups), Top category.
- "Who paid this month" bars per family member and "Upcoming bills" (next three with amounts and due-in-N-days).
- Monthly budget ring that fills as you spend; turns red when over.
- Animated 7-day bar chart and a category donut for the current month.
- Five most recent expenses, with a shortcut to the full list.
- "Insights" shortcut appears once the Advanced Insights feature is unlocked.

## Expenses
- The list opens with This month / vs last month tiles, a search field and category filters; entries are grouped by day with subtotals.
- Add expense: a large amount hero, then Details, a 5-column category icon grid, "Paid by" avatar chips, Today / Yesterday / Pick-a-date, and an optional group split (equal or custom) — all as card sections.
- Search by title/note and filter by category; list is grouped by day with daily subtotals.
- Tap any expense to edit or delete it.
- Every new expense earns points (see Rewards).

## Budgets & alerts
- One overall monthly budget plus optional per-category limits.
- Progress bars per category for the current month.
- Push notifications when a budget crosses 80% and 100% (can be switched off in Me → Notifications).
- Finishing a month under the overall budget earns +100 points and the Budget Boss badge.

## Groups & splitting
- Create groups (home, trip, party…) from your family members with an emoji.
- Add an expense to a group and split it equally among chosen members or with custom amounts; the form shows live per-person shares and whether custom amounts add up.
- Group page shows total shared, who owes whom, a minimal "settle up" plan, and all shared expenses.
- "Mark paid" records a settlement and earns points.
- Share button sends a text summary of balances to WhatsApp, SMS, email, etc.

## Family members
- Add partner, kids, parents, roommates or friends with a relation label.
- Each member has their own avatar; tap it to open the avatar studio for that member.
- Removing a member cleans up their expenses and splits.

## Avatar studio
- Skin tone (6) and hair colour (7) swatches, always free.
- Hair (9), outfits (12) and accessories (6): short/buzz/long/curly hair, tee and shirt are free; the rest unlock with points directly from the studio.
- Live preview of every option on your own avatar; the avatar gently bobs on the dashboard and profile.

## Rewards & gamification
**Earning points**
- +10 per expense logged, +5 for the first log of the day, +2 per streak day (max +20)
- +30 create a group, +15 settle a balance, +20 add a family member
- +100 finish a month under budget
- Bonus points for every badge earned

**Levels** — XP-based (Rookie → Saver → Planner → Tracker → Strategist → Accountant → Treasurer → Guardian → Master → Legend). Level-ups trigger confetti, an in-app toast and a notification.

**Badges (11)** — one horizontally scrolling row, earned first (newest at top, gradient ring, bonus points) then locked ones with a progress bar (e.g. 6/7 days). Tap any badge for an animated detail card: emblem pops in (spinning gradient ring when earned), progress, reward points, earned date, and a numbered "How to earn it" list. First step, Getting serious, Century, Week warrior, Month master, Team player, Fair share, Family first, Budget boss, Stylist, Collector.

**Shop** — spend points on:
- 6 colour themes (Sunrise free; Ocean, Forest, Midnight, Candy, Royal). Applying a theme animates the whole UI's colours.
- Avatar outfits, hair styles and accessories.
- Features: Advanced Insights (6-month stacked category trend chart with highlights) and CSV export.

Points history is listed at the bottom of Rewards.

## Reminders & bills
- Repeat Once / Daily / Weekly (weekday) / Monthly (day of month) / Yearly (month + day) — made for rent, EMIs, utilities, insurance and school fees; quick presets fill these in.
- Optional amount and category; cards show the schedule, "Due today / tomorrow / in N days" and the amount, sorted by next due date.
- Toggle individual reminders on/off; the notification shows the amount due.
- Separate notification channels and in-app toggles for reminders & bills, budget alerts and rewards (Me → Notifications).

## Me tab & settings
- Profile header with animated avatar, level, points, streak; Edit profile (name, mobile, role, PIN) and Avatar studio.
- Household: family members, budgets, reminders & bills, insights, CSV export (when unlocked).
- Security: App lock toggle. Appearance: System / Light / Dark. Currency: scrollable list (INR, USD, EUR, GBP, AED, JPY, AUD, CAD, SGD).
- Notifications: three independent toggles. Account: Log out / switch.
- All data stays on the device and is included in Android backup.
