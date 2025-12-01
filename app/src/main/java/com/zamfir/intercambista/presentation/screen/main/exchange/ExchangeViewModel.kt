package com.zamfir.intercambista.presentation.screen.main.exchange

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamfir.intercambista.data.repository.CoinsRepository
import com.zamfir.intercambista.presentation.screen.main.MainScreenUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExchangeViewModel @Inject constructor(private val repository : CoinsRepository) : ViewModel() {

    private val _state = MutableStateFlow<ExchangeState>(ExchangeState.Loading)
    val state : StateFlow<ExchangeState> = _state

    private val _dialogState = MutableStateFlow<ExchangeDialogState>(ExchangeDialogState.NoneShown)
    val dialogState : StateFlow<ExchangeDialogState> = _dialogState

    private val _onCoinChange = MutableSharedFlow<Unit>()
    val onCoinChange: SharedFlow<Unit> = _onCoinChange

    init {
        getData()
    }

    fun getData() = viewModelScope.launch {
        val baseCoin = repository.getBaseCurrency()
        val favCoins = repository.getFavoritesCurrenciesRates()
        val lastUpdate = repository.getLastUpdate()

        baseCoin?.let { coin ->
            _state.update {
                ExchangeState.Success(
                    MainScreenUi(
                        baseCoin = coin,
                        favoriteCoins = favCoins,
                        lastUpdate = lastUpdate
                    )
                )
            }
        } ?: run {
            _state.update { ExchangeState.Error("Moeda base null") }
        }
    }

    private fun updateList() = viewModelScope.launch {
        val newFavorites = repository.getFavoritesCurrenciesRates()

        _state.update { currentState ->
            when(currentState){
                is ExchangeState.Success -> {
                    ExchangeState.Success(currentState.mainUi.copy(favoriteCoins = newFavorites))
                }
                else -> currentState
            }
        }
    }

    fun onIntent(intent : ExchangeIntents) = viewModelScope.launch{
        when(intent){
            ExchangeIntents.OnChangeBaseCoin -> { _dialogState.update { ExchangeDialogState.ShowConfirmationCoinChangeDialog }}
            ExchangeIntents.OnChangeCoinConfirmed -> {
                _dialogState.update { ExchangeDialogState.NoneShown }
                _onCoinChange.emit(Unit)
            }
            ExchangeIntents.OnDismissDialog -> { _dialogState.update { ExchangeDialogState.NoneShown }}
            ExchangeIntents.OnRefresh -> updateList()
        }
    }
}