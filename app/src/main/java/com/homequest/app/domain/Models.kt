package com.homequest.app.domain

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.homequest.app.R

data class Category(
    val id: String,
    val customName: String,
    @StringRes val nameRes: Int?,
    val iconKey: String,
    val color: Color,
    val builtIn: Boolean
) {
    val icon: ImageVector get() = CategoryIcons.of(iconKey)
}

data class CategoryAmount(val category: Category, val total: Double)

object CategoryIcons {
    private val byKey: Map<String, ImageVector> = linkedMapOf(
        "cart" to Icons.Rounded.ShoppingCart,
        "bulb" to Icons.Rounded.Lightbulb,
        "home" to Icons.Rounded.Home,
        "car" to Icons.Rounded.DirectionsCar,
        "restaurant" to Icons.Rounded.Restaurant,
        "medical" to Icons.Rounded.MedicalServices,
        "school" to Icons.Rounded.School,
        "movie" to Icons.Rounded.Movie,
        "bag" to Icons.Rounded.ShoppingBag,
        "box" to Icons.Rounded.Inventory2,
        "phone" to Icons.Rounded.PhoneAndroid,
        "gift" to Icons.Rounded.CardGiftcard,
        "pets" to Icons.Rounded.Pets,
        "flight" to Icons.Rounded.Flight,
        "train" to Icons.Rounded.Train,
        "fuel" to Icons.Rounded.LocalGasStation,
        "fitness" to Icons.Rounded.FitnessCenter,
        "cafe" to Icons.Rounded.LocalCafe,
        "baby" to Icons.Rounded.ChildCare,
        "wifi" to Icons.Rounded.Wifi,
        "repair" to Icons.Rounded.Build,
        "savings" to Icons.Rounded.Savings,
        "clothes" to Icons.Rounded.Checkroom,
        "party" to Icons.Rounded.Celebration,
        "games" to Icons.Rounded.SportsEsports,
        "spa" to Icons.Rounded.Spa
    )

    val keys: List<String> = byKey.keys.toList()

    fun of(key: String): ImageVector = byKey[key] ?: Icons.Rounded.Inventory2
}

val CategoryColors = listOf(
    Color(0xFF43A047), Color(0xFFF9A825), Color(0xFF3949AB), Color(0xFF039BE5), Color(0xFFF4511E),
    Color(0xFFD81B60), Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFFFB8C00), Color(0xFF757575),
    Color(0xFF6D4C41), Color(0xFF00897B), Color(0xFFC0CA33), Color(0xFF5E35B1), Color(0xFFE53935),
    Color(0xFF1E88E5), Color(0xFF7CB342), Color(0xFFFFB300), Color(0xFFEC407A), Color(0xFF26A69A),
    Color(0xFFAB47BC), Color(0xFF5C6BC0), Color(0xFFFF7043), Color(0xFF8D6E63), Color(0xFF78909C),
    Color(0xFF0D47A1), Color(0xFF1B5E20), Color(0xFFB71C1C), Color(0xFF4A148C), Color(0xFF006064),
    Color(0xFFFF4081), Color(0xFF00E5FF), Color(0xFF76FF03), Color(0xFFFFD600), Color(0xFF455A64),
    Color(0xFFF48FB1), Color(0xFF80CBC4), Color(0xFFB39DDB), Color(0xFFFFCC80), Color(0xFF90CAF9)
)

object BuiltInCategories {
    const val GROCERIES = "GROCERIES"
    const val UTILITIES = "UTILITIES"
    const val RENT = "RENT"
    const val TRANSPORT = "TRANSPORT"
    const val DINING = "DINING"
    const val HEALTH = "HEALTH"
    const val EDUCATION = "EDUCATION"
    const val ENTERTAINMENT = "ENTERTAINMENT"
    const val SHOPPING = "SHOPPING"
    const val OTHER = "OTHER"

    class Seed(val id: String, @StringRes val nameRes: Int, val iconKey: String, val color: Color)

