package com.zamfir.intercambista.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zamfir.intercambista.presentation.components.AlertDialogComponent
import com.zamfir.intercambista.presentation.components.HorizontalSpacerOf
import com.zamfir.intercambista.presentation.components.CountryFlagImageComponent
import com.zamfir.intercambista.presentation.components.SimpleSearchBar
import com.zamfir.intercambista.presentation.theme.IntercambistaTheme
import com.zamfir.intercambista.viewmodel.CurrencyViewModel
import dagger.hilt.android.AndroidEntryPoint


data class Currencies(
    val flagImage : String,
    val coinName : String,
    val coinSymbol : String,
    val coinCode : String,
    var isSelected: MutableState<Boolean> = mutableStateOf(false)
){
    fun toggleSelection(){
        isSelected.value = !isSelected.value
    }
}

@AndroidEntryPoint
class FavoritesSelectionScreen : ComponentActivity() {

    private val viewModel : CurrencyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getCountries()

        setContent {
            IntercambistaTheme {

                val currenciesState by viewModel.uiCurrenciesListState.collectAsStateWithLifecycle()
                val loadingState by viewModel.uiFirstLoadingCurrenciesState.collectAsStateWithLifecycle()
                val saveFavoritesState by viewModel.uiSavingFavCurrenciesState.collectAsStateWithLifecycle()

                when{
                    loadingState.progress != null -> { LoadingCurrenciesScreen() }
                    saveFavoritesState -> {
                        startActivity(Intent(this@FavoritesSelectionScreen, CurrencyActivity::class.java))
                        finish()
                    }
                    currenciesState.exception != null -> {
                        Toast.makeText(this, currenciesState.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val filteredList = currenciesState.currencies.filter { it.code != currenciesState.baseCurrency?.code }.map {
                            Currencies(
                                flagImage = it.flag,
                                coinSymbol = it.symbol,
                                coinName = it.info,
                                coinCode = it.code,
                            ).apply {
                                isSelected.value = it.favorited
                            }
                        }

                        MultipleSelectionScreen(filteredList,
                            onSearch = { query ->
                                viewModel.filterCurrency(query)
                            }, onConfirm = { selectedCurrencies ->
                                viewModel.saveFavCurrencies(selectedCurrencies)
                            }, onCancel = {
                                startActivity(Intent(this@FavoritesSelectionScreen, CurrencyActivity::class.java))
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleSelectionScreen(currencies: List<Currencies>, onSearch: (String) -> Unit, onCancel: () -> Unit, onConfirm: (List<String>) -> Unit) {

    val selectedIndexList = remember { mutableStateOf(currencies.filter { it.isSelected.value }.map { it.coinCode }) }

    var showDialog by remember { mutableStateOf(false) }

    if(showDialog){
        AlertDialogComponent(
            dialogTitle = "Confirmação",
            dialogText = "Você selecionou ${selectedIndexList.value.size} moedas, tem certeza que deseja cancelar a seleção?",
            onConfirmation = {
                showDialog = false
                onCancel.invoke()
            },
            onDismissRequest = {
                showDialog = false
            },
            icon = Icons.Default.CheckCircle
        )
    }

    Scaffold(topBar = { TopAppBar(
        title = {Text("Seleção de moedas")},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = Color.White
        ),
        navigationIcon = {
            IconButton(onClick = {
                if (selectedIndexList.value.isNotEmpty()) {
                    showDialog = true
                } else {
                    showDialog = false
                    onCancel.invoke()
                }
            }){
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        })
    },
        floatingActionButton = {
            ExtendedFloatingActionButton(containerColor = MaterialTheme.colorScheme.background, text = { Text("Salvar") }, icon = { Icon(Icons.Filled.Check, null)}, onClick = {
                onConfirm.invoke(selectedIndexList.value.toList())
            })
        }){
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .fillMaxSize()
                .padding(it)
        ) {

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                SimpleSearchBar(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) { query ->
                    onSearch.invoke(query)
                }

                HorizontalSpacerOf(12)

                CurrencyListA(currencies) { currency ->
                    currency.toggleSelection()
                    selectedIndexList.value = if(currency.isSelected.value){
                        selectedIndexList.value.plus(currency.coinCode)
                    }else{
                        selectedIndexList.value.minus(currency.coinCode)
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyItem(currency : Currencies, onItemClick: (Currencies) -> Unit){

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
        .background(if (currency.isSelected.value) MaterialTheme.colorScheme.background else Color.White)
        .clip(shape = RoundedCornerShape(12.dp))
        .padding(8.dp)
        .clickable {
            onItemClick.invoke(currency)
        }
    ){
        CountryFlagImageComponent(
            currency.flagImage,
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier
            .fillMaxHeight()
            .width(12.dp))

        Text(text = currency.coinName, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 18.sp, color = Color.Black)

        Spacer(modifier = Modifier.weight(1.0f))

        Text(text = currency.coinSymbol, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 16.sp, color = Color.Black)
    }
}

@Composable
fun CurrencyListA(currencies: List<Currencies>, onItemClick: (Currencies) -> Unit){
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(
            count = currencies.size,
            key = {
                currencies[it].coinCode
            },
            itemContent = { index ->
                val currencyItem = currencies[index]
                CurrencyItem(currencyItem) { onItemClick.invoke(it) }
            }
        )
    }
}

@Composable
fun LoadingCurrenciesScreen(){

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

            Text("Obtendo moedas salvas...", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White)

        }
    }
}