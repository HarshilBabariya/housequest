export const REPO_URL = 'https://github.com/HarshilBabariya/housequest'
export const APP_VERSION = __APP_VERSION__

export const downloads = {
  full: { label: 'Full', file: 'HomeQuest-full.apk', url: `${import.meta.env.BASE_URL}downloads/HomeQuest-full.apk` },
  lite: { label: 'Lite', file: 'HomeQuest-lite.apk', url: `${import.meta.env.BASE_URL}downloads/HomeQuest-lite.apk` }
}

export const stats = [
  { value: 33, label: 'badges to earn' },
  { value: 6130, label: 'bonus points on offer' },
  { value: 16, label: 'themes, 9 of them festive' },
  { value: 18, label: 'avatar outfits' }
]

export const features = [
  {
    key: 'log',
    emoji: '⚡',
    title: 'Log in seconds',
    body: 'Centre + button, amount hero, Yesterday / Today / Tomorrow chips, inline Paid by and Cash · UPI · Card · Bank tiles. Credits (cashback, refunds) subtract from every total.'
  },
  {
    key: 'voice',
    emoji: '🎙️',
    title: 'Say it out loud',
    body: '“250 groceries by UPI” or “paid 120 for pani puri cash” — amount, category, mode and note are filled for you, in English, Hindi or Gujarati.'
  },
  {
    key: 'capture',
    emoji: '📩',
    title: 'Auto-capture payments',
    body: 'The Full build reads bank and UPI SMS notifications (text messages only) and turns debits and credits into one-tap drafts. Offers and OTPs are ignored.'
  },
  {
    key: 'budgets',
    emoji: '🎯',
    title: 'Budgets & forecast',
    body: 'Monthly and per-category limits, 80% / 100% alerts, and a plain-words forecast: “At this pace you’ll overspend by ₹4,200 around 24 Nov”.'
  },
  {
    key: 'groups',
    emoji: '🤝',
    title: 'Groups & splitting',
    body: 'Home, trip, party — split equally or with custom amounts, see who owes whom, settle up with a minimal plan and share the summary to WhatsApp.'
  },
  {
    key: 'family',
    emoji: '👨‍👩‍👧',
    title: 'Family accounts',
    body: 'Every member has a profile, mobile number, optional PIN and their own avatar. App lock keeps the ledger private; everyone shares one household ledger.'
  },
  {
    key: 'insights',
    emoji: '📈',
    title: 'Insights & calendar',
    body: 'Week / Month / Year trends, category donut with ▲▼ change, payment-mode split, who paid, and a calendar that colours each day green or red against your daily budget.'
  },
  {
    key: 'khata',
    emoji: '🏪',
    title: 'Kirana khata & services',
    body: 'Shop credit tallies, milk / maid / newspaper month grids with the maths spelt out, gold and chit funds valued at today’s rate.'
  },
  {
    key: 'reminders',
    emoji: '🔔',
    title: 'Reminders & bills',
    body: 'An animated 30-day orbit of what’s due, exact-time alarms, and bills that log themselves on the due date. Tap a notification and it opens the right screen.'
  },
  {
    key: 'rewards',
    emoji: '🏆',
    title: 'Points, levels & badges',
    body: '+10 per expense, streak bonuses, 10 levels from Rookie to Legend and 33 badges with full-screen celebrations and a cash-register ka-ching.'
  },
  {
    key: 'avatar',
    emoji: '🧑‍🎨',
    title: 'Your avatar',
    body: '16 hairstyles, 18 outfits, 13 accessories, 10 moods that react to your budget and streak, and seven actions — wave, jump, spin, dance, wink, cheer, laugh.'
  },
  {
    key: 'sounds',
    emoji: '🔊',
    title: 'Sounds of a home',
    body: 'A cooker seeti for the daily nudge, a doorbell for bills, a debit ting for money out, microwave beeps at 80% of a budget, and a POS buzz when you cross it.'
  },
  {
    key: 'data',
    emoji: '🔒',
    title: 'Local-first, yours',
    body: 'No account, no server, no tracking. Everything lives in the phone’s database and Android backup. Import and export CSV, export a PDF, or erase it all in one tap.'
  },
  {
    key: 'share',
    emoji: '🖼️',
    title: 'Weekly share card',
    body: 'A 1080×1350 card in your theme colours — week total, ▲▼ vs last week, top three categories, who paid, streak and points — made for the family group.'
  },
  {
    key: 'categories',
    emoji: '🏷️',
    title: 'Categories your way',
    body: 'Add your own with one of 26 icons, 40 preset colours or a custom hue; recolour the built-in ones. Voice entry learns your custom names too.'
  },
  {
    key: 'languages',
    emoji: '🗣️',
    title: 'English · हिन्दी · ગુજરાતી',
    body: 'Switch from onboarding or Me → Language. Dates, badges, rewards, mood lines and every notification follow — and the Polyglot badge lands when you do.'
  }
]

