package com.homequest.app.domain

import androidx.annotation.StringRes
import com.homequest.app.R

data class RewardItem(
    val id: String,
    val kind: RewardKind,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val cost: Int,
    val emoji: String,
    val pack: FestivalPack? = null
) {
    val isFree: Boolean get() = cost == 0

    fun claimableDuring(active: FestivalPack?): Boolean = pack != null && pack == active
}

data class PointPack(val id: String, @StringRes val nameRes: Int, val rupees: Int, val points: Int)

object PointPacks {
    val packs = listOf(
        PointPack("pack_s", R.string.plan_starter, 49, 500),
        PointPack("pack_m", R.string.plan_plus, 99, 1200),
        PointPack("pack_l", R.string.plan_pro, 199, 3000)
    )
    fun find(id: String): PointPack? = packs.firstOrNull { it.id == id }
}

object RewardCatalog {
    val items: List<RewardItem> = listOf(
        RewardItem(ThemeIds.SUNRISE, RewardKind.THEME, R.string.rw_sunrise, R.string.rw_sunrise_d, 0, "🌅"),
        RewardItem(ThemeIds.OCEAN, RewardKind.THEME, R.string.rw_ocean, R.string.rw_ocean_d, 200, "🌊"),
        RewardItem(ThemeIds.FOREST, RewardKind.THEME, R.string.rw_forest, R.string.rw_forest_d, 250, "🌲"),
        RewardItem(ThemeIds.MIDNIGHT, RewardKind.THEME, R.string.rw_midnight, R.string.rw_midnight_d, 300, "🌙"),
        RewardItem(ThemeIds.CANDY, RewardKind.THEME, R.string.rw_candy, R.string.rw_candy_d, 350, "🍬"),
        RewardItem(ThemeIds.ROYAL, RewardKind.THEME, R.string.rw_royal, R.string.rw_royal_d, 500, "👑"),
        RewardItem(ThemeIds.GLASS, RewardKind.THEME, R.string.rw_glass, R.string.rw_glass_d, 600, "🫧"),

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
        RewardItem(Outfits.POLO, RewardKind.OUTFIT, R.string.rw_polo, R.string.rw_polo_d, 0, "🎽"),
        RewardItem(Outfits.JERSEY, RewardKind.OUTFIT, R.string.rw_jersey, R.string.rw_jersey_d, 180, "🏏"),
        RewardItem(Outfits.LEATHER, RewardKind.OUTFIT, R.string.rw_leather, R.string.rw_leather_d, 280, "🏍️"),
        RewardItem(Outfits.NEHRU, RewardKind.OUTFIT, R.string.rw_nehru, R.string.rw_nehru_d, 260, "🧥"),
        RewardItem(Outfits.LEHENGA, RewardKind.OUTFIT, R.string.rw_lehenga, R.string.rw_lehenga_d, 320, "💃"),
        RewardItem(Outfits.LAB_COAT, RewardKind.OUTFIT, R.string.rw_lab_coat, R.string.rw_lab_coat_d, 220, "🥼"),

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
        RewardItem(HairStyles.SIDE_PART, RewardKind.HAIR, R.string.rw_side_part, R.string.rw_side_part_d, 0, "💼"),
        RewardItem(HairStyles.PIXIE, RewardKind.HAIR, R.string.rw_pixie, R.string.rw_pixie_d, 0, "🧚"),
        RewardItem(HairStyles.SPIKY, RewardKind.HAIR, R.string.rw_spiky, R.string.rw_spiky_d, 100, "⚡"),
        RewardItem(HairStyles.QUIFF, RewardKind.HAIR, R.string.rw_quiff, R.string.rw_quiff_d, 140, "🕺"),
        RewardItem(HairStyles.MAN_BUN, RewardKind.HAIR, R.string.rw_man_bun, R.string.rw_man_bun_d, 140, "🧘"),
        RewardItem(HairStyles.AFRO, RewardKind.HAIR, R.string.rw_afro, R.string.rw_afro_d, 160, "🌳"),

        RewardItem(Accessories.NONE, RewardKind.ACCESSORY, R.string.rw_none, R.string.rw_none_d, 0, "✨"),
        RewardItem(Accessories.BINDI, RewardKind.ACCESSORY, R.string.rw_bindi, R.string.rw_bindi_d, 50, "🔴"),
        RewardItem(Accessories.EARRINGS, RewardKind.ACCESSORY, R.string.rw_earrings, R.string.rw_earrings_d, 80, "💛"),
        RewardItem(Accessories.GLASSES, RewardKind.ACCESSORY, R.string.rw_glasses, R.string.rw_glasses_d, 100, "👓"),
        RewardItem(Accessories.CAP, RewardKind.ACCESSORY, R.string.rw_cap, R.string.rw_cap_d, 150, "🧢"),
        RewardItem(Accessories.HEADPHONES, RewardKind.ACCESSORY, R.string.rw_headphones, R.string.rw_headphones_d, 200, "🎧"),
        RewardItem(Accessories.CROWN, RewardKind.ACCESSORY, R.string.rw_crown, R.string.rw_crown_d, 400, "👑"),
        RewardItem(Accessories.FLOWER, RewardKind.ACCESSORY, R.string.rw_flower, R.string.rw_flower_d, 60, "🌸"),
        RewardItem(Accessories.NOSE_RING, RewardKind.ACCESSORY, R.string.rw_nose_ring, R.string.rw_nose_ring_d, 90, "💍"),
        RewardItem(Accessories.BOW_TIE, RewardKind.ACCESSORY, R.string.rw_bow_tie, R.string.rw_bow_tie_d, 90, "🎀"),
        RewardItem(Accessories.BEANIE, RewardKind.ACCESSORY, R.string.rw_beanie, R.string.rw_beanie_d, 120, "🧶"),
        RewardItem(Accessories.SUNGLASSES, RewardKind.ACCESSORY, R.string.rw_sunglasses, R.string.rw_sunglasses_d, 150, "🕶️"),
        RewardItem(Accessories.CHAIN, RewardKind.ACCESSORY, R.string.rw_chain, R.string.rw_chain_d, 220, "⛓️"),
        RewardItem(Accessories.TURBAN, RewardKind.ACCESSORY, R.string.rw_turban, R.string.rw_turban_d, 250, "👳"),

        RewardItem(ThemeIds.DIWALI, RewardKind.THEME, R.string.rw_theme_diwali, R.string.rw_theme_diwali_d, 0, "🪔", FestivalPack.DIWALI),
        RewardItem(ThemeIds.HOLI, RewardKind.THEME, R.string.rw_theme_holi, R.string.rw_theme_holi_d, 0, "🎨", FestivalPack.HOLI),
        RewardItem(ThemeIds.GARBA, RewardKind.THEME, R.string.rw_theme_garba, R.string.rw_theme_garba_d, 0, "💃", FestivalPack.NAVRATRI),
        RewardItem(ThemeIds.KITES, RewardKind.THEME, R.string.rw_theme_kites, R.string.rw_theme_kites_d, 0, "🪁", FestivalPack.UTTARAYAN),
        RewardItem(ThemeIds.RAKHI, RewardKind.THEME, R.string.rw_theme_rakhi, R.string.rw_theme_rakhi_d, 0, "🎀", FestivalPack.RAKHI),
        RewardItem(ThemeIds.BAPPA, RewardKind.THEME, R.string.rw_theme_bappa, R.string.rw_theme_bappa_d, 0, "🌺", FestivalPack.GANESH),
        RewardItem(ThemeIds.TIRANGA, RewardKind.THEME, R.string.rw_theme_tiranga, R.string.rw_theme_tiranga_d, 0, "🇮🇳", FestivalPack.TIRANGA),
        RewardItem(ThemeIds.CHRISTMAS, RewardKind.THEME, R.string.rw_theme_christmas, R.string.rw_theme_christmas_d, 0, "🎄", FestivalPack.CHRISTMAS),
        RewardItem(ThemeIds.NEW_YEAR, RewardKind.THEME, R.string.rw_theme_new_year, R.string.rw_theme_new_year_d, 0, "🎆", FestivalPack.NEW_YEAR),
        RewardItem(Outfits.DIWALI_SILK, RewardKind.OUTFIT, R.string.rw_diwali_silk, R.string.rw_diwali_silk_d, 0, "🪔", FestivalPack.DIWALI),
        RewardItem(Outfits.HOLI, RewardKind.OUTFIT, R.string.rw_holi, R.string.rw_holi_d, 0, "🎨", FestivalPack.HOLI),
        RewardItem(Outfits.GARBA, RewardKind.OUTFIT, R.string.rw_garba, R.string.rw_garba_d, 0, "💃", FestivalPack.NAVRATRI),
        RewardItem(Outfits.KITE, RewardKind.OUTFIT, R.string.rw_kite, R.string.rw_kite_d, 0, "🪁", FestivalPack.UTTARAYAN),
        RewardItem(Outfits.RAKHI, RewardKind.OUTFIT, R.string.rw_rakhi, R.string.rw_rakhi_d, 0, "🎀", FestivalPack.RAKHI),
        RewardItem(Outfits.BAPPA, RewardKind.OUTFIT, R.string.rw_bappa, R.string.rw_bappa_d, 0, "🌺", FestivalPack.GANESH),
        RewardItem(Outfits.TIRANGA, RewardKind.OUTFIT, R.string.rw_tiranga, R.string.rw_tiranga_d, 0, "🇮🇳", FestivalPack.TIRANGA),
        RewardItem(Outfits.SANTA, RewardKind.OUTFIT, R.string.rw_santa, R.string.rw_santa_d, 0, "🎅", FestivalPack.CHRISTMAS),
        RewardItem(Outfits.SEQUIN, RewardKind.OUTFIT, R.string.rw_sequin, R.string.rw_sequin_d, 0, "🪩", FestivalPack.NEW_YEAR),
        RewardItem(Accessories.GULAL, RewardKind.ACCESSORY, R.string.rw_gulal, R.string.rw_gulal_d, 0, "🌈", FestivalPack.HOLI),
        RewardItem(Accessories.SANTA_HAT, RewardKind.ACCESSORY, R.string.rw_santa_hat, R.string.rw_santa_hat_d, 0, "🎅", FestivalPack.CHRISTMAS),
        RewardItem(Accessories.PARTY_HAT, RewardKind.ACCESSORY, R.string.rw_party_hat, R.string.rw_party_hat_d, 0, "🥳", FestivalPack.NEW_YEAR),

        RewardItem(Features.INSIGHTS, RewardKind.FEATURE, R.string.rw_insights, R.string.rw_insights_d, 300, "📈"),
        RewardItem(Features.EXPORT_CSV, RewardKind.FEATURE, R.string.rw_export, R.string.rw_export_d, 250, "📤")
    )

