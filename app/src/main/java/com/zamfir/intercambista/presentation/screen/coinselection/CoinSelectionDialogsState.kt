package com.zamfir.intercambista.presentation.screen.coinselection

import com.zamfir.intercambista.data.database.entity.Currency

sealed class CoinSelectionDialogsState {

    data object OnNoneShown : CoinSelectionDialogsState()
    data class  OnShowSelectionConfirmation(val currency : Currency) : CoinSelectionDialogsState()
    data object OnShowLoadingFavoritesInfo : CoinSelectionDialogsState()
    data class  OnShowErrorDialog(val msg : String, val exception: Exception? = null) : CoinSelectionDialogsState()

}