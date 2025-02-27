package com.zamfir.intercambista.viewmodel.state

data class FetchCountriesState(
    val loadingStage : Int = -1,
    val exception : Exception? = null
)