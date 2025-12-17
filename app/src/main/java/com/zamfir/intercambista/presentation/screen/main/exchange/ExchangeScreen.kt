package com.zamfir.intercambista.presentation.screen.main.exchange

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.zamfir.intercambista.data.enums.SortOption
import com.zamfir.intercambista.presentation.components.AlertDialogComponent
import com.zamfir.intercambista.presentation.components.CountryFlagImageComponent
import com.zamfir.intercambista.presentation.components.CustomErrorDialog
import com.zamfir.intercambista.presentation.components.HorizontalSpacerOf
import com.zamfir.intercambista.presentation.screen.commom.DialogLoading
import com.zamfir.intercambista.presentation.theme.IntercambistaTheme

@Composable
fun ExchangeScreenNavigation(
    exchangeUiState: ExchangeState,
    dialogState: ExchangeDialogState,
    windowSizeClass: WindowSizeClass,
    onAction: (ExchangeIntents) -> Unit
) {
    ExchangeScreen(exchangeUiState, windowSizeClass,onAction)
    Dialogs(dialogState, onAction)
}

@Composable
private fun ExchangeScreen(
    uiState: ExchangeState,
    windowSizeClass: WindowSizeClass,
    onAction: (ExchangeIntents) -> Unit
){

    LaunchedEffect(uiState) {
        when(uiState){
            ExchangeState.UpdatingCoins -> onAction(ExchangeIntents.OnLoadingDialog)
            is ExchangeState.Error -> onAction(ExchangeIntents.OnFailureDialog(uiState.msg, uiState.exception))
            else -> {}
        }
    }

    when (val state = uiState) {
        is ExchangeState.Error -> {}
        ExchangeState.Loading -> {}
        ExchangeState.UpdatingCoins -> {}
        is ExchangeState.Success -> {
            SetOrientation(windowSizeClass.widthSizeClass, state.mainUi.baseCoin,
                state.mainUi.lastUpdate,
                state.mainUi.favoriteCoins,
                state.sortType,
                onAction)
        }
    }
}

@Composable
private fun SetOrientation(
    orientation: WindowWidthSizeClass,
    baseCoin: Currency,
    lastUpdate: String,
    favoriteCoins: List<ExchangeDto>,
    sortType: SortOption,
    onAction: (ExchangeIntents) -> Unit
){
    when (orientation) {
        WindowWidthSizeClass.Compact -> {
            ExchangeScreenPortrait(
                baseCoin,
                lastUpdate,
                favoriteCoins,
                sortType,
                onAction
            )
        }
        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded -> {
            ExchangeScreenLandscape(
                baseCoin,
                lastUpdate,
                favoriteCoins,
                sortType,
                onAction
            )
        }
    }
}

@Composable
private fun Dialogs(dialogsState : ExchangeDialogState, onAction: (ExchangeIntents) -> Unit){

    when(val state = dialogsState){
        ExchangeDialogState.NoneShown -> {}
        ExchangeDialogState.ShowConfirmationCoinChangeDialog -> {
            AlertDialogComponent(
                dialogTitle = "Confirmação",
                dialogText = "Deseja realmente alterar a moeda base?\n\nObs: suas moedas favoritadas continuaram as mesmas.",
                onConfirmation = {
                    onAction(ExchangeIntents.OnChangeCoinConfirmed)
                },
                onDismissRequest = { onAction(ExchangeIntents.OnDismissDialog) },
                icon = painterResource(R.drawable.outline_check_circle_24)
            )
        }

        ExchangeDialogState.ShowLoadingDialog -> {
            DialogLoading("Atualizando moedas...")
        }

        is ExchangeDialogState.ShowErrorDialog -> {
            CustomErrorDialog(
                mensagem = state.msg,
                stackTrace = state.exception?.stackTraceToString() ?: "",
                isDismissable = true,
                onConfirm = { onAction(ExchangeIntents.OnDismissDialog) },
                onDismiss = { onAction(ExchangeIntents.OnDismissDialog) }
            )
        }
    }
}

@Composable
private fun ExchangeScreenPortrait(
    baseCoin: Currency,
    lastUpdate: String,
    favoriteCoins: List<ExchangeDto>,
    sortType: SortOption,
    onAction: (ExchangeIntents) -> Unit
) {
    Surface {
        Column(
            Modifier
                .fillMaxSize().background(MaterialTheme.colorScheme.onTertiary),
        ) {
            Header(baseCoin, lastUpdate,sortType, onAction)
            ExchangesList(favoriteCoins, onAction)
        }
    }
}


@Composable
private fun ExchangeScreenLandscape(
    baseCoin: Currency,
    lastUpdate: String,
    favoriteCoins: List<ExchangeDto>,
    sortType: SortOption,
    onAction: (ExchangeIntents) -> Unit
) {
    Surface {
        Row(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.onTertiary),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Header(baseCoin, lastUpdate, sortType, onAction)
            }

            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            ) {
                ExchangesList(favoriteCoins, onAction)
            }
        }
    }
}

@Composable
private fun Header(
    baseCoin: Currency,
    lastUpdate: String,
    sortType: SortOption,
    onAction: (ExchangeIntents) -> Unit
) {
    Column(Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier
                .height(20.dp)
                .padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.baseline_cloud_download_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
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
            modifier = Modifier.height(50.dp), verticalAlignment = Alignment.CenterVertically
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

        Row(modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
            BotaoTrocarMoeda(onAction)

            Spacer(modifier = Modifier.weight(1f))

            ElevatedFilterChip(
                onClick = { onAction(ExchangeIntents.OnFilter) },
                label = when(sortType){
                    SortOption.ASCENDING ->  {{ Text("Valor - Crescente") }}
                    SortOption.DESCENDING -> {{ Text("Valor - Decrescente") }}
                },
                selected = false,
                trailingIcon =when(sortType) {
                    SortOption.ASCENDING -> {
                        {
                            Icon(
                                painter = painterResource(R.drawable.rounded_arrow_drop_up_24),
                                contentDescription = "Asc icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    }

                    SortOption.DESCENDING -> {
                        {
                            Icon(
                                painter = painterResource(R.drawable.rounded_arrow_drop_down_24),
                                contentDescription = "desc icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BotaoTrocarMoeda(onAction: (ExchangeIntents) -> Unit) {
    Row(
        modifier = Modifier
            .clickable {
                onAction(ExchangeIntents.OnChangeBaseCoin)
            }
    ) {
        Image(
            painterResource(R.drawable.rounded_compare_arrows_24), null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
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

@Composable
private fun ExchangesList(favoriteCoins: List<ExchangeDto>, onAction: (ExchangeIntents) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {

        LazyColumn(
            modifier = Modifier
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
@Composable
private fun Preview() {
    IntercambistaTheme {
        ExchangeScreenPortrait(
            baseCoin = Currency(
                code = "BRL",
                info = "Real Brasileiro",
                flag = "",
                symbol = "R$"
            ),
            lastUpdate = "11/11/1111 at 11:11:11",
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
            sortType = SortOption.DESCENDING
        ) {}
    }
}