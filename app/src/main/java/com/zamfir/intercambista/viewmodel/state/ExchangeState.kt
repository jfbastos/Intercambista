package com.zamfir.intercambista.viewmodel.state

import com.zamfir.intercambista.data.database.entity.Currency

data class ExchangeState(
    val baseCurrency : Currency? = null,
    val favoritesCurrencies : List<Currency> = listOf(),
    val exchanges : HashMap<String, Double> = hashMapOf(),
    val lastUpdateDatetime : String = ""
)