package com.homequest.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.ui.Gate
import com.homequest.app.ui.MainViewModel
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.ConfettiBurst
import com.homequest.app.ui.components.RewardToast
import com.homequest.app.ui.navigation.HomeQuestNavHost
import com.homequest.app.ui.screens.onboarding.OnboardingScreen
import com.homequest.app.ui.screens.profile.LockScreen
import com.homequest.app.ui.theme.HomeQuestTheme
import com.homequest.app.util.LocaleHelper
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HomeQuestRoot() }
    }
}

@Composable
private fun HomeQuestRoot() {
    val vm = appViewModel { MainViewModel(it) }
    val settings = vm.settings.collectAsStateWithLifecycle().value ?: return
    val toast by vm.toast.collectAsStateWithLifecycle()
    val confetti by vm.confetti.collectAsStateWithLifecycle()
    val members by vm.members.collectAsStateWithLifecycle()
    val unlocked by vm.sessionUnlocked.collectAsStateWithLifecycle()
    val pinError by vm.pinError.collectAsStateWithLifecycle()
    val erasing by vm.erasing.collectAsStateWithLifecycle()
    val baseContext = LocalContext.current
    val localized = remember(settings.language) {
        Locale.setDefault(Locale.forLanguageTag(settings.language))
        LocaleHelper.localized(baseContext, settings.language)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides localized.resources.configuration) {
        HomeQuestTheme(themeId = settings.themeId, darkMode = settings.darkMode) {
            Surface(Modifier.fillMaxSize()) {
                Crossfade(targetState = vm.gate(settings, unlocked, erasing), animationSpec = tween(350), label = "gate") { gate ->
                    Box(Modifier.fillMaxSize()) {
                        when (gate) {
                            Gate.ONBOARDING -> OnboardingScreen(language = settings.language, onLanguage = vm::setLanguage, onComplete = vm::completeOnboarding)
                            Gate.LOCKED -> LockScreen(
                                members = members,
                                preselectedId = settings.activeMemberId,
                                pinError = pinError,
                                onClearError = vm::clearPinError,
                                onUnlock = vm::unlock
                            )
                            Gate.OPEN -> {
                                HomeQuestNavHost(onSignOut = vm::signOut, onEraseAll = vm::eraseAll)
                                ConfettiBurst(trigger = confetti)
                                Box(Modifier.align(Alignment.TopCenter)) { RewardToast(event = toast, onDismiss = vm::dismissToast) }
                            }
                        }
                    }
                }
            }
        }
    }
}
