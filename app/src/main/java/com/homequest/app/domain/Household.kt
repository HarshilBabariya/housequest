package com.homequest.app.domain

import androidx.annotation.StringRes
import com.homequest.app.R

enum class ServiceBilling(@StringRes val labelRes: Int) {
    PER_UNIT(R.string.billing_per_unit),
    MONTHLY(R.string.billing_monthly)
}

data class ServicePreset(@StringRes val nameRes: Int, val emoji: String, val billing: ServiceBilling, val rate: Double, val defaultQty: Double, val categoryId: String)

object ServicePresets {
    val all = listOf(
        ServicePreset(R.string.svc_milk, "🥛", ServiceBilling.PER_UNIT, 60.0, 1.0, BuiltInCategories.GROCERIES),
        ServicePreset(R.string.svc_newspaper, "📰", ServiceBilling.PER_UNIT, 8.0, 1.0, BuiltInCategories.OTHER),
        ServicePreset(R.string.svc_water_can, "💧", ServiceBilling.PER_UNIT, 40.0, 1.0, BuiltInCategories.UTILITIES),
        ServicePreset(R.string.svc_maid, "🧹", ServiceBilling.MONTHLY, 3000.0, 1.0, BuiltInCategories.OTHER),
        ServicePreset(R.string.svc_cook, "🍳", ServiceBilling.MONTHLY, 5000.0, 1.0, BuiltInCategories.OTHER),
        ServicePreset(R.string.svc_driver, "🚗", ServiceBilling.MONTHLY, 12000.0, 1.0, BuiltInCategories.TRANSPORT)
    )
}

data class ServiceMonth(val units: Double, val leaves: Int, val amount: Double)

val GoldKarats = listOf(24, 22, 18)
