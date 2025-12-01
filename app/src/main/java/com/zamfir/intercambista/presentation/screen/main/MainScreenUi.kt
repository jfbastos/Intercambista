package com.zamfir.intercambista.presentation.screen.main

import com.zamfir.intercambista.data.database.dto.ExchangeDto
import com.zamfir.intercambista.data.database.entity.Currency

data class MainScreenUi (
    val baseCoin : Currency,
    val favoriteCoins : List<ExchangeDto>,
    val lastUpdate : String,
)