    private val byId = items.associateBy { it.id }

    fun defaults(): List<String> = items.filter { it.isFree && it.pack == null }.map { it.id }

    fun ofKind(kind: RewardKind): List<RewardItem> = items.filter { it.kind == kind }

    fun ofPack(pack: FestivalPack): List<RewardItem> = items.filter { it.pack == pack }

    fun dressFor(pack: FestivalPack, config: AvatarConfig): AvatarConfig {
        val festive = ofPack(pack)
        return config.copy(
            outfitId = festive.firstOrNull { it.kind == RewardKind.OUTFIT }?.id ?: config.outfitId,
            accessoryId = festive.firstOrNull { it.kind == RewardKind.ACCESSORY }?.id ?: config.accessoryId
        )
    }

    fun find(id: String): RewardItem? = byId[id]
}

enum class BadgeTier { EASY, HARD }

data class Badge(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val emoji: String,
    val bonusPoints: Int,
    val target: Int,
    @StringRes val unitRes: Int,
    val tier: BadgeTier,
    val howTo: List<Int>
)

data class BadgeSnapshot(
    val expenses: Int = 0,
    val distinctCategories: Int = 0,
    val paymentModes: Int = 0,
    val maxExpense: Double = 0.0,
    val smallExpenses: Int = 0,
    val notedExpenses: Int = 0,
    val earlyLogs: Int = 0,
    val lateLogs: Int = 0,
    val voiceLogs: Int = 0,
    val streak: Int = 0,
    val groups: Int = 0,
    val settlements: Int = 0,
    val membersBesidesYou: Int = 0,
    val reminders: Int = 0,
    val customCategories: Int = 0,
    val paidUnlocks: Int = 0,
    val outfitUnlocks: Int = 0,
    val accessoryUnlocks: Int = 0,
    val themeUnlocks: Int = 0,
    val monthsUnderBudget: Int = 0,
    val level: Int = 1,
    val languageChanged: Boolean = false
)