    val seeds: List<Seed> = listOf(
        Seed(GROCERIES, R.string.cat_groceries, "cart", CategoryColors[0]),
        Seed(UTILITIES, R.string.cat_utilities, "bulb", CategoryColors[1]),
        Seed(RENT, R.string.cat_rent, "home", CategoryColors[2]),
        Seed(TRANSPORT, R.string.cat_transport, "car", CategoryColors[3]),
        Seed(DINING, R.string.cat_dining, "restaurant", CategoryColors[4]),
        Seed(HEALTH, R.string.cat_health, "medical", CategoryColors[5]),
        Seed(EDUCATION, R.string.cat_education, "school", CategoryColors[6]),
        Seed(ENTERTAINMENT, R.string.cat_fun, "movie", CategoryColors[7]),
        Seed(SHOPPING, R.string.cat_shopping, "bag", CategoryColors[8]),
        Seed(OTHER, R.string.cat_other, "box", CategoryColors[9])
    )

    private val nameById = seeds.associate { it.id to it.nameRes }

    fun nameRes(id: String): Int? = nameById[id]
}

enum class PaymentMode(@StringRes val labelRes: Int, val icon: ImageVector, val color: Color) {
    CASH(R.string.pay_cash, Icons.Rounded.Payments, Color(0xFF43A047)),
    UPI(R.string.pay_upi, Icons.Rounded.QrCode, Color(0xFF5E35B1)),
    CARD(R.string.pay_card, Icons.Rounded.CreditCard, Color(0xFFFB8C00)),
    BANK(R.string.pay_bank, Icons.Rounded.AccountBalance, Color(0xFF039BE5))
}

enum class SplitType { EQUAL, CUSTOM, NONE }

enum class RepeatRule(@StringRes val labelRes: Int) {
    ONCE(R.string.repeat_once),
    DAILY(R.string.repeat_daily),
    WEEKLY(R.string.repeat_weekly),
    MONTHLY(R.string.repeat_monthly),
    YEARLY(R.string.repeat_yearly)
}

enum class Mood(val emoji: String, @StringRes val labelRes: Int, @ArrayRes val linesRes: Int) {
    HAPPY("😄", R.string.mood_happy, R.array.mood_lines_happy),
    LOVE("😍", R.string.mood_love, R.array.mood_lines_love),
    EXCITED("🤩", R.string.mood_excited, R.array.mood_lines_excited),
    SMART("🤓", R.string.mood_smart, R.array.mood_lines_smart),
    NEUTRAL("🙂", R.string.mood_neutral, R.array.mood_lines_neutral),
    CONFUSED("😕", R.string.mood_confused, R.array.mood_lines_confused),
    SLEEPY("😴", R.string.mood_sleepy, R.array.mood_lines_sleepy),
    SAD("😢", R.string.mood_sad, R.array.mood_lines_sad),
    WORRIED("😟", R.string.mood_worried, R.array.mood_lines_worried),
    ANGRY("😤", R.string.mood_angry, R.array.mood_lines_angry)
}

data class Currency(val code: String, val symbol: String)

val Currencies = listOf(Currency("INR", "₹"), Currency("USD", "$"), Currency("EUR", "€"))

enum class AppLanguage(val tag: String, val nativeName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी"),
    GUJARATI("gu", "ગુજરાતી")
}

object Relations {
    const val ME = "me"
    val all: List<Pair<String, Int>> = listOf(
        ME to R.string.rel_me, "partner" to R.string.rel_partner, "child" to R.string.rel_child, "parent" to R.string.rel_parent,
        "sibling" to R.string.rel_sibling, "roommate" to R.string.rel_roommate, "friend" to R.string.rel_friend, "other" to R.string.rel_other
    )

    @StringRes fun labelRes(key: String): Int? = all.firstOrNull { it.first.equals(key, ignoreCase = true) }?.second
}

enum class RewardKind(@StringRes val labelRes: Int) {
    THEME(R.string.kind_themes),
    OUTFIT(R.string.kind_outfits),
    HAIR(R.string.kind_hair),
    ACCESSORY(R.string.kind_accessories),
    FEATURE(R.string.kind_features)
}

enum class Gender(@StringRes val labelRes: Int, val emoji: String) {
    MALE(R.string.gender_male, "👦"),
    FEMALE(R.string.gender_female, "👧")
}

data class AvatarConfig(
    val skinTone: Int = 0,
    val hairId: String = HairStyles.SHORT,
    val hairColor: Int = 0,
    val outfitId: String = Outfits.TEE,
    val accessoryId: String = Accessories.NONE,
    val gender: Gender = Gender.MALE,
    val mood: Mood? = null,
    val faceShape: FaceShape = FaceShape.OVAL,
    val beard: Beard = Beard.NONE
)

