package com.zamfir.intercambista.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.zamfir.intercambista.R

@Composable
fun CountryFlagImageComponent(url : String, modifier: Modifier){
    if(url.isBlank()){
        Image(
            painter = if(isSystemInDarkTheme()) painterResource(R.drawable.baseline_no_photography_24_white) else painterResource(R.drawable.baseline_no_photography_24),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }else{
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}