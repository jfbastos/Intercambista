package com.zamfir.intercambista.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalSpacerOf(distance : Int) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(distance.dp)
    )
}