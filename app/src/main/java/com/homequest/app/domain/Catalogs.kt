package com.homequest.app.domain

import androidx.annotation.StringRes
import com.homequest.app.R

data class RewardItem(
    val id: String,
    val kind: RewardKind,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val cost: Int,
    val emoji: String
) {
    val isFree: Boolean get() = cost == 0
}

object RewardCatalog {
    val items: List<RewardItem> = listOf(
        RewardItem(ThemeIds.SUNRISE, RewardKind.THEME, R.string.rw_sunrise, R.string.rw_sunrise_d, 0, "🌅"),
        RewardItem(ThemeIds.OCEAN, RewardKind.THEME, R.string.rw_ocean, R.string.rw_ocean_d, 200, "🌊"),
        RewardItem(ThemeIds.FOREST, RewardKind.THEME, R.string.rw_forest, R.string.rw_forest_d, 250, "🌲"),
        RewardItem(ThemeIds.MIDNIGHT, RewardKind.THEME, R.string.rw_midnight, R.string.rw_midnight_d, 300, "🌙"),
        RewardItem(ThemeIds.CANDY, RewardKind.THEME, R.string.rw_candy, R.string.rw_candy_d, 350, "🍬"),
        RewardItem(ThemeIds.ROYAL, RewardKind.THEME, R.string.rw_royal, R.string.rw_royal_d, 500, "👑"),

        RewardItem(Outfits.TEE, RewardKind.OUTFIT, R.string.rw_tee, R.string.rw_tee_d, 0, "👕"),
        RewardItem(Outfits.SHIRT, RewardKind.OUTFIT, R.string.rw_shirt, R.string.rw_shirt_d, 0, "👔"),
        RewardItem(Outfits.HOODIE, RewardKind.OUTFIT, R.string.rw_hoodie, R.string.rw_hoodie_d, 150, "🧥"),
        RewardItem(Outfits.SWEATER, RewardKind.OUTFIT, R.string.rw_sweater, R.string.rw_sweater_d, 150, "🧶"),
        RewardItem(Outfits.DENIM, RewardKind.OUTFIT, R.string.rw_denim, R.string.rw_denim_d, 200, "🧢"),
        RewardItem(Outfits.KURTA, RewardKind.OUTFIT, R.string.rw_kurta, R.string.rw_kurta_d, 250, "🪔"),
        RewardItem(Outfits.DRESS, RewardKind.OUTFIT, R.string.rw_dress, R.string.rw_dress_d, 250, "👗"),
        RewardItem(Outfits.BLAZER, RewardKind.OUTFIT, R.string.rw_blazer, R.string.rw_blazer_d, 250, "🤵"),
        RewardItem(Outfits.SAREE, RewardKind.OUTFIT, R.string.rw_saree, R.string.rw_saree_d, 300, "🥻"),
        RewardItem(Outfits.SHERWANI, RewardKind.OUTFIT, R.string.rw_sherwani, R.string.rw_sherwani_d, 300, "👘"),
        RewardItem(Outfits.SPACESUIT, RewardKind.OUTFIT, R.string.rw_spacesuit, R.string.rw_spacesuit_d, 400, "🚀"),
        RewardItem(Outfits.HERO, RewardKind.OUTFIT, R.string.rw_hero, R.string.rw_hero_d, 500, "🦸"),

        RewardItem(HairStyles.SHORT, RewardKind.HAIR, R.string.rw_short, R.string.rw_short_d, 0, "💇"),
        RewardItem(HairStyles.BUZZ, RewardKind.HAIR, R.string.rw_buzz, R.string.rw_buzz_d, 0, "✂️"),
        RewardItem(HairStyles.LONG, RewardKind.HAIR, R.string.rw_long, R.string.rw_long_d, 0, "💁"),
        RewardItem(HairStyles.CURLY, RewardKind.HAIR, R.string.rw_curly, R.string.rw_curly_d, 0, "🌀"),
        RewardItem(HairStyles.WAVY, RewardKind.HAIR, R.string.rw_wavy, R.string.rw_wavy_d, 100, "🌊"),
        RewardItem(HairStyles.BOB, RewardKind.HAIR, R.string.rw_bob, R.string.rw_bob_d, 100, "💈"),
        RewardItem(HairStyles.PONYTAIL, RewardKind.HAIR, R.string.rw_ponytail, R.string.rw_ponytail_d, 120, "🎀"),
        RewardItem(HairStyles.BUN, RewardKind.HAIR, R.string.rw_bun, R.string.rw_bun_d, 120, "🍡"),
        RewardItem(HairStyles.BRAIDS, RewardKind.HAIR, R.string.rw_braids, R.string.rw_braids_d, 150, "🪢"),
        RewardItem(HairStyles.MOHAWK, RewardKind.HAIR, R.string.rw_mohawk, R.string.rw_mohawk_d, 180, "🎸"),

        RewardItem(Accessories.NONE, RewardKind.ACCESSORY, R.string.rw_none, R.string.rw_none_d, 0, "✨"),
        RewardItem(Accessories.BINDI, RewardKind.ACCESSORY, R.string.rw_bindi, R.string.rw_bindi_d, 50, "🔴"),
        RewardItem(Accessories.EARRINGS, RewardKind.ACCESSORY, R.string.rw_earrings, R.string.rw_earrings_d, 80, "💛"),
        RewardItem(Accessories.GLASSES, RewardKind.ACCESSORY, R.string.rw_glasses, R.string.rw_glasses_d, 100, "👓"),
        RewardItem(Accessories.CAP, RewardKind.ACCESSORY, R.string.rw_cap, R.string.rw_cap_d, 150, "🧢"),
        RewardItem(Accessories.HEADPHONES, RewardKind.ACCESSORY, R.string.rw_headphones, R.string.rw_headphones_d, 200, "🎧"),
        RewardItem(Accessories.CROWN, RewardKind.ACCESSORY, R.string.rw_crown, R.string.rw_crown_d, 400, "👑"),

        RewardItem(Features.INSIGHTS, RewardKind.FEATURE, R.string.rw_insights, R.string.rw_insights_d, 300, "📈"),
        RewardItem(Features.EXPORT_CSV, RewardKind.FEATURE, R.string.rw_export, R.string.rw_export_d, 250, "📤")
    )