export const festivals = [
  { key: 'uttarayan', emoji: '🪁', name: 'Uttarayan', when: '13–15 Jan', effect: 'A kite climbs across the sky with a fluttering tail', sound: 'Kite flutter and whistles', outfit: 'Kite-flyer tee', look: { name: 'Kite Sky', c: ['#0284C7', '#FACC15', '#EF4444', '#0F2031'] } },
  { key: 'tiranga', emoji: '🇮🇳', name: 'Republic & Independence Day', when: '26 Jan · 15 Aug', effect: 'A shower of tricolour confetti', sound: 'Bugle fanfare', outfit: 'Tiranga jacket', look: { name: 'Tiranga', c: ['#F26B0F', '#138808', '#1E3A8A', '#181E1A'] } },
  { key: 'holi', emoji: '🎨', name: 'Holi', when: 'March', effect: 'Water balloons fly up and burst into colour', sound: 'Dhol, pichkari and a water-balloon splash', outfit: 'Holi white kurta + gulal face', look: { name: 'Holi Rang', c: ['#E91E8C', '#FFC400', '#00B8D4', '#2A1224'] } },
  { key: 'rakhi', emoji: '🎀', name: 'Raksha Bandhan', when: 'August', effect: 'Rose petals drift down past swinging temple bells', sound: 'Temple bell and santoor plucks', outfit: 'Rakhi pastel kurta', look: { name: 'Rakhi Pastel', c: ['#DB2777', '#F59E0B', '#FB923C', '#2B1720'] } },
  { key: 'ganesh', emoji: '🌺', name: 'Ganesh Chaturthi', when: '10 days, Aug–Sep', effect: 'Marigold petals shower down past swinging temple bells', sound: 'Dhol-tasha and bell', outfit: 'Bappa kurta', look: { name: 'Bappa Saffron', c: ['#EA580C', '#DC2626', '#EAB308', '#2B190C'] } },
  { key: 'navratri', emoji: '💃', name: 'Navratri', when: '9 nights, Sep–Oct', effect: 'Colour lights fall past swinging garba lanterns', sound: 'Garba dhol with dandiya clicks', outfit: 'Garba kediyu', look: { name: 'Garba Nights', c: ['#D9271C', '#FF9F1C', '#16A34A', '#291512'] } },
  { key: 'diwali', emoji: '🪔', name: 'Diwali', when: 'Dhanteras → Bhai Dooj', effect: 'A pataka rocket that bursts into fireworks', sound: 'Anaar, rocket and a string of firecrackers', outfit: 'Diwali silk kurta', look: { name: 'Diwali Glow', c: ['#B4231F', '#F5B301', '#FF7A1A', '#2A130C'] } },
  { key: 'christmas', emoji: '🎄', name: 'Christmas', when: '24–26 Dec', effect: 'Snowfall with swinging jingle bells', sound: 'Sleigh bells and chimes', outfit: 'Santa suit + Santa hat', look: { name: 'Jingle Red', c: ['#C62828', '#2E7D32', '#D4A017', '#16201A'] } },
  { key: 'newyear', emoji: '🎆', name: 'New Year', when: '31 Dec – 1 Jan', effect: 'A rocket that bursts into fireworks', sound: 'Countdown beeps, popper and party horn', outfit: 'Sequin party jacket + party hat', look: { name: 'New Year Gold', c: ['#7C3AED', '#F5C451', '#E879F9', '#17143A'] } }
]

