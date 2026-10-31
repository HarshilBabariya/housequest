package com.homequest.app.domain

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Wifi
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
        "cart" to Icons.Filled.ShoppingCart,
        "bulb" to Icons.Filled.Lightbulb,
        "home" to Icons.Filled.Home,
        "car" to Icons.Filled.DirectionsCar,
        "restaurant" to Icons.Filled.Restaurant,
        "medical" to Icons.Filled.MedicalServices,
        "school" to Icons.Filled.School,
        "movie" to Icons.Filled.Movie,
        "bag" to Icons.Filled.ShoppingBag,
        "box" to Icons.Filled.Inventory2,
        "phone" to Icons.Filled.PhoneAndroid,
        "gift" to Icons.Filled.CardGiftcard,
        "pets" to Icons.Filled.Pets,
        "flight" to Icons.Filled.Flight,
        "train" to Icons.Filled.Train,
        "fuel" to Icons.Filled.LocalGasStation,
        "fitness" to Icons.Filled.FitnessCenter,
        "cafe" to Icons.Filled.LocalCafe,
        "baby" to Icons.Filled.ChildCare,
        "wifi" to Icons.Filled.Wifi,
        "repair" to Icons.Filled.Build,
        "savings" to Icons.Filled.Savings,
        "clothes" to Icons.Filled.Checkroom,
        "party" to Icons.Filled.Celebration,
        "games" to Icons.Filled.SportsEsports,
        "spa" to Icons.Filled.Spa
    )

    val keys: List<String> = byKey.keys.toList()

    fun of(key: String): ImageVector = byKey[key] ?: Icons.Filled.Inventory2
}

val CategoryColors = listOf(
    Color(0xFF43A047), Color(0xFFF9A825), Color(0xFF3949AB), Color(0xFF039BE5), Color(0xFFF4511E),
    Color(0xFFD81B60), Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFFFB8C00), Color(0xFF757575),
    Color(0xFF6D4C41), Color(0xFF00897B), Color(0xFFC0CA33), Color(0xFF5E35B1), Color(0xFFE53935)
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

enum class PaymentMode(@StringRes val labelRes: Int, val icon: ImageVector) {
    CASH(R.string.pay_cash, Icons.Filled.Payments),
    UPI(R.string.pay_upi, Icons.Filled.QrCode),
    CARD(R.string.pay_card, Icons.Filled.CreditCard),
    BANK(R.string.pay_bank, Icons.Filled.AccountBalance)
}

enum class SplitType { EQUAL, CUSTOM, NONE }

enum class RepeatRule(@StringRes val labelRes: Int) {
    ONCE(R.string.repeat_once),
    DAILY(R.string.repeat_daily),
    WEEKLY(R.string.repeat_weekly),
    MONTHLY(R.string.repeat_monthly),
    YEARLY(R.string.repeat_yearly)
}

enum class Mood(val emoji: String, @StringRes val labelRes: Int, @StringRes val lineRes: Int) {
    HAPPY("😄", R.string.mood_happy, R.string.mood_line_happy),
    LOVE("😍", R.string.mood_love, R.string.mood_line_love),
    EXCITED("🤩", R.string.mood_excited, R.string.mood_line_excited),
    SMART("🤓", R.string.mood_smart, R.string.mood_line_smart),
    NEUTRAL("🙂", R.string.mood_neutral, R.string.mood_line_neutral),
    CONFUSED("😕", R.string.mood_confused, R.string.mood_line_confused),
    SLEEPY("😴", R.string.mood_sleepy, R.string.mood_line_sleepy),
    SAD("😢", R.string.mood_sad, R.string.mood_line_sad),
    WORRIED("😟", R.string.mood_worried, R.string.mood_line_worried),
    ANGRY("😤", R.string.mood_angry, R.string.mood_line_angry)
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
    val mood: Mood? = null
)

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
}

object Accessories {
    const val NONE = "acc_none"
    const val BINDI = "acc_bindi"
    const val EARRINGS = "acc_earrings"
    const val GLASSES = "acc_glasses"
    const val CAP = "acc_cap"
    const val HEADPHONES = "acc_headphones"
    const val CROWN = "acc_crown"
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
}

val SkinTones = listOf(
    Color(0xFFFFE0BD), Color(0xFFF1C27D), Color(0xFFD9A066),
    Color(0xFFB5753F), Color(0xFF8D5524), Color(0xFF5C3A21)
)

val HairColors = listOf(
    Color(0xFF2A1B17), Color(0xFF5B3A29), Color(0xFFB4532A),
    Color(0xFFE0B678), Color(0xFF8E8E8E), Color(0xFF6D28D9), Color(0xFF0E9AA7)
)
