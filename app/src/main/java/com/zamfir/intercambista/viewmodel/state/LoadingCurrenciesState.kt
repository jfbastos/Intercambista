package com.zamfir.intercambista.viewmodel.state

import com.zamfir.intercambista.data.enums.FetchCurrencyInfoStages

data class LoadingCurrenciesState(
    val progress : FetchCurrencyInfoStages? = null
)