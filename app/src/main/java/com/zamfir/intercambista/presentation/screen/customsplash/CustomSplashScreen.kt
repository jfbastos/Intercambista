package com.zamfir.intercambista.presentation.screen.customsplash

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zamfir.intercambista.R
import com.zamfir.intercambista.presentation.components.SimpleAlertDialog
import com.zamfir.intercambista.presentation.screen.commom.NavigationEvents
import kotlinx.coroutines.delay

@Composable
fun CustomSplashScreenNavigation(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            Log.d("theming", "Event -> $event")
            when(event){
                NavigationEvents.GoToCoinSelectionScreen -> {
                    navController.navigate("CoinSelectionScreen") {
                        popUpTo("CustomSplashScreen") { inclusive = true }
                    }
                }
                NavigationEvents.GoToMainScreen ->  {
                    navController.navigate("MainScreen"){
                        popUpTo("CustomSplashScreen") { inclusive = true }
                    }
                }
            }
        }
    }

    CustomSplashScreen(viewModel)

    ConfigDialogs(viewModel)
}

@Composable
private fun ConfigDialogs(viewModel : SplashViewModel){

    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()

    when(val state = dialogState){
        SplashDialogsState.OnNoneShown -> {}
        is SplashDialogsState.OnShowErrorDialog -> {
            SimpleAlertDialog(state.msg) {
                viewModel.onIntent(SplashIntents.OnDismissDialog)
            }
        }
    }
}

@Composable
fun CustomSplashScreen(viewModel: SplashViewModel) {

    val uiState by viewModel.splashState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            SplashState.OnCurrencyAlreadySelected -> {
                viewModel.onIntent(SplashIntents.OnNavigateMainScreen)
            }
            SplashState.OnSuccess -> {
                viewModel.onIntent(SplashIntents.OnNavigateCoinSelection)
            }
            is SplashState.OnFailure -> {
                viewModel.onIntent(SplashIntents.OnFailure(state.error))
            }
            else -> {}
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {

        RotatingIcon()

        when(val state = uiState){
            is SplashState.Loading -> {
                Text(
                    text = state.msg,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            else -> {}
        }
    }
}

@Composable
fun RotatingIcon() {

    var rotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            animate(
                initialValue = rotation,
                targetValue = rotation + 180f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = LinearEasing
                )
            ) { value, _ ->
                rotation = value
            }
        }
    }

    Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = "Carregando",
        modifier = Modifier
            .size(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .graphicsLayer {
                rotationZ = rotation % 360f
            }
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight"
)
@Composable
private fun Preview() {
    //CustomSplashScreen(SplashState.Loading("Carregando")) {}
}

