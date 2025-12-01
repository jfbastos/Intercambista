package com.zamfir.intercambista.presentation.screen.main.exchange

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamfir.intercambista.R
import com.zamfir.intercambista.data.database.dto.ExchangeDto
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.presentation.components.AlertDialogComponent
import com.zamfir.intercambista.presentation.components.CountryFlagImageComponent
import com.zamfir.intercambista.presentation.components.HorizontalSpacerOf
import com.zamfir.intercambista.presentation.theme.IntercambistaTheme

@Composable
fun ExchangeScreenNavigation(
    exchangeUiState: ExchangeState,
    dialogState: ExchangeDialogState,
    onAction: (ExchangeIntents) -> Unit
) {
    ExchangeScreen(exchangeUiState, onAction)
    Dialogs(dialogState, onAction)
}

@Composable
private fun ExchangeScreen(uiState: ExchangeState, onAction: (ExchangeIntents) -> Unit){
    Log.d("ExchangeScreen", "Recompondo com estado: ${
        when(uiState) {
            is ExchangeState.Success -> "${uiState.mainUi.favoriteCoins.size} moedas"
            else -> uiState::class.simpleName
        }
    }")


    when (val state = uiState) {
        is ExchangeState.Error -> {}
        ExchangeState.Loading -> {}
        is ExchangeState.Success -> {
            ExchangeScreen(
                state.mainUi.baseCoin,
                state.mainUi.lastUpdate,
                state.mainUi.favoriteCoins,
                onAction
            )
        }
    }
}

@Composable
private fun Dialogs(dialogsState : ExchangeDialogState, onAction: (ExchangeIntents) -> Unit){

    when(dialogsState){
        ExchangeDialogState.NoneShown -> {}
        ExchangeDialogState.ShowConfirmationCoinChangeDialog -> {
            AlertDialogComponent(
                dialogTitle = "Confirmação",
                dialogText = "Deseja realmente alterar a moeda base?\n\nObs: suas moedas favoritadas continuaram as mesmas.",
                onConfirmation = {
                    onAction(ExchangeIntents.OnChangeCoinConfirmed)
                },
                onDismissRequest = { onAction(ExchangeIntents.OnDismissDialog) },
                icon = Icons.Default.CheckCircle
            )
        }
    }
}

@Composable
private fun ExchangeScreen(
    baseCoin: Currency,
    lastUpdate: String,
    favoriteCoins: List<ExchangeDto>,
    onAction: (ExchangeIntents) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
        //.clickable { onAction(CoinSelectionIntents.OnShowConfirmationDialog(currency)) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onTertiary),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier
                    .fillMaxSize()
            ) {
                Header(baseCoin, lastUpdate, onAction)
                ExchangesList(favoriteCoins)
            }
        }
    }
}

@Composable
private fun Header(baseCoin: Currency, lastUpdate: String, onAction : (ExchangeIntents) -> Unit) {
    Column(Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                imageVector = Icons.Rounded.DateRange,
                contentDescription = null,
                colorFilter = ColorFilter.tint(if(isSystemInDarkTheme()) Color.White else Color.Black)
            )

            Spacer(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight()
            )

            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(lastUpdate, fontSize = 12.sp)
            }
        }

        HorizontalSpacerOf(18)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            CountryFlagImageComponent(
                baseCoin.flag,
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.size(12.dp))

            val currencyName = baseCoin.info
            val code = baseCoin.code

            val countryInfo: String =
                if (currencyName.length > 15) currencyName.split(" ").first() else currencyName

            Column {
                Text(countryInfo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(code, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalSpacerOf(18)

        Row(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clickable {
                    onAction(ExchangeIntents.OnChangeBaseCoin)
                }) {

            Image(
                painterResource(if(isSystemInDarkTheme()) R.drawable.baseline_currency_exchange_24 else R.drawable.outline_currency_exchange_24_black), null, modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Trocar moeda",
                modifier = Modifier.align(Alignment.CenterVertically),
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
private fun ExchangesList(favoriteCoins: List<ExchangeDto>) {
    Log.d("ExchangesList", "Renderizando lista com ${favoriteCoins.size} moedas")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                count = favoriteCoins.size,
                itemContent = { index -> CurrencyListItem(favoriteCoins[index]) }
            )
        }
    }
}

@Composable
private fun CurrencyListItem(exchange: ExchangeDto) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
        //.clickable { onAction(CoinSelectionIntents.OnShowConfirmationDialog(currency)) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            CountryFlagImageComponent(
                exchange.flag,
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .align(Alignment.CenterVertically)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${exchange.info} - ${exchange.code}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${exchange.symbol} ${exchange.rate}",
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
    IntercambistaTheme {
        ExchangeScreen(
            baseCoin = Currency(
                code = "BRL",
                info = "Real Brasileiro",
                flag = "",
                symbol = "R$"
            ),
            favoriteCoins = listOf(
                ExchangeDto(
                    code = "BRL",
                    info = "Real Brasileiro",
                    flag = "",
                    symbol = "R$",
                    rate = "0.0"
                ),
                ExchangeDto(
                    code = "BRL",
                    info = "Real Brasileiro",
                    flag = "",
                    symbol = "R$",
                    rate = "0.0"
                ),
                ExchangeDto(
                    code = "BRL",
                    info = "Real Brasileiro",
                    flag = "",
                    symbol = "R$",
                    rate = "0.0"
                ),
                ExchangeDto(
                    code = "BRL",
                    info = "Real Brasileiro",
                    flag = "",
                    symbol = "R$",
                    rate = "0.0"
                ),
                ExchangeDto(
                    code = "BRL",
                    info = "Real Brasileiro",
                    flag = "",
                    symbol = "R$",
                    rate = "0.0"
                ),
                ExchangeDto(
                    code = "BRL",
                    info = "Real Brasileiro",
                    flag = "",
                    symbol = "R$",
                    rate = "0.0"
                ),
                ExchangeDto(
                    code = "BRL",
                    info = "Real Brasileiro",
                    flag = "",
                    symbol = "R$",
                    rate = "0.0"
                ),
                ExchangeDto(
                    code = "BRL",
                    info = "Real Brasileiro",
                    flag = "",
                    symbol = "R$",
                    rate = "0.0"
                ),
                ExchangeDto(
                    code = "BRL",
                    info = "Real Brasileiro",
                    flag = "",
                    symbol = "R$",
                    rate = "0.0"
                ),
            ),
            lastUpdate = "11/11/1111 at 11:11:11"
        ){}
    }
}