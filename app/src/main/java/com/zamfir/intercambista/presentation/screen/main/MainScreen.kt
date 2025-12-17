package com.zamfir.intercambista.presentation.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zamfir.intercambista.R
import com.zamfir.intercambista.presentation.screen.commom.NavigationEvents
import com.zamfir.intercambista.presentation.screen.main.coins.CoinScreenNavigation
import com.zamfir.intercambista.presentation.screen.main.coins.CoinsViewModel
import com.zamfir.intercambista.presentation.screen.main.exchange.ExchangeIntents
import com.zamfir.intercambista.presentation.screen.main.exchange.ExchangeScreenNavigation
import com.zamfir.intercambista.presentation.screen.main.exchange.ExchangeViewModel
import com.zamfir.intercambista.presentation.screen.main.navigation.MainScreenNavDestination


@Composable
fun MainScreenNavigation(
    navController: NavController,
    windowSizeClass : WindowSizeClass,
    viewModel: MainViewModel = hiltViewModel()
){

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when(event){
                NavigationEvents.GoToCoinSelectionScreen ->  {
                    navController.navigate("CoinSelectionScreen"){
                        popUpTo("MainScreen") { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    MainScreen(viewModel, windowSizeClass)
}

@Composable
fun MainScreen(viewModel: MainViewModel, windowSizeClass: WindowSizeClass) {

    val navState by viewModel.navState.collectAsStateWithLifecycle()

    val bottomNavController = rememberNavController()

    LaunchedEffect(navState) {
        val route = when(navState.current){
            MainScreenNavDestination.Calculator -> "calculator"
            MainScreenNavDestination.Coins -> "coins"
            MainScreenNavDestination.Exchange -> "exchange"
        }

        bottomNavController.navigate(route){
            popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = false
        }
    }

    LaunchedEffect(bottomNavController) {
        bottomNavController.currentBackStackEntryFlow.collect { backStackEntry ->
            when (backStackEntry.destination.route) {
                "exchange" -> viewModel.onIntent(MainScreenIntent.NavigateTo(MainScreenNavDestination.Exchange))
                "coins" -> viewModel.onIntent(MainScreenIntent.NavigateTo(MainScreenNavDestination.Coins))
                "calculator" -> viewModel.onIntent(MainScreenIntent.NavigateTo(MainScreenNavDestination.Calculator))
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onTertiary)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            MainBottomBar(current = navState.current) {
                viewModel.onIntent(MainScreenIntent.NavigateTo(it))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MainBottomNavHost(
                windowSizeClass = windowSizeClass,
                navController = bottomNavController,
                onAction = viewModel::onIntent
            )
        }
    }
}

@Composable
fun MainBottomNavHost(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController,
    onAction: (MainScreenIntent) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "exchange"
    ) {
        composable("exchange") { backStackEntry ->
            val exchangeVm : ExchangeViewModel = hiltViewModel()
            val uiState by exchangeVm.state.collectAsState()
            val dialogState by exchangeVm.dialogState.collectAsState()

            val currentEntry by navController.currentBackStackEntryAsState()
            val isCurrentDestination = currentEntry?.destination?.route == "exchange"

            LaunchedEffect(isCurrentDestination) {
                if (isCurrentDestination) {
                    exchangeVm.onIntent(ExchangeIntents.OnRefresh)
                }
            }

            LaunchedEffect(Unit) {
                exchangeVm.onCoinChange.collect { event ->
                    onAction(MainScreenIntent.OnReturnToCoinSelection)
                }
            }

            ExchangeScreenNavigation(
                uiState,
                dialogState,
                windowSizeClass,
                exchangeVm::onIntent
            )
        }

        composable("coins") {
            val coinsVm : CoinsViewModel = hiltViewModel()
            val uiState by coinsVm.state.collectAsStateWithLifecycle()

            CoinScreenNavigation(uiState, coinsVm::onIntent)
        }

        composable("calculator") {
            CalculatorScreen()
        }
    }
}

@Composable
fun CalculatorScreen(){
    Text("Calculadora")
}

@Composable
fun MainBottomBar(
    current: MainScreenNavDestination,
    onItemClick: (MainScreenNavDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.onTertiary,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = current == MainScreenNavDestination.Exchange,
            onClick = { onItemClick(MainScreenNavDestination.Exchange) },
            icon = { Icon(painterResource(R.drawable.baseline_currency_exchange_24), contentDescription = "Cotações") },
            label = { Text("Cotações") }
        )

        NavigationBarItem(
            selected = current == MainScreenNavDestination.Coins,
            onClick = { onItemClick(MainScreenNavDestination.Coins) },
            icon = { Icon(painterResource(R.drawable.outline_universal_currency_alt_24), contentDescription = "Moedas") },
            label = { Text("Moedas") }
        )

        NavigationBarItem(
            selected = current == MainScreenNavDestination.Calculator,
            onClick = { onItemClick(MainScreenNavDestination.Calculator) },
            icon = { Icon(painterResource(R.drawable.rounded_calculate_24), contentDescription = "Calculadora") },
            label = { Text("Calculadora") }
        )
    }
}