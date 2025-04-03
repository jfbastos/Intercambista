package com.zamfir.intercambista.viewmodel.state

import com.zamfir.intercambista.data.database.entity.Currency

data class CurrencyState(
    val currencies : List<Currency> = listOf(),
    val baseCurrency : Currency? = null,
    val exception : Exception? = null
)