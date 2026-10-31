package com.homequest.app.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(val label: String, val icon: ImageVector, val color: Color) {
    GROCERIES("Groceries", Icons.Filled.ShoppingCart, Color(0xFF43A047)),
    UTILITIES("Utilities", Icons.Filled.Lightbulb, Color(0xFFF9A825)),
    RENT("Rent & Home", Icons.Filled.Home, Color(0xFF3949AB)),
    TRANSPORT("Transport", Icons.Filled.DirectionsCar, Color(0xFF039BE5)),
    DINING("Dining out", Icons.Filled.Restaurant, Color(0xFFF4511E)),
    HEALTH("Health", Icons.Filled.MedicalServices, Color(0xFFD81B60)),
    EDUCATION("Education", Icons.Filled.School, Color(0xFF8E24AA)),
    ENTERTAINMENT("Fun", Icons.Filled.Movie, Color(0xFF00ACC1)),
    SHOPPING("Shopping", Icons.Filled.ShoppingBag, Color(0xFFFB8C00)),
    OTHER("Other", Icons.Filled.Inventory2, Color(0xFF757575));
}

enum class SplitType { EQUAL, CUSTOM, NONE }

enum class RepeatRule(val label: String) {
    ONCE("Once"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

enum class Mood { HAPPY, NEUTRAL, WORRIED, ANGRY }

data class Currency(val code: String, val symbol: String)

val Currencies = listOf(
    Currency("INR", "₹"), Currency("USD", "$"), Currency("EUR", "€"), Currency("GBP", "£"),
    Currency("AED", "د.إ"), Currency("JPY", "¥"), Currency("AUD", "A$"), Currency("CAD", "C$"), Currency("SGD", "S$")
)

enum class RewardKind(val label: String) {
    THEME("Themes"),
    OUTFIT("Outfits"),
    HAIR("Hair"),
    ACCESSORY("Accessories"),
    FEATURE("Features")
}

data class AvatarConfig(
    val skinTone: Int = 0,
    val hairId: String = HairStyles.SHORT,
    val hairColor: Int = 0,
    val outfitId: String = Outfits.TEE,
    val accessoryId: String = Accessories.NONE
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
