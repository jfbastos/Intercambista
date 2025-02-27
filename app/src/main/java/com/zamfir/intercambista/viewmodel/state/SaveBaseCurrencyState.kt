package com.zamfir.intercambista.viewmodel.state

import java.lang.Exception

data class SaveBaseCurrencyState(
    val isSuccess : Boolean = false,
    val exception: Exception? = null
)