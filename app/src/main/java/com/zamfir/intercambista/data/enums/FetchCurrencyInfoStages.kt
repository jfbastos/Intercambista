package com.zamfir.intercambista.data.enums

enum class FetchCurrencyInfoStages(val stage : Int, val info : String) {

    LOADING_DB_CURRENCIES(0, "Obtendo moedas salvas..."),
    LOADING_AVAILABLE_CURRENCIES(1, "Baixando moedas disponíveis..."),
    LOADING_INFO_CURRENCIES(2, "Obtendo informações das moedas...")

}