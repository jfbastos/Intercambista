package com.zamfir.intercambista.presentation.screen.coinselection

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.presentation.components.AlertDialogComponent
import com.zamfir.intercambista.presentation.components.CountryFlagImageComponent
import com.zamfir.intercambista.presentation.components.CustomErrorDialog
import com.zamfir.intercambista.presentation.components.HorizontalSpacerOf
import com.zamfir.intercambista.presentation.components.SimpleSearchBar
import com.zamfir.intercambista.presentation.screen.coinselection.CoinSelectionIntents.OnCoinSelectionConfirmed
import com.zamfir.intercambista.presentation.screen.coinselection.CoinSelectionIntents.OnDismissDialog
import com.zamfir.intercambista.presentation.screen.coinselection.CoinSelectionIntents.OnSearchCoin
import com.zamfir.intercambista.presentation.screen.commom.CurrencyCardShimmer
import com.zamfir.intercambista.presentation.screen.commom.NavigationEvents

@Composable
fun CoinSelectionNavigation(
    navController : NavController,
    viewModel : CoinSelectionViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when(event){
                NavigationEvents.GoToMainScreen ->  {
                    navController.navigate("MainScreen"){
                        popUpTo("CoinSelectionScreen") { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    ConfigDialogs(viewModel)
    CoinSelectionScreen(viewModel)
}

@Composable
fun CoinSelectionScreen(viewModel: CoinSelectionViewModel) {

    val uiState by viewModel.currenciesState.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {

        Text("Selecione uma Moeda", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        HorizontalSpacerOf(12)

        SimpleSearchBar(Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
        ) { query ->
            viewModel.onIntent(OnSearchCoin(query))
        }

        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(24.dp))
                .fillMaxSize()
        ) {
            ConfigScreenInfo(uiState, viewModel::onIntent)
        }
    }
}

@Composable
private fun ConfigScreenInfo(
    uiState: CoinSelectionState,
    onAction: (CoinSelectionIntents) -> Unit
) {

    LaunchedEffect(uiState::class) {
        when(uiState){
            is CoinSelectionState.OnFailure -> {
                onAction(CoinSelectionIntents.OnShowErrorDialog(uiState.msg, uiState.exception) )
            }
            else -> {}
        }
    }

    when (uiState) {
        is CoinSelectionState.Loading -> {
            Log.d("theming", "Loading")
            LoadingScreen()
        }

        is CoinSelectionState.OnFailure -> {}

        is CoinSelectionState.OnSuccess -> {
            CurrencyList(uiState.currencies, onAction)
        }
    }
}

@Composable
private fun ConfigDialogs(viewModel: CoinSelectionViewModel) {
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()

    when (val state = dialogState) {
        CoinSelectionDialogsState.OnNoneShown -> {}
        is CoinSelectionDialogsState.OnShowErrorDialog -> {
            CustomErrorDialog(
                mensagem = state.msg,
                stackTrace = state.exception?.stackTraceToString() ?: "",
                isDismissable = true,
                onConfirm = { viewModel.onIntent(OnDismissDialog) },
                onDismiss = { viewModel.onIntent(OnDismissDialog) }
            )
        }
        is CoinSelectionDialogsState.OnShowSelectionConfirmation -> {
            AlertDialogComponent(
                dialogTitle = "Confirmação",
                dialogText = "Deseja realmente utilizar ${state.currency.info} - ${state.currency.code} como moeda principal?\n\nNão se preocupe, você poderá muda-la posteriormente.",
                onConfirmation = {
                    viewModel.onIntent(OnCoinSelectionConfirmed(state.currency.code))
                },
                onDismissRequest = { viewModel.onIntent(OnDismissDialog) },
                icon = Icons.Default.CheckCircle
            )
        }

        CoinSelectionDialogsState.OnShowLoadingFavoritesInfo -> {
            DialogLoading()
        }
    }
}

@Composable
fun DialogLoading(onDismissRequest: () -> Unit = {}){
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .sizeIn(minWidth = 240.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Carregando cotas das moedas favoritadas",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        repeat(6) {
            CurrencyCardShimmer()
        }
    }
}

@Composable
fun CurrencyList(currencies: List<Currency>, onAction: (CoinSelectionIntents) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(
            count = currencies.size,
            key = {
                currencies[it].id
            },
            itemContent = { index ->
                val currencyItem = currencies[index]
                CurrencyListItem(currency = currencyItem, onAction = onAction)
            }
        )
    }
}

@Composable
fun CurrencyListItem(currency : Currency, onAction: (CoinSelectionIntents) -> Unit){
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction(CoinSelectionIntents.OnShowConfirmationDialog(currency)) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            CountryFlagImageComponent(
                currency.flag,
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .align(Alignment.CenterVertically)
            )

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = currency.info,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight"
)
@Composable
private fun Preview() {
    ConfigScreenInfo(
        CoinSelectionState.OnSuccess(
            currencies =
                listOf(
                    Currency(
                        code = "BRL",
                        info = "Real Brasileiro",
                        flag = "",
                        symbol = "R$"
                    )
                )
        )
    ) { }
}