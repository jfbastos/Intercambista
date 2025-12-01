package com.zamfir.intercambista.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.data.enums.FetchCurrencyInfoStages
import com.zamfir.intercambista.presentation.components.AlertDialogComponent
import com.zamfir.intercambista.presentation.components.HorizontalSpacerOf
import com.zamfir.intercambista.presentation.components.CountryFlagImageComponent
import com.zamfir.intercambista.presentation.components.SimpleSearchBar
import com.zamfir.intercambista.presentation.theme.IntercambistaTheme
import com.zamfir.intercambista.viewmodel.CurrencyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FirstAccessActivity : ComponentActivity(){

    private val viewModel : CurrencyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getCountries()

        setContent {
            IntercambistaTheme {
                val currenciesState by viewModel.uiCurrenciesListState.collectAsStateWithLifecycle()
                val loadingState by viewModel.uiFirstLoadingCurrenciesState.collectAsStateWithLifecycle()
                val saveState by viewModel.uiSaveCurrencyState.collectAsStateWithLifecycle()

                if(saveState.isSuccess){
                    startActivity(Intent(this@FirstAccessActivity, CurrencyActivity::class.java))
                    finish()
                }

                when{
                    loadingState.progress != null -> LoadingScreen(loadingState.progress)
                    currenciesState.exception != null -> Log.e("DEBUG", "Failed to get countries to select")
                    else -> {
                        SelectCurrencyScreen(currenciesState.currencies, onSearch = { query ->
                            viewModel.filterCurrency(query)
                        }, onItemClick = { currency ->
                            viewModel.setBaseCurrency(currency)
                        })
                    }
                }
            }
        }
    }
}


@Composable
fun LoadingScreen(progress: FetchCurrencyInfoStages?){
    var loadingCurrency by remember { mutableStateOf(false) }
    var loadingCountryInfo by remember { mutableStateOf(false) }
    var loadingCurrencies by remember { mutableStateOf(false) }

    Box(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier
            .align(Alignment.Center)
            .size(200.dp)){
            CircularProgressIndicator(
                modifier = Modifier
                    .size(100.dp)
                    .padding(12.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )

            HorizontalSpacerOf(12)

            AnimatedVisibility(visible = loadingCurrency) {
                Text(progress!!.info, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White)
            }

            AnimatedVisibility(visible = loadingCountryInfo) {
                Text(progress!!.info, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White)
            }

            AnimatedVisibility(visible = loadingCurrencies) {
                Text(progress!!.info, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White)
            }

            when (progress) {
                FetchCurrencyInfoStages.LOADING_DB_CURRENCIES -> {
                    loadingCurrencies = true
                    loadingCountryInfo = false
                    loadingCurrency = false
                }
                FetchCurrencyInfoStages.LOADING_AVAILABLE_CURRENCIES -> {
                    loadingCurrencies = false
                    loadingCountryInfo = false
                    loadingCurrency = true
                }
                FetchCurrencyInfoStages.LOADING_INFO_CURRENCIES -> {
                    loadingCurrencies = false
                    loadingCountryInfo = true
                    loadingCurrency = false
                }

                null -> {
                    Log.d("DEBUG", "Status null")
                }
            }
        }
    }
}

@Composable
private fun SelectCurrencyScreen(currencies: List<Currency>, onSearch: (String) -> Unit, onItemClick: (Currency) -> Unit) {
    Column(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Bem vindo(a), ", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            HorizontalSpacerOf(12)

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "escolha uma moeda para iniciarmos: ",
                    fontSize = 16.sp,
                    color = Color.White)
            }

            HorizontalSpacerOf(12)
        }

        HorizontalSpacerOf(0)

        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(18.dp))
                .background(Color.White)
                .fillMaxWidth()
                .fillMaxSize()
        ) {

            SimpleSearchBar(Modifier
                .fillMaxWidth()
                .padding(8.dp)
            ) { query -> onSearch.invoke(query) }

            CurrencyList(currencies = currencies) { onItemClick.invoke(it) }
        }
    }
}

@Composable
fun CurrencyList(currencies : List<Currency>, onItemClick: (Currency) -> Unit){
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(
            count = currencies.size,
            key = {
                currencies[it].id
            },
            itemContent = { index ->
                val currencyItem = currencies[index]
                CurrencyListItem(currencyItem) { onItemClick.invoke(currencyItem) }
            }
        )
    }
}

@Composable
fun CurrencyListItem(currency : Currency, onItemClick : () -> Unit){

    var showDialog by remember { mutableStateOf(false) }

    if(showDialog){
        AlertDialogComponent(
            dialogTitle = "Confirmação",
            dialogText = "Deseja realmente utilizar ${if(currency.symbol.isNotBlank()) currency.symbol + " - " else ""}${currency.info} como moeda principal?\n\nNão se preocupe, você poderá muda-la posteriormente.",
            onConfirmation = {
                onItemClick.invoke()
                showDialog = false
            },

            onDismissRequest = {
                showDialog = false
            },
            icon = Icons.Default.CheckCircle
        )
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
        .clip(shape = RoundedCornerShape(12.dp))
        .background(Color.White)
        .padding(8.dp)
        .clickable { showDialog = true }
    ){

        CountryFlagImageComponent(
            currency.flag,
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier
            .fillMaxHeight()
            .width(12.dp))

        Text(text = currency.info, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 18.sp, color = Color.Black)

        Spacer(modifier = Modifier.weight(1.0f))

        Text(text = currency.symbol.ifBlank { "-" }, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 16.sp, color = Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun FirstAccessPreview() {
    IntercambistaTheme {}
}