    private val byId = items.associateBy { it.id }

    fun defaults(): List<String> = items.filter { it.isFree }.map { it.id }

    fun ofKind(kind: RewardKind): List<RewardItem> = items.filter { it.kind == kind }

    fun find(id: String): RewardItem? = byId[id]
}

data class Badge(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val emoji: String,
    val bonusPoints: Int,
    val target: Int,
    @StringRes val unitRes: Int,
    val howTo: List<Int>
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
        Badge(FIRST_STEP, R.string.b_first_step, R.string.b_first_step_d, "🥇", 20, 1, R.string.unit_expense,
            listOf(R.string.b_first_step_1, R.string.b_first_step_2, R.string.b_first_step_3)),
        Badge(TEN_LOGGED, R.string.b_ten, R.string.b_ten_d, "🔟", 40, 10, R.string.unit_expenses,
            listOf(R.string.b_ten_1, R.string.b_ten_2, R.string.b_ten_3)),
        Badge(CENTURY, R.string.b_century, R.string.b_century_d, "💯", 150, 100, R.string.unit_expenses,
            listOf(R.string.b_century_1, R.string.b_century_2, R.string.b_century_3)),
        Badge(WEEK_WARRIOR, R.string.b_week, R.string.b_week_d, "🔥", 60, 7, R.string.unit_days,
            listOf(R.string.b_week_1, R.string.b_week_2, R.string.b_week_3)),
        Badge(MONTH_MASTER, R.string.b_month, R.string.b_month_d, "🏆", 250, 30, R.string.unit_days,
            listOf(R.string.b_month_1, R.string.b_month_2, R.string.b_month_3)),
        Badge(TEAM_PLAYER, R.string.b_team, R.string.b_team_d, "🤝", 30, 1, R.string.unit_group,
            listOf(R.string.b_team_1, R.string.b_team_2, R.string.b_team_3)),
        Badge(FAIR_SHARE, R.string.b_fair, R.string.b_fair_d, "⚖️", 40, 1, R.string.unit_settlement,
            listOf(R.string.b_fair_1, R.string.b_fair_2, R.string.b_fair_3)),
        Badge(FAMILY_FIRST, R.string.b_family, R.string.b_family_d, "👨‍👩‍👧", 50, 3, R.string.unit_members,
            listOf(R.string.b_family_1, R.string.b_family_2, R.string.b_family_3)),
        Badge(BUDGET_BOSS, R.string.b_budget, R.string.b_budget_d, "🎯", 200, 1, R.string.unit_month,
            listOf(R.string.b_budget_1, R.string.b_budget_2, R.string.b_budget_3)),
        Badge(STYLIST, R.string.b_stylist, R.string.b_stylist_d, "🧵", 30, 1, R.string.unit_outfit,
            listOf(R.string.b_stylist_1, R.string.b_stylist_2, R.string.b_stylist_3)),
        Badge(COLLECTOR, R.string.b_collector, R.string.b_collector_d, "🗂️", 80, 5, R.string.unit_rewards,
            listOf(R.string.b_collector_1, R.string.b_collector_2, R.string.b_collector_3))
    )

    private val byId = badges.associateBy { it.id }

    fun find(id: String): Badge? = byId[id]
}
