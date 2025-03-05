package com.zamfir.intercambista.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.zamfir.intercambista.ui.components.AlertDialogComponent
import com.zamfir.intercambista.ui.components.HorizontalSpacerOf
import com.zamfir.intercambista.ui.components.ImageComponent
import com.zamfir.intercambista.ui.components.SearchfieldComponent
import com.zamfir.intercambista.ui.theme.IntercambistaTheme
import com.zamfir.intercambista.ui.theme.LightPurple
import com.zamfir.intercambista.ui.theme.PurpleBlue
import com.zamfir.intercambista.viewmodel.CurrencyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job


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
class CurrencyFavSelectionScreen : ComponentActivity() {

    private val viewModel : CurrencyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getCountries()

        setContent {
            IntercambistaTheme {

                val currenciesState by viewModel.uiCurrenciesState.collectAsStateWithLifecycle()
                val saveFavoritesState by viewModel.uiSavingFavCurrenciesState.collectAsStateWithLifecycle()

                when{
                    saveFavoritesState -> {
                        startActivity(Intent(this@CurrencyFavSelectionScreen, CurrencyActivity::class.java))
                        finish()
                    }
                    currenciesState.loadingStage != -1 -> LoadingScreen(currenciesState.loadingStage)
                    currenciesState.loadingStage == -1 && currenciesState.result.isEmpty() -> {
                        LaunchedEffect(key1 = Unit) {
                            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                viewModel.getCountries()
                            }
                        }
                    }
                    currenciesState.loadingStage == -1 -> {
                        MultipleSelectionScreen(currenciesState.result.map {
                            Currencies(
                                flagImage = it.flag,
                                coinSymbol = it.symbol,
                                coinName = it.info,
                                coinCode = it.code,
                            ).apply {
                                isSelected.value = it.favorited
                            }
                        }, onSearch = { query ->
                            viewModel.filterCurrency(query)
                        }, onFinishing = { selectedCurrencies ->
                            viewModel.saveFavCurrencies(selectedCurrencies)
                        })
                    }
                    currenciesState.exception != null -> Log.d("DEBUG", "Failed to get countries to select")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleSelectionScreen(currencies: List<Currencies>, onSearch: (String) -> Job, onFinishing: (List<String>) -> Job) {

    val selectedIndexList = remember { mutableStateOf(currencies.filter { it.isSelected.value }.map { it.coinCode }) }

    var showDialog by remember { mutableStateOf(false) }

    if(showDialog){
        AlertDialogComponent(
            dialogTitle = "Confirmação",
            dialogText = "Você selecionou ${selectedIndexList.value.size} modeas, tem certeza que deseja cancelar a seleção?",
            onConfirmation = {
                showDialog = false
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
            containerColor = PurpleBlue,
            titleContentColor = Color.White
        ),
        navigationIcon = {
            IconButton(onClick = {
                if (selectedIndexList.value.isNotEmpty()) {
                    showDialog = true
                } else {
                    showDialog = false
                    onFinishing.invoke(selectedIndexList.value.toList())
                }
            }){
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        })
    },
        floatingActionButton = {
            ExtendedFloatingActionButton(containerColor = PurpleBlue, text = { Text("Salvar") }, icon = { Icon(Icons.Filled.Check, null)}, onClick = {
                onFinishing.invoke(selectedIndexList.value.toList())
            })
        }){
        Column(
            modifier = Modifier
                .background(PurpleBlue)
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
                SearchfieldComponent(
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
        .background(if (currency.isSelected.value) LightPurple else Color.White)
        .clip(shape = RoundedCornerShape(12.dp))
        .padding(8.dp)
        .clickable {
            onItemClick.invoke(currency)
        }
    ){
        ImageComponent(
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

@Preview(showBackground = true)
@Composable
fun CurrencySelectionActivityPreview() {
    IntercambistaTheme {

    }
}