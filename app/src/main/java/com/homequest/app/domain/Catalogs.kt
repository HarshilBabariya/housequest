package com.homequest.app.domain

data class RewardItem(
    val id: String,
    val kind: RewardKind,
    val name: String,
    val description: String,
    val cost: Int,
    val emoji: String
) {
    val isFree: Boolean get() = cost == 0
}

object RewardCatalog {
    val items: List<RewardItem> = listOf(
        RewardItem(ThemeIds.SUNRISE, RewardKind.THEME, "Sunrise", "Warm coral & gold", 0, "🌅"),
        RewardItem(ThemeIds.OCEAN, RewardKind.THEME, "Ocean", "Cool teal & navy", 200, "🌊"),
        RewardItem(ThemeIds.FOREST, RewardKind.THEME, "Forest", "Deep greens & moss", 250, "🌲"),
        RewardItem(ThemeIds.MIDNIGHT, RewardKind.THEME, "Midnight", "Indigo neon glow", 300, "🌙"),
        RewardItem(ThemeIds.CANDY, RewardKind.THEME, "Candy", "Pink & lavender pop", 350, "🍬"),
        RewardItem(ThemeIds.ROYAL, RewardKind.THEME, "Royal", "Purple & gold luxe", 500, "👑"),

        RewardItem(Outfits.TEE, RewardKind.OUTFIT, "Casual tee", "Everyday comfort", 0, "👕"),
        RewardItem(Outfits.HOODIE, RewardKind.OUTFIT, "Hoodie", "Cosy weekend look", 150, "🧥"),
        RewardItem(Outfits.KURTA, RewardKind.OUTFIT, "Kurta", "Festive traditional wear", 250, "🪔"),
        RewardItem(Outfits.BLAZER, RewardKind.OUTFIT, "Blazer", "Sharp and professional", 250, "🤵"),
        RewardItem(Outfits.SPACESUIT, RewardKind.OUTFIT, "Spacesuit", "Budget to the moon", 400, "🚀"),
        RewardItem(Outfits.HERO, RewardKind.OUTFIT, "Hero suit", "Savings superhero", 500, "🦸"),

        RewardItem(HairStyles.SHORT, RewardKind.HAIR, "Short", "Classic crop", 0, "💇"),
        RewardItem(HairStyles.LONG, RewardKind.HAIR, "Long", "Flowing locks", 0, "💁"),
        RewardItem(HairStyles.CURLY, RewardKind.HAIR, "Curly", "Bouncy curls", 0, "🌀"),
        RewardItem(HairStyles.BUN, RewardKind.HAIR, "Top bun", "Neat and tidy", 120, "🎀"),
        RewardItem(HairStyles.MOHAWK, RewardKind.HAIR, "Mohawk", "Bold statement", 180, "🎸"),

        RewardItem(Accessories.NONE, RewardKind.ACCESSORY, "None", "Keep it simple", 0, "✨"),
        RewardItem(Accessories.GLASSES, RewardKind.ACCESSORY, "Glasses", "Smart saver", 100, "👓"),
        RewardItem(Accessories.CAP, RewardKind.ACCESSORY, "Cap", "Street style", 150, "🧢"),
        RewardItem(Accessories.HEADPHONES, RewardKind.ACCESSORY, "Headphones", "Tune out overspending", 200, "🎧"),
        RewardItem(Accessories.CROWN, RewardKind.ACCESSORY, "Crown", "Ruler of the budget", 400, "👑"),

        RewardItem(Features.INSIGHTS, RewardKind.FEATURE, "Advanced insights", "Category trends over 6 months", 300, "📈"),
        RewardItem(Features.EXPORT_CSV, RewardKind.FEATURE, "CSV export", "Share your ledger as a spreadsheet", 250, "📤")
    )

    fun defaults(): List<String> = items.filter { it.isFree }.map { it.id }

    fun ofKind(kind: RewardKind): List<RewardItem> = items.filter { it.kind == kind }
}

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val bonusPoints: Int
)

object BadgeCatalog {
    const val FIRST_STEP = "badge_first_step"
    const val TEN_LOGGED = "badge_ten_logged"
    const val CENTURY = "badge_century"
    const val WEEK_WARRIOR = "badge_week_warrior"
    const val MONTH_MASTER = "badge_month_master"
    const val TEAM_PLAYER = "badge_team_player"
    const val FAIR_SHARE = "badge_fair_share"
    const val FAMILY_FIRST = "badge_family_first"
    const val BUDGET_BOSS = "badge_budget_boss"
    const val STYLIST = "badge_stylist"
    const val COLLECTOR = "badge_collector"

    val badges: List<Badge> = listOf(
        Badge(FIRST_STEP, "First step", "Log your first expense", "🥇", 20),
        Badge(TEN_LOGGED, "Getting serious", "Log 10 expenses", "🔟", 40),
        Badge(CENTURY, "Century", "Log 100 expenses", "💯", 150),
        Badge(WEEK_WARRIOR, "Week warrior", "7-day logging streak", "🔥", 60),
        Badge(MONTH_MASTER, "Month master", "30-day logging streak", "🏆", 250),
        Badge(TEAM_PLAYER, "Team player", "Create your first group", "🤝", 30),
        Badge(FAIR_SHARE, "Fair share", "Settle up a group balance", "⚖️", 40),
        Badge(FAMILY_FIRST, "Family first", "Add 3 family members", "👨‍👩‍👧", 50),
        Badge(BUDGET_BOSS, "Budget boss", "Finish a month under budget", "🎯", 200),
        Badge(STYLIST, "Stylist", "Unlock your first outfit", "🧵", 30),
        Badge(COLLECTOR, "Collector", "Unlock 5 rewards", "🗂️", 80)
    )

    private val byId = badges.associateBy { it.id }

    fun find(id: String): Badge? = byId[id]
}
