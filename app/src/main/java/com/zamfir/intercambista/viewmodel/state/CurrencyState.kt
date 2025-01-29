package com.zamfir.intercambista.viewmodel.state

import com.zamfir.intercambista.data.database.entity.Currency

data class CurrencyState(
    val loadingState : Int = -1,
    val result : List<Currency> = listOf(),
    val exception : Exception? = null
)