data class BadgeStatus(val badge: Badge, val earnedAt: Long?, val progress: Int) {
    val earned: Boolean get() = earnedAt != null
    val fraction: Float get() = if (earned) 1f else (progress.toFloat() / badge.target).coerceIn(0f, 1f)
}

object BadgeCatalog {
    const val FIRST_STEP = "badge_first_step"
    const val TEN_LOGGED = "badge_ten_logged"
    const val CENTURY = "badge_century"
    const val HALF_K = "badge_half_k"
    const val WEEK_WARRIOR = "badge_week_warrior"
    const val MONTH_MASTER = "badge_month_master"
    const val STREAK_60 = "badge_streak_60"
    const val STREAK_100 = "badge_streak_100"
    const val EARLY_BIRD = "badge_early_bird"
    const val NIGHT_OWL = "badge_night_owl"
    const val VOICE_FIRST = "badge_voice"
    const val NOTE_TAKER = "badge_notes"
    const val EXPLORER = "badge_explorer"
    const val CUSTOM_CAT = "badge_custom_cat"
    const val PAYMENT_PRO = "badge_payment_pro"
    const val BIG_SPENDER = "badge_big_spender"
    const val PENNY = "badge_penny"
    const val TEAM_PLAYER = "badge_team_player"
    const val GROUP_GURU = "badge_group_guru"
    const val FAIR_SHARE = "badge_fair_share"
    const val SETTLER = "badge_settler"
    const val FAMILY_FIRST = "badge_family_first"
    const val FULL_HOUSE = "badge_full_house"
    const val BILL_PLANNER = "badge_bill_planner"
    const val BUDGET_BOSS = "badge_budget_boss"
    const val BUDGET_HATTRICK = "badge_budget_hattrick"
    const val STYLIST = "badge_stylist"
    const val ACCESSORIZED = "badge_accessorized"
    const val NEW_LOOK = "badge_new_look"
    const val COLLECTOR = "badge_collector"
    const val POLYGLOT = "badge_polyglot"
    const val LEVEL_5 = "badge_level_5"
    const val LEVEL_10 = "badge_level_10"

