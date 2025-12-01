package com.zamfir.intercambista.presentation.screen.main.coins

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.presentation.components.CountryFlagImageComponent
import com.zamfir.intercambista.presentation.components.SimpleSearchBar
import com.zamfir.intercambista.presentation.screen.main.exchange.ExchangeIntents
import com.zamfir.intercambista.presentation.theme.IntercambistaTheme
import com.zamfir.intercambista.presentation.theme.outlineDark

@Composable
fun CoinScreenNavigation(
    coinsUiState : CoinsState,
    onAction: (CoinsIntents) -> Unit
){
    CoinsScreen(coinsUiState, onAction)
}

@Composable
private fun CoinsScreen(coinsState: CoinsState, onAction: (CoinsIntents) -> Unit){
    Surface(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onTertiary)
        ){
            Column(
                Modifier.fillMaxSize()
            ){
                SimpleSearchBar(Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp)
                ) { query ->
                    //viewModel.onIntent(OnSearchCoin(query))
                }

                when(val state  = coinsState){
                    is CoinsState.OnFailure -> {}
                    CoinsState.OnLoading -> {}
                    is CoinsState.OnSuccess -> {
                        CoinsList(state.listOfCoins, onAction)
                    }
                }

            }
        }
    }
}

@Composable
private fun CoinsList(favoriteCoins: List<Currency>, onAction: (CoinsIntents) -> Unit) {
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
                itemContent = { index -> CoinsListItem(favoriteCoins[index], onAction) }
            )
        }
    }
}

@Composable
private fun CoinsListItem(coin: Currency, onAction: (CoinsIntents) -> Unit) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().clickable { onAction(CoinsIntents.OnCoinClicked(coin)) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            CountryFlagImageComponent(
                coin.flag,
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .align(Alignment.CenterVertically)
            )


            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${coin.info} - ${coin.code}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val scale by animateFloatAsState(
                targetValue = if (coin.favorited) 1.3f else 1f,
                animationSpec = tween(150),
                label = "scaleAnim"
            )

            val starColor by animateColorAsState(
                targetValue = if (coin.favorited) Color.Yellow else outlineDark,
                animationSpec = tween(300),
                label = "starColor"
            )

            Image(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                colorFilter = ColorFilter.tint(starColor),
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )

/*            Image(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                colorFilter = ColorFilter.tint(if(coin.favorited) Color.Yellow else outlineDark)
            )*/
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
        CoinsScreen(
            coinsState = CoinsState.OnSuccess(
                listOf(
                    Currency(
                        code = "BRL",
                        info = "Real Brasileiro",
                        flag = "",
                        symbol = "R$",
                        favorited = true
                    ),
                    Currency(
                        code = "CAD",
                        info = "Dolar Canadense",
                        flag = "",
                        symbol = "$",
                        favorited = false
                    )
                )
            ),
            onAction = {}
        )
    }
}