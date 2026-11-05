package com.homequest.app.util

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import com.homequest.app.HomeActivity
import com.homequest.app.R
import com.homequest.app.data.repository.AppSettings
import com.homequest.app.domain.FestivalDay
import com.homequest.app.domain.FestivalPack

object FestiveLauncher {
    private val appPackage = HomeActivity::class.java.name.substringBeforeLast('.')

    fun packFor(settings: AppSettings, festival: FestivalDay?): FestivalPack? = festival?.festival?.pack?.takeIf { settings.festiveMode && settings.festiveIcon }

    fun switchIconInBackground(context: Context, pack: FestivalPack?) {
        val state = ActivityManager.RunningAppProcessInfo().also(ActivityManager::getMyMemoryState)
        if (state.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) switchIcon(context, pack)
    }

    fun apply(activity: Activity, pack: FestivalPack?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) activity.splashScreen.setSplashScreenTheme(splashTheme(pack))
        switchIcon(activity, pack)
    }

    private fun switchIcon(context: Context, pack: FestivalPack?) {
        val pm = context.packageManager
        val wanted = alias(pack)
        (listOf<FestivalPack?>(pack) + (listOf(null) + FestivalPack.entries).filter { it != pack }).forEach { candidate ->
            val component = ComponentName(context, alias(candidate))
            val state = pm.getComponentEnabledSetting(component)
            val enabled = state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && candidate == null)
            val enable = alias(candidate) == wanted
            if (enabled != enable) {
                pm.setComponentEnabledSetting(
                    component,
                    if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    private fun alias(pack: FestivalPack?) = if (pack == null) "$appPackage.MainActivity" else "$appPackage.Launcher_${pack.name}"

    private fun splashTheme(pack: FestivalPack?): Int = when (pack) {
        null -> Resources.ID_NULL
        FestivalPack.UTTARAYAN -> R.style.Theme_HomeQuest_Splash_Uttarayan
        FestivalPack.TIRANGA -> R.style.Theme_HomeQuest_Splash_Tiranga
        FestivalPack.HOLI -> R.style.Theme_HomeQuest_Splash_Holi
        FestivalPack.RAKHI -> R.style.Theme_HomeQuest_Splash_Rakhi
        FestivalPack.GANESH -> R.style.Theme_HomeQuest_Splash_Ganesh
        FestivalPack.NAVRATRI -> R.style.Theme_HomeQuest_Splash_Navratri
        FestivalPack.DIWALI -> R.style.Theme_HomeQuest_Splash_Diwali
        FestivalPack.CHRISTMAS -> R.style.Theme_HomeQuest_Splash_Christmas
        FestivalPack.NEW_YEAR -> R.style.Theme_HomeQuest_Splash_NewYear
    }
}
