package com.zamfir.intercambista.presentation.dialog

import android.util.Log
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.presentation.components.HorizontalSpacerOf
import com.zamfir.intercambista.presentation.components.CountryFlagImageComponent
import com.zamfir.intercambista.presentation.components.SimpleSearchBar
import com.zamfir.intercambista.viewmodel.CurrencyViewModel

@Composable
fun BaseCurrencySelectionDialog(showDialog : MutableState<Boolean>, currencies : List<Currency>, onDismiss : () -> Unit, onSearch : (String) -> Unit, onSelect : (Currency) -> Unit) {
    if(showDialog.value){
        Dialog(onDismissRequest = { showDialog.value = false; onDismiss() }){
            Column(
                Modifier
                    .clip(shape = RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .height(500.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp)

            ) {
                Column {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Escolha uma nova moeda de base: ",
                            fontSize = 16.sp,
                            color = Color.White)
                    }

                    HorizontalSpacerOf(8)
                }

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

                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ){
                        items(
                            count = currencies.size,
                            key = {
                                currencies[it].id
                            },
                            itemContent = { index ->
                                val currencyItem = currencies[index]
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                                    .clip(shape = RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(8.dp)
                                    .clickable { onSelect.invoke(currencyItem) }
                                ){
                                    CountryFlagImageComponent(
                                        currencyItem.flag,
                                        Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .align(Alignment.CenterVertically)
                                    )

                                    Spacer(modifier = Modifier
                                        .fillMaxHeight()
                                        .width(12.dp))

                                    Text(text = currencyItem.info, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 18.sp, color = Color.Black)

                                    Spacer(modifier = Modifier.weight(1.0f))

                                    Text(text = currencyItem.symbol.ifBlank { "-" }, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 16.sp, color = Color.Black)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShowCountrySelection(showSelectionDialog: MutableState<Boolean>, viewModel: CurrencyViewModel, onFinish : () -> Unit){

    val currencies by viewModel.uiCurrenciesListState.collectAsStateWithLifecycle()
    val saveState by viewModel.uiSaveCurrencyState.collectAsStateWithLifecycle()

    if(saveState.isSuccess){
        showSelectionDialog.value = false
        onFinish.invoke()
    }

    if(currencies.exception != null){
        Log.e("DEBUG", "Falha ao obter valores de moedas na tela de seleção.")
    }else{
        BaseCurrencySelectionDialog(showSelectionDialog, currencies.currencies,
            onDismiss = {},
            onSearch = { query ->
                viewModel.filterCurrency(query)
            },
            onSelect = { currency ->
                viewModel.setNewBaseCurrency(currency)
            })
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDialog() {
    val currencyList = listOf(
        Currency(
            id = 1,
            code = "BRL",
            info = "Real",
            symbol = "R$",
            flag = ""
        ), Currency(
            id = 2,
            code = "USD",
            info = "Dolar americano",
            symbol = "$",
            flag = ""
        )
    )

    val showDialog = remember { mutableStateOf(true) }
    BaseCurrencySelectionDialog(showDialog, currencyList, onDismiss = {}, onSearch = {}, onSelect = {})
}