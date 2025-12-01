package com.zamfir.intercambista.presentation.screen.commom

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun ForceWhiteStatusBarIcons() {
    val activity = LocalActivity.current as ComponentActivity
    val window = activity.window

    SideEffect {
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false
    }
}