package com.zamfir.intercambista.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zamfir.intercambista.ui.theme.PurpleBlueDark

@Composable
fun SearchfieldComponent(modifier: Modifier,onSearch: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedTextColor = PurpleBlueDark,
            unfocusedBorderColor = PurpleBlueDark,
            unfocusedLabelColor = PurpleBlueDark,
            unfocusedLeadingIconColor = PurpleBlueDark
        ),
        value = text,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        onValueChange = {
            text = it
            onSearch.invoke(it)
        })
}

