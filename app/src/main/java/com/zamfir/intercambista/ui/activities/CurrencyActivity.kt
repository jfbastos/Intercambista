package com.zamfir.intercambista.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.ui.components.HorizontalSpacerOf
import com.zamfir.intercambista.ui.components.ImageComponent
import com.zamfir.intercambista.ui.theme.Gray80
import com.zamfir.intercambista.ui.theme.IntercambistaTheme
import com.zamfir.intercambista.ui.theme.PurpleBlue
import com.zamfir.intercambista.ui.theme.PurpleBlueDark
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

        viewModel.getBaseCurrencyAsStateFlow()
        viewModel.getFavCurrencies()

        setContent {
            IntercambistaTheme{

                val baseCurrency by viewModel.uiBaseCurrencyState.collectAsStateWithLifecycle()
                val favCurrencies by viewModel.uiFavCurrenciesState.collectAsStateWithLifecycle()

                Scaffold (
                    floatingActionButton = {
                        FloatingActionButton(containerColor = PurpleBlue, contentColor = Color.White ,onClick = {
                            startActivity(Intent(this@CurrencyActivity, CurrencyFavSelectionScreen::class.java))
                            finish()
                        }) {
                            Icon(Icons.Filled.Add, null)
                        }
                    }
                ) { padding ->
                    Body(baseCurrency.baseCurrency,favCurrencies,padding)
                }
            }
        }
    }
}

@Composable
fun Body(baseCurrency: Currency?, favCurrencies: List<Currency>, paddingValues: PaddingValues){
    Column(Modifier.background(color = Gray80).padding(paddingValues)) {
        BaseCurrencyHeader(baseCurrency)
        AllCurrenciesList(favCurrencies)
    }
}

private fun getDataHoraAgoraFormatada() : String{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val agora = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
        val dataFormatada = agora.format(formatter)
        return dataFormatada
    } else {
        return ""
    }
}

@Composable
fun BaseCurrencyHeader(baseCurrency: Currency?) {

    var text by remember { mutableStateOf("1") }

    Column( modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
        .background(color = PurpleBlue)
        .padding(8.dp)
    ){
        Row(modifier = Modifier.fillMaxWidth()){
            Image(imageVector = Icons.Default.DateRange, contentDescription = null, colorFilter = ColorFilter.tint(Color.White))

            Spacer(modifier = Modifier.size(12.dp))

            Text(getDataHoraAgoraFormatada(), color = Color.White, fontSize = 12.sp)
        }

        HorizontalSpacerOf(6)

        Row(modifier = Modifier.fillMaxWidth()){
            Image(imageVector = Icons.Default.Refresh, contentDescription = null, colorFilter = ColorFilter.tint(Color.White))

            Spacer(modifier = Modifier.size(12.dp))

            Text("24/01/2025 às 10:51:12", color = Color.White,  fontSize = 12.sp)
        }

        HorizontalSpacerOf(14)

        Row{
            Row(Modifier.align(alignment = Alignment.Bottom)) {
                ImageComponent(
                    baseCurrency?.flag ?: "",
                    Modifier
                        .size(height = 48.dp, width = 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.size(12.dp))

                val currencyName = baseCurrency?.info ?: "-"
                val code = baseCurrency?.code ?: "-"

                val countryInfo  : String = if(currencyName.length > 15) currencyName.split(" ").first() else currencyName

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
                    modifier = Modifier.align(Alignment.End).width(130.dp),
                    onValueChange = { text = it},
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = PurpleBlueDark,
                        focusedContainerColor = PurpleBlueDark,
                        focusedIndicatorColor =  Color.Transparent,
                        unfocusedIndicatorColor = PurpleBlue
                    ),
                    singleLine = true,
                    keyboardActions = KeyboardActions(onDone = {

                    }),
                )
            }
        }

        HorizontalSpacerOf(16)
    }
}

@Composable
fun CurrencyListItem(currency : Currency){
    Box(Modifier.clip(RoundedCornerShape(12.dp)).size(width = 120.dp, height = 80.dp).background(Color.White).padding(12.dp)){
        ImageComponent(
            currency.flag,
            Modifier
                .size(width = 36.dp, height = 28.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.TopStart)
        )

        Column (modifier = Modifier.align(Alignment.BottomEnd)){
            Text(currency.info.split(" ").first(), modifier = Modifier.align(Alignment.End), fontSize = 18.sp, color = Color.Black)

            Text("R$ 26,33", fontSize = 12.sp, color = Color.Black)
        }
    }
}

@Composable
fun AllCurrenciesList(favCurrencies: List<Currency>) {
    LazyVerticalGrid (
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        items(
            count = favCurrencies.size,
            itemContent = { index -> CurrencyListItem(favCurrencies[index]) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyActivityPreview() {
    IntercambistaTheme {}
}