enum class FaceShape(@StringRes val labelRes: Int) {
    OVAL(R.string.face_oval),
    ROUND(R.string.face_round),
    SQUARE(R.string.face_square),
    HEART(R.string.face_heart),
    LONG(R.string.face_long)
}

enum class Beard(@StringRes val labelRes: Int) {
    NONE(R.string.beard_none),
    STUBBLE(R.string.beard_stubble),
    MUSTACHE(R.string.beard_mustache),
    GOATEE(R.string.beard_goatee),
    CHIN_STRAP(R.string.beard_chin_strap),
    FULL(R.string.beard_full)
}

enum class AvatarAction(@StringRes val labelRes: Int, val emoji: String, val durationMs: Int) {
    WAVE(R.string.act_wave, "👋", 1500),
    JUMP(R.string.act_jump, "🦘", 1100),
    SPIN(R.string.act_spin, "🌀", 1000),
    DANCE(R.string.act_dance, "💃", 2000),
    WINK(R.string.act_wink, "😉", 1200),
    CHEER(R.string.act_cheer, "🙌", 1600),
    LAUGH(R.string.act_laugh, "😂", 1500)
}

object HairStyles {
    const val SHORT = "hair_short"
    const val BUZZ = "hair_buzz"
    const val LONG = "hair_long"
    const val WAVY = "hair_wavy"
    const val BOB = "hair_bob"
    const val PONYTAIL = "hair_ponytail"
    const val CURLY = "hair_curly"
    const val BUN = "hair_bun"
    const val BRAIDS = "hair_braids"
    const val MOHAWK = "hair_mohawk"
    const val SPIKY = "hair_spiky"
    const val SIDE_PART = "hair_side_part"
    const val QUIFF = "hair_quiff"
    const val AFRO = "hair_afro"
    const val PIXIE = "hair_pixie"
    const val MAN_BUN = "hair_man_bun"
}

object Outfits {
    const val TEE = "outfit_tee"
    const val SHIRT = "outfit_shirt"
    const val HOODIE = "outfit_hoodie"
    const val DENIM = "outfit_denim"
    const val SWEATER = "outfit_sweater"
    const val BLAZER = "outfit_blazer"
    const val KURTA = "outfit_kurta"
    const val SHERWANI = "outfit_sherwani"
    const val SAREE = "outfit_saree"
    const val DRESS = "outfit_dress"
    const val SPACESUIT = "outfit_spacesuit"
    const val HERO = "outfit_hero"
    const val POLO = "outfit_polo"
    const val JERSEY = "outfit_jersey"
    const val LEATHER = "outfit_leather"
    const val NEHRU = "outfit_nehru"
    const val LEHENGA = "outfit_lehenga"
    const val LAB_COAT = "outfit_lab_coat"
}

object Accessories {
    const val NONE = "acc_none"
    const val BINDI = "acc_bindi"
    const val EARRINGS = "acc_earrings"
    const val GLASSES = "acc_glasses"
    const val CAP = "acc_cap"
    const val HEADPHONES = "acc_headphones"
    const val CROWN = "acc_crown"
    const val SUNGLASSES = "acc_sunglasses"
    const val BEANIE = "acc_beanie"
    const val TURBAN = "acc_turban"
    const val FLOWER = "acc_flower"
    const val BOW_TIE = "acc_bow_tie"
    const val NOSE_RING = "acc_nose_ring"
    const val CHAIN = "acc_chain"
}

object Features {
    const val INSIGHTS = "feature_insights"
    const val EXPORT_CSV = "feature_export_csv"
}

object ThemeIds {
    const val SUNRISE = "theme_sunrise"
    const val OCEAN = "theme_ocean"
    const val FOREST = "theme_forest"
    const val MIDNIGHT = "theme_midnight"
    const val CANDY = "theme_candy"
    const val ROYAL = "theme_royal"
    const val GLASS = "theme_glass"
}

val SkinTones = listOf(
    Color(0xFFFFE0BD), Color(0xFFF1C27D), Color(0xFFD9A066),
    Color(0xFFB5753F), Color(0xFF8D5524), Color(0xFF5C3A21)
)

val HairColors = listOf(
    Color(0xFF2A1B17), Color(0xFF5B3A29), Color(0xFFB4532A),
    Color(0xFFE0B678), Color(0xFF8E8E8E), Color(0xFF6D28D9), Color(0xFF0E9AA7)
)