    val badges: List<Badge> = listOf(
        Badge(FIRST_STEP, R.string.b_first_step, R.string.b_first_step_d, "🥇", 20, 1, R.string.unit_expense, BadgeTier.EASY,
            listOf(R.string.b_first_step_1, R.string.b_first_step_2, R.string.b_first_step_3)),
        Badge(TEN_LOGGED, R.string.b_ten_logged, R.string.b_ten_logged_d, "🔟", 40, 10, R.string.unit_expenses, BadgeTier.EASY,
            listOf(R.string.b_ten_logged_1, R.string.b_ten_logged_2, R.string.b_ten_logged_3)),
        Badge(CENTURY, R.string.b_century, R.string.b_century_d, "💯", 150, 100, R.string.unit_expenses, BadgeTier.HARD,
            listOf(R.string.b_century_1, R.string.b_century_2, R.string.b_century_3)),
        Badge(HALF_K, R.string.b_half_k, R.string.b_half_k_d, "🚀", 500, 500, R.string.unit_expenses, BadgeTier.HARD,
            listOf(R.string.b_half_k_1, R.string.b_half_k_2, R.string.b_half_k_3)),
        Badge(WEEK_WARRIOR, R.string.b_week_warrior, R.string.b_week_warrior_d, "🔥", 60, 7, R.string.unit_days, BadgeTier.EASY,
            listOf(R.string.b_week_warrior_1, R.string.b_week_warrior_2, R.string.b_week_warrior_3)),
        Badge(MONTH_MASTER, R.string.b_month_master, R.string.b_month_master_d, "🏆", 250, 30, R.string.unit_days, BadgeTier.HARD,
            listOf(R.string.b_month_master_1, R.string.b_month_master_2, R.string.b_month_master_3)),
        Badge(STREAK_60, R.string.b_streak_60, R.string.b_streak_60_d, "⚡", 400, 60, R.string.unit_days, BadgeTier.HARD,
            listOf(R.string.b_streak_60_1, R.string.b_streak_60_2, R.string.b_streak_60_3)),
        Badge(STREAK_100, R.string.b_streak_100, R.string.b_streak_100_d, "🐉", 1000, 100, R.string.unit_days, BadgeTier.HARD,
            listOf(R.string.b_streak_100_1, R.string.b_streak_100_2, R.string.b_streak_100_3)),
        Badge(EARLY_BIRD, R.string.b_early_bird, R.string.b_early_bird_d, "🌅", 30, 1, R.string.unit_expense, BadgeTier.EASY,
            listOf(R.string.b_early_bird_1, R.string.b_early_bird_2, R.string.b_early_bird_3)),
        Badge(NIGHT_OWL, R.string.b_night_owl, R.string.b_night_owl_d, "🦉", 30, 1, R.string.unit_expense, BadgeTier.EASY,
            listOf(R.string.b_night_owl_1, R.string.b_night_owl_2, R.string.b_night_owl_3)),
        Badge(VOICE_FIRST, R.string.b_voice, R.string.b_voice_d, "🎙️", 40, 1, R.string.unit_expense, BadgeTier.EASY,
            listOf(R.string.b_voice_1, R.string.b_voice_2, R.string.b_voice_3)),
        Badge(NOTE_TAKER, R.string.b_notes, R.string.b_notes_d, "📝", 60, 10, R.string.unit_expenses, BadgeTier.EASY,
            listOf(R.string.b_notes_1, R.string.b_notes_2, R.string.b_notes_3)),
        Badge(EXPLORER, R.string.b_explorer, R.string.b_explorer_d, "🧭", 60, 5, R.string.unit_categories, BadgeTier.EASY,
            listOf(R.string.b_explorer_1, R.string.b_explorer_2, R.string.b_explorer_3)),
        Badge(CUSTOM_CAT, R.string.b_custom_cat, R.string.b_custom_cat_d, "🏷️", 50, 1, R.string.unit_category, BadgeTier.EASY,
            listOf(R.string.b_custom_cat_1, R.string.b_custom_cat_2, R.string.b_custom_cat_3)),
        Badge(PAYMENT_PRO, R.string.b_payment_pro, R.string.b_payment_pro_d, "💳", 80, 4, R.string.unit_modes, BadgeTier.EASY,
            listOf(R.string.b_payment_pro_1, R.string.b_payment_pro_2, R.string.b_payment_pro_3)),
        Badge(BIG_SPENDER, R.string.b_big_spender, R.string.b_big_spender_d, "💎", 100, 1, R.string.unit_expense, BadgeTier.EASY,
            listOf(R.string.b_big_spender_1, R.string.b_big_spender_2, R.string.b_big_spender_3)),
        Badge(PENNY, R.string.b_penny, R.string.b_penny_d, "🪙", 80, 20, R.string.unit_expenses, BadgeTier.EASY,
            listOf(R.string.b_penny_1, R.string.b_penny_2, R.string.b_penny_3)),
        Badge(TEAM_PLAYER, R.string.b_team_player, R.string.b_team_player_d, "🤝", 30, 1, R.string.unit_group, BadgeTier.EASY,
            listOf(R.string.b_team_player_1, R.string.b_team_player_2, R.string.b_team_player_3)),
        Badge(GROUP_GURU, R.string.b_group_guru, R.string.b_group_guru_d, "🧑‍🤝‍🧑", 100, 3, R.string.unit_groups, BadgeTier.HARD,
            listOf(R.string.b_group_guru_1, R.string.b_group_guru_2, R.string.b_group_guru_3)),
        Badge(FAIR_SHARE, R.string.b_fair_share, R.string.b_fair_share_d, "⚖️", 40, 1, R.string.unit_settlement, BadgeTier.EASY,
            listOf(R.string.b_fair_share_1, R.string.b_fair_share_2, R.string.b_fair_share_3)),
        Badge(SETTLER, R.string.b_settler, R.string.b_settler_d, "🏦", 300, 10, R.string.unit_settlements, BadgeTier.HARD,
            listOf(R.string.b_settler_1, R.string.b_settler_2, R.string.b_settler_3)),
        Badge(FAMILY_FIRST, R.string.b_family_first, R.string.b_family_first_d, "👨‍👩‍👧", 50, 3, R.string.unit_members, BadgeTier.EASY,
            listOf(R.string.b_family_first_1, R.string.b_family_first_2, R.string.b_family_first_3)),
        Badge(FULL_HOUSE, R.string.b_full_house, R.string.b_full_house_d, "🏠", 150, 5, R.string.unit_members, BadgeTier.HARD,
            listOf(R.string.b_full_house_1, R.string.b_full_house_2, R.string.b_full_house_3)),
        Badge(BILL_PLANNER, R.string.b_bill_planner, R.string.b_bill_planner_d, "📅", 100, 5, R.string.unit_reminders, BadgeTier.EASY,
            listOf(R.string.b_bill_planner_1, R.string.b_bill_planner_2, R.string.b_bill_planner_3)),
        Badge(BUDGET_BOSS, R.string.b_budget_boss, R.string.b_budget_boss_d, "🎯", 200, 1, R.string.unit_month, BadgeTier.HARD,
            listOf(R.string.b_budget_boss_1, R.string.b_budget_boss_2, R.string.b_budget_boss_3)),
        Badge(BUDGET_HATTRICK, R.string.b_budget_hattrick, R.string.b_budget_hattrick_d, "🥉", 600, 3, R.string.unit_months, BadgeTier.HARD,
            listOf(R.string.b_budget_hattrick_1, R.string.b_budget_hattrick_2, R.string.b_budget_hattrick_3)),
        Badge(STYLIST, R.string.b_stylist, R.string.b_stylist_d, "🧵", 30, 1, R.string.unit_outfit, BadgeTier.EASY,
            listOf(R.string.b_stylist_1, R.string.b_stylist_2, R.string.b_stylist_3)),
        Badge(ACCESSORIZED, R.string.b_accessorized, R.string.b_accessorized_d, "💍", 120, 3, R.string.unit_accessories, BadgeTier.HARD,
            listOf(R.string.b_accessorized_1, R.string.b_accessorized_2, R.string.b_accessorized_3)),
        Badge(NEW_LOOK, R.string.b_new_look, R.string.b_new_look_d, "🎨", 50, 1, R.string.unit_theme, BadgeTier.EASY,
            listOf(R.string.b_new_look_1, R.string.b_new_look_2, R.string.b_new_look_3)),
        Badge(COLLECTOR, R.string.b_collector, R.string.b_collector_d, "🗂️", 80, 5, R.string.unit_rewards, BadgeTier.HARD,
            listOf(R.string.b_collector_1, R.string.b_collector_2, R.string.b_collector_3)),
        Badge(POLYGLOT, R.string.b_polyglot, R.string.b_polyglot_d, "🗣️", 30, 1, R.string.unit_language, BadgeTier.EASY,
            listOf(R.string.b_polyglot_1, R.string.b_polyglot_2, R.string.b_polyglot_3)),
        Badge(LEVEL_5, R.string.b_level_5, R.string.b_level_5_d, "🎖️", 300, 5, R.string.unit_level, BadgeTier.HARD,
            listOf(R.string.b_level_5_1, R.string.b_level_5_2, R.string.b_level_5_3)),
        Badge(LEVEL_10, R.string.b_level_10, R.string.b_level_10_d, "👑", 1000, 10, R.string.unit_level, BadgeTier.HARD,
            listOf(R.string.b_level_10_1, R.string.b_level_10_2, R.string.b_level_10_3))
    )

