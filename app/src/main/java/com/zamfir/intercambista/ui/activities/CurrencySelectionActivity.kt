package com.zamfir.intercambista.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.ui.components.SearchfieldComponent
import com.zamfir.intercambista.ui.theme.IntercambistaTheme

class CurrencySelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IntercambistaTheme {
                Column(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .fillMaxWidth()
                        .fillMaxSize()
                ) {

                    SearchfieldComponent(
                        Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                    ) { query ->  }

                    //CurrencyList(currencies = currencies) { onItemClick.invoke(it) }
                }
            }
        }
    }


}

@Composable
fun CurrencyItem(currency : Currency, onItemClick : () -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
        .clip(shape = RoundedCornerShape(12.dp))
        .background(Color.White)
        .padding(8.dp)
        .clickable { onItemClick.invoke() }){
        AsyncImage(
            model = currency.flag,
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

        Text(text = currency.info, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 18.sp, color = Color.Black)

        Spacer(modifier = Modifier.weight(1.0f))

        Text(text = currency.symbol, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 16.sp, color = Color.Black)
    }
}


@Preview(showBackground = true)
@Composable
fun CurrencySelectionActivityPreview() {
    IntercambistaTheme {

    }
}