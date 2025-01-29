package com.zamfir.intercambista.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zamfir.intercambista.R
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.ui.theme.IntercambistaTheme
import com.zamfir.intercambista.ui.theme.PurpleBlue
import com.zamfir.intercambista.ui.theme.PurpleBlueDark
import com.zamfir.intercambista.viewmodel.CurrencyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

     private val viewModel : CurrencyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.fetchCountries()

        setContent {
            IntercambistaTheme {
                val loadingState by viewModel.uiLoadingState.collectAsState()

                if(loadingState.progress > 0){
                    LoadingScreen(loadingState.progress)
                }else{
                    viewModel.getCountries()
                    SelectCurrencyScreen(viewModel, onSearch = { query ->
                        viewModel.filterCurrency(query)
                    }, onItemClick = { currency -> })
                }
            }
        }
    }
}


@Composable
fun LoadingScreen(progress : Int){

    var loadingCurrency by remember { mutableStateOf(true) }
    var loadingCountryInfo by remember { mutableStateOf(false) }

    Box(
        Modifier
            .background(PurpleBlue)
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.Center).size(200.dp)){
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

            SpaceBetweenElements(12)

            AnimatedVisibility(visible = loadingCurrency) {
                Text("Carregando moedas...", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White)
            }

            AnimatedVisibility(visible = loadingCountryInfo) {
                Text("Carregando informações dos países...", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White)
            }

            when (progress) {
                1 -> {
                    loadingCurrency = false
                    loadingCountryInfo = true
                }
                2 -> {
                    loadingCurrency = false
                    loadingCountryInfo = true
                }
            }
        }
    }
}

@Composable
private fun SelectCurrencyScreen(viewModel: CurrencyViewModel ,onSearch: (String) -> Unit, onItemClick: (Currency) -> Unit) {
    val currencyUiState by viewModel.uiCurrencyState.collectAsState()

    Column(
        Modifier
            .background(PurpleBlue)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Bem vindo(a), ", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            SpaceBetweenElements(8)

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "para iniciarmos você precisa escolher primeiro uma moeda que servirá de base para cálculo. Fique tranquilo, você poderá muda-la a qualquer momento.", color = Color.White)
            }
        }

        SpaceBetweenElements(0)

        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(18.dp))
                .background(Color.White)
                .fillMaxWidth()
                .fillMaxSize()
        ) {

            Searchfield{ query -> onSearch.invoke(query)}

            CurrencyList(currencies = currencyUiState.result){ onItemClick.invoke(it) }
        }
    }
}

@Composable
private fun Searchfield(onSearch: (String) -> Unit) {
    var text by remember { mutableStateOf("")}
    OutlinedTextField(
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedTextColor = PurpleBlueDark,
            unfocusedBorderColor = PurpleBlueDark,
            unfocusedLabelColor = PurpleBlueDark,
            unfocusedLeadingIconColor = PurpleBlueDark
        ),
        value = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        onValueChange = {
            text = it
            onSearch.invoke(it)
        })
}

@Composable
private fun SpaceBetweenElements(distance : Int) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(distance.dp)
    )
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
fun CurrencyListItem(country : Currency, onItemClick : () -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
        .clip(shape = RoundedCornerShape(12.dp))
        .background(Color.White)
        .padding(8.dp)
        .clickable { onItemClick.invoke() }){
        AsyncImage(
            model = country.flag,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier
            .fillMaxHeight()
            .width(12.dp))

        Text(text = country.info, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 18.sp, color = Color.Black)

        Spacer(modifier = Modifier.weight(1.0f))

        Text(text = country.symbol, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 16.sp, color = Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IntercambistaTheme {
        LoadingScreen(1)
    }
}