export const festivalPerks = [
  'Themes, decorations and the festival app icon switch on 3 days before the festival and stay for 3 days after',
  'A full-screen celebration on first open, with the festival’s own sound and your avatar already dressed up',
  'Tap the festival banner on Home any time to replay the celebration — a pataka rocket, colour balloons, a kite, tricolour confetti, petal showers or snow',
  'Claim my gifts: the pack, +50 ⭐ and the outfit — free during the window, yours for good',
  'Hanging decor on Home — garlands and diyas, torans, kites, bunting, fairy lights, stars'
]

export const points = [
  ['+10', 'per expense logged'],
  ['+5', 'first log of the day'],
  ['+2 / day', 'streak bonus, up to +20'],
  ['+30', 'create a group'],
  ['+15', 'settle a balance'],
  ['+20', 'add a family member'],
  ['+100', 'finish a month under budget'],
  ['+50', 'claim a festival pack']
]

export const levels = ['Rookie', 'Saver', 'Planner', 'Tracker', 'Strategist', 'Accountant', 'Treasurer', 'Guardian', 'Master', 'Legend']

export const badges = ['🥇 First step', '🔟 Getting serious', '💯 Century', '🚀 Half a thousand', '🔥 Week warrior', '🏆 Month master', '⚡ Iron streak', '🐉 Legendary streak', '🌅 Early bird', '🦉 Night owl', '🎙️ Say it out loud', '📝 Note taker', '🧭 Explorer', '🏷️ Make it yours', '💳 Payment pro', '💎 Big spender', '🪙 Penny counter', '🤝 Team player', '🧑‍🤝‍🧑 Group guru', '⚖️ Fair share', '🏦 Settler', '👨‍👩‍👧 Family first', '🏠 Full house', '📅 Bill planner', '🎯 Budget boss', '🥉 Budget hat-trick', '🧵 Stylist', '💍 Accessorized', '🎨 New look', '🗂️ Collector', '🗣️ Polyglot', '🎖️ Rising star', '👑 Legend']

export const builds = [
  {
    key: 'full',
    title: 'Full',
    tagline: 'Everything, including auto-capture of bank and UPI text messages.',
    rows: ['Auto-capture payment drafts from SMS notifications', 'All features of Lite', 'Declares a notification listener, so Play Protect blocks a browser / WhatsApp install in India', 'Install over USB: adb install -r HomeQuest-full.apk']
  },
  {
    key: 'lite',
    title: 'Lite',
    tagline: 'The same app without auto-capture — installs from anywhere.',
    rows: ['Every feature except SMS auto-capture', 'Installs straight from this page, WhatsApp or a file manager', 'Same app id: install either over the other and keep your data', 'Best choice if you don’t use a computer']
  }
]

export const installSteps = [
  ['Download', 'Tap Full or Lite above. The APK is the latest build — this page is republished only after a successful build.'],
  ['Allow the install', 'Open the file; Android asks once to allow installs from your browser or file manager. Updates install over the previous build — only a build older than 1.9.3 has to be uninstalled first, because those were signed with a throwaway key.'],
  ['Full build only', 'Play Protect blocks it from a browser. Copy it to a computer and run adb install -r HomeQuest-full.apk, then grant notification access on Me → Capture.'],
  ['Set up', 'Pick a language, your name and mobile, a currency and an optional budget — two screens. A daily 8:30 PM nudge is ready for you; App lock is a switch on Me whenever you want it.']
]

export const languages = ['English', 'हिन्दी', 'ગુજરાતી']