    private val byId = badges.associateBy { it.id }

    fun find(id: String): Badge? = byId[id]

    fun progress(id: String, s: BadgeSnapshot): Int = when (id) {
        FIRST_STEP, TEN_LOGGED, CENTURY, HALF_K -> s.expenses
        WEEK_WARRIOR, MONTH_MASTER, STREAK_60, STREAK_100 -> s.streak
        EARLY_BIRD -> s.earlyLogs
        NIGHT_OWL -> s.lateLogs
        VOICE_FIRST -> s.voiceLogs
        NOTE_TAKER -> s.notedExpenses
        EXPLORER -> s.distinctCategories
        CUSTOM_CAT -> s.customCategories
        PAYMENT_PRO -> s.paymentModes
        BIG_SPENDER -> if (s.maxExpense >= 10_000) 1 else 0
        PENNY -> s.smallExpenses
        TEAM_PLAYER, GROUP_GURU -> s.groups
        FAIR_SHARE, SETTLER -> s.settlements
        FAMILY_FIRST, FULL_HOUSE -> s.membersBesidesYou
        BILL_PLANNER -> s.reminders
        BUDGET_BOSS, BUDGET_HATTRICK -> s.monthsUnderBudget
        STYLIST -> s.outfitUnlocks
        ACCESSORIZED -> s.accessoryUnlocks
        NEW_LOOK -> s.themeUnlocks
        COLLECTOR -> s.paidUnlocks
        POLYGLOT -> if (s.languageChanged) 1 else 0
        LEVEL_5, LEVEL_10 -> s.level
        else -> 0
    }
}
