package com.zamfir.intercambista.presentation.screen.main.coins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.data.repository.CoinsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinsViewModel @Inject constructor(private val repository : CoinsRepository) : ViewModel() {

    private val _state : MutableStateFlow<CoinsState> = MutableStateFlow(CoinsState.OnLoading)
    val state : StateFlow<CoinsState> = _state

    init {
        getData()
    }

    private fun getData() = viewModelScope.launch {
        val coins = repository.getCurrencies() ?: listOf()

        _state.update { CoinsState.OnSuccess(coins) }
    }

    private fun toggleFav(coin : Currency) = viewModelScope.launch {
        val editedCoin = repository.toggleFavorite(coin)
        _state.update { currentState ->
            when(val state = currentState){
                is CoinsState.OnSuccess -> {
                    val updatedList = state.listOfCoins.map { c ->
                        if (c.code == coin.code) editedCoin else c
                    }

                    CoinsState.OnSuccess(updatedList)
                }
                else -> state
            }
        }
    }

    fun onIntent(intents: CoinsIntents){
        when(intents){
            is CoinsIntents.OnCoinClicked -> {toggleFav(intents.coin)}
            is CoinsIntents.OnFilter -> {}
            is CoinsIntents.OnSearch -> {}
        }
    }
}