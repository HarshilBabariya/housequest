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
    WEEKLY("Weekly")
}

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
    const val LONG = "hair_long"
    const val CURLY = "hair_curly"
    const val MOHAWK = "hair_mohawk"
    const val BUN = "hair_bun"
}

object Outfits {
    const val TEE = "outfit_tee"
    const val HOODIE = "outfit_hoodie"
    const val BLAZER = "outfit_blazer"
    const val KURTA = "outfit_kurta"
    const val SPACESUIT = "outfit_spacesuit"
    const val HERO = "outfit_hero"
}

object Accessories {
    const val NONE = "acc_none"
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
    Color(0xFFFFDBB4), Color(0xFFEDB98A), Color(0xFFD08B5B),
    Color(0xFFAE5D29), Color(0xFF694D3D), Color(0xFF3B2219)
)

val HairColors = listOf(
    Color(0xFF2C1B18), Color(0xFF6A4E42), Color(0xFFB55239),
    Color(0xFFE6BE8A), Color(0xFF9E9E9E), Color(0xFF7B1FA2), Color(0xFF00ACC1)
)
