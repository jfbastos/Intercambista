package com.zamfir.intercambista.presentation.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zamfir.intercambista.R
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.presentation.components.HorizontalSpacerOf
import com.zamfir.intercambista.presentation.components.CountryFlagImageComponent
import com.zamfir.intercambista.presentation.dialog.ShowCountrySelection
import com.zamfir.intercambista.presentation.theme.IntercambistaTheme
import com.zamfir.intercambista.viewmodel.CurrencyViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class CurrencyActivity : ComponentActivity() {

    private val viewModel : CurrencyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getMainScreenInfo(1.0)

        setContent {
            IntercambistaTheme{
                val homeInfo by viewModel.uiMainScreenState.collectAsStateWithLifecycle()

                val showSelectionDialog = remember { mutableStateOf(false) }

                ShowCountrySelection(showSelectionDialog,viewModel){
                    viewModel.getMainScreenInfo(1.0)
                }

                Scaffold (
                    floatingActionButton = {
                        FloatingActionButton(containerColor = MaterialTheme.colorScheme.background, contentColor = Color.White ,onClick = {
                            startActivity(Intent(this@CurrencyActivity, FavoritesSelectionScreen::class.java))
                            finish()
                        }) {
                            Icon(Icons.Filled.Add, null)
                        }
                    }
                ) { padding ->
                    Column(Modifier
                        .background(color = Color.Gray)
                        .padding(padding)) {
                        BaseCurrencyHeader(
                            homeInfo.baseCurrency,
                            homeInfo.lastUpdateDatetime,
                            onBaseValueChange = { value ->
                                viewModel.getMainScreenInfo(value.toDoubleOrNull() ?: 1.0)
                            },
                            onChangeCurrency = {
                                viewModel.getCountries()
                                showSelectionDialog.value = true
                            }
                        )
                        AllCurrenciesList(homeInfo.favoritesCurrencies, homeInfo.exchanges)
                    }
                }
            }
        }
    }
}

private fun getDataHoraAgoraFormatada() : String{
    val agora = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
    val dataFormatada = agora.format(formatter)
    return dataFormatada
}

@Composable
fun BaseCurrencyHeader(baseCurrency: Currency?, lastUpdate : String, onBaseValueChange : (String) -> Unit, onChangeCurrency : () -> Unit) {

    var text by remember { mutableStateOf("1") }

    Column( modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
        .background(color = MaterialTheme.colorScheme.background)
        .padding(8.dp)
    ){
        Row(modifier = Modifier.fillMaxWidth()){
            Image(imageVector = Icons.Default.DateRange, contentDescription = null, colorFilter = ColorFilter.tint(Color.White))

            Spacer(modifier = Modifier.size(12.dp))

            Text(getDataHoraAgoraFormatada(), color = Color.White, fontSize = 12.sp)

            Spacer(modifier = Modifier.weight(1.0f))


        }

        HorizontalSpacerOf(6)

        Row(modifier = Modifier.fillMaxWidth()){
            Image(imageVector = Icons.Default.Refresh, contentDescription = null, colorFilter = ColorFilter.tint(Color.White))

            Spacer(modifier = Modifier.size(12.dp))

            Text(lastUpdate, color = Color.White,  fontSize = 12.sp)
        }

        HorizontalSpacerOf(14)

        Row{
            Row(Modifier.align(alignment = Alignment.CenterVertically)) {
                CountryFlagImageComponent(
                    baseCurrency?.flag ?: "",
                    Modifier
                        .size(height = 48.dp, width = 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.size(12.dp))

                val currencyName = baseCurrency?.info ?: "-"
                val code = baseCurrency?.code ?: "-"

                val countryInfo  : String = /*if(currencyName.length > 15) currencyName.split(" ").first() else*/ currencyName

                Column {
                    Text(countryInfo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(code, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1.0f))

            Column {
                Text("Base de cálculo", color = Color.White, modifier = Modifier.align(Alignment.End), fontSize = 12.sp)

               TextField(
                    value = text,
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp, textAlign = TextAlign.End),
                    shape = RoundedCornerShape(8.dp),
                    prefix = { Text (baseCurrency?.symbol ?: "", color = Color.White) },
                    modifier = Modifier
                        .align(Alignment.End)
                        .width(130.dp),
                    onValueChange = {
                        val newString = it.filter { char ->
                            char == ".".first() || char == ",".first()
                        }
                        if (newString.length <= 1)
                            text = it
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor =  Color.Transparent,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.background
                    ),
                    singleLine = true,
                    keyboardActions = KeyboardActions(onDone = {
                        if(text.isNotBlank()) onBaseValueChange.invoke(text)
                        else{
                            text = "1.0"
                            onBaseValueChange.invoke(text)
                        }
                    }),
                )
            }
        }

        HorizontalSpacerOf(16)

        Row(modifier = Modifier.clickable {
            onChangeCurrency.invoke()
        }) {
            Image(painterResource(R.drawable.baseline_currency_exchange_24), null, modifier = Modifier.size(16.dp).align(Alignment.CenterVertically))

            Spacer(modifier = Modifier.width(6.dp))

            Text(text = "Trocar moeda", modifier = Modifier.align(Alignment.CenterVertically), color = Color.White,  fontSize = 14.sp, textDecoration = TextDecoration.Underline)
        }
    }
}

@Composable
private fun CurrencyListItem(currency : Currency, exchangeValue : Double?){
    Box(Modifier
        .clip(RoundedCornerShape(12.dp))
        .fillMaxWidth()
        .background(Color.White)
        .padding(12.dp)){
        CountryFlagImageComponent(
            currency.flag,
            Modifier
                .size(width = 42.dp, height = 32.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.TopStart)
        )

        Column (modifier = Modifier
            .align(Alignment.BottomEnd)
            .fillMaxHeight()){
            Text("${currency.info} - ${currency.code}", modifier = Modifier.align(Alignment.End), fontSize = 18.sp, color = Color.Black)

            HorizontalSpacerOf(12)

            Text("${currency.symbol} ${formatExchangeValue(valor = exchangeValue)}", fontSize = 16.sp, color = Color.Black, modifier = Modifier.align(Alignment.End))
        }
    }
}

private fun formatExchangeValue(valor : Double?) : String{
    if(valor == null) return "0,00"
    return String.format(Locale("PT", "BR"),"%.2f", valor)
}

@Composable
fun AllCurrenciesList(favCurrencies: List<Currency>, exchanges: HashMap<String, Double>) {
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        items(
            count = favCurrencies.size,
            itemContent = { index -> CurrencyListItem(favCurrencies[index], exchanges[favCurrencies[index].code]) }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    val currencyList = listOf(
        Currency(
            code = "BRL",
            info = "Real",
            symbol = "R$",
            flag = ""
        ), Currency(
            code = "USD",
            info = "Dolar americano",
            symbol = "$",
            flag = ""
        )
    )
}