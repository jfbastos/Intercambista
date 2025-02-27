package com.zamfir.intercambista.viewmodel.state

import com.zamfir.intercambista.data.database.entity.Currency
import java.lang.Exception

data class BaseCurrencyState (
    val baseCurrency : Currency? = null,
    val isLoading : Boolean = false,
    val exception: Exception? = null
)