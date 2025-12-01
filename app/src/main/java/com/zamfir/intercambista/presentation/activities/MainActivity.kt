package com.zamfir.intercambista.presentation.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.zamfir.intercambista.presentation.navigation.AppNavigation
import com.zamfir.intercambista.presentation.theme.IntercambistaTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            IntercambistaTheme {
                Surface(modifier = Modifier.fillMaxSize()){
                    AppNavigation()
                }
            }
        }
    }
}