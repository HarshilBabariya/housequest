package com.homequest.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val settings by vm.settings.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val confetti by vm.confetti.collectAsStateWithLifecycle()
    val members by vm.members.collectAsStateWithLifecycle()
    val unlocked by vm.sessionUnlocked.collectAsStateWithLifecycle()
    val pinError by vm.pinError.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    HomeQuestTheme(themeId = settings.themeId, darkMode = settings.darkMode) {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                when (vm.gate(settings, unlocked)) {
                    Gate.ONBOARDING -> OnboardingScreen(onComplete = vm::completeOnboarding)
                    Gate.LOCKED -> LockScreen(
                        members = members,
                        preselectedId = settings.activeMemberId,
                        pinError = pinError,
                        onClearError = vm::clearPinError,
                        onUnlock = vm::unlock
                    )
                    Gate.OPEN -> {
                        HomeQuestNavHost(onSignOut = vm::signOut)
                        ConfettiBurst(trigger = confetti)
                        Box(Modifier.align(Alignment.TopCenter)) { RewardToast(event = toast, onDismiss = vm::dismissToast) }
                    }
                }
            }
        }
    }
}
