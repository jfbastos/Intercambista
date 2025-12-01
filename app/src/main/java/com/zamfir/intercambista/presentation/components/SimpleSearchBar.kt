package com.zamfir.intercambista.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSearchBar(
    modifier: Modifier = Modifier,
    placeholder: String = "Pesquisar",
    onSearchChange: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    SearchBar(
        query = text,
        onQueryChange = { newText ->
            text = newText
            onSearchChange(newText)
        },
        onSearch = { query ->
            onSearchChange(query)
        },
        active = false,
        onActiveChange = {},
        modifier = modifier,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(
                    onClick = {
                        text = ""
                        onSearchChange("")
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Limpar")
                }
            }
        }
    ) { /* vazio: sem sugestões */ }
}

@Preview
@Composable
private fun Preview() {
    SimpleSearchBar(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) { }
}
