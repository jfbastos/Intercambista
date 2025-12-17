package com.zamfir.intercambista.presentation.screen.main.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamfir.intercambista.data.enums.SortOption
import com.zamfir.intercambista.data.repository.CoinsRepository
import com.zamfir.intercambista.presentation.screen.main.MainScreenUi
import com.zamfir.intercambista.presentation.screen.main.exchange.ExchangeDialogState.NoneShown
import com.zamfir.intercambista.presentation.screen.main.exchange.ExchangeDialogState.ShowConfirmationCoinChangeDialog
import com.zamfir.intercambista.presentation.screen.main.exchange.ExchangeDialogState.ShowErrorDialog
import com.zamfir.intercambista.presentation.screen.main.exchange.ExchangeDialogState.ShowLoadingDialog
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

    private val _dialogState = MutableStateFlow<ExchangeDialogState>(NoneShown)
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
        val sort = repository.getListSortPreference()

        baseCoin?.let { coin ->
            _state.update {
                ExchangeState.Success(
                    MainScreenUi(
                        baseCoin = coin,
                        favoriteCoins = when(sort){
                            SortOption.ASCENDING -> favCoins.sortedBy { it.rate }
                            SortOption.DESCENDING -> favCoins.sortedByDescending { it.rate }
                        },
                        lastUpdate = lastUpdate
                    ),
                    sort
                )
            }
        } ?: run {
            _state.update { ExchangeState.Error("Moeda base null") }
        }
    }

    private fun updateList() = viewModelScope.launch {
        repository.checkFavCoinsWithoutRates(onFinish = { updatedList ->
            _state.update { currentState ->
                when(currentState){
                    is ExchangeState.Success -> {
                        ExchangeState.Success(currentState.mainUi.copy(favoriteCoins = when(currentState.sortType){
                            SortOption.ASCENDING -> updatedList.sortedBy { it.rate }
                            SortOption.DESCENDING -> updatedList.sortedByDescending { it.rate }
                        }), sortType = currentState.sortType)
                    }
                    else -> currentState
                }
            }
        }, onFailure = {
            ExchangeState.Error("Falha ao atualizar moedas.", Exception(it))
        })
    }

    private fun toggleSort() = viewModelScope.launch {
        val newSort = repository.toggleListSortPreference()

        _state.update { currentState ->
            when(currentState){
                is ExchangeState.Success -> {

                    val sortedList = when(newSort){
                        SortOption.ASCENDING -> currentState.mainUi.favoriteCoins.sortedBy { it.rate }
                        SortOption.DESCENDING -> currentState.mainUi.favoriteCoins.sortedByDescending { it.rate }
                    }

                    ExchangeState.Success(currentState.mainUi.copy(favoriteCoins = sortedList), newSort)
                }
                else -> currentState
            }
        }
    }

    fun onIntent(intent : ExchangeIntents) = viewModelScope.launch{
        when(intent){
            ExchangeIntents.OnChangeBaseCoin -> { _dialogState.update { ShowConfirmationCoinChangeDialog }}
            ExchangeIntents.OnChangeCoinConfirmed -> {
                _dialogState.update { NoneShown }
                _onCoinChange.emit(Unit)
            }
            ExchangeIntents.OnDismissDialog -> { _dialogState.update { NoneShown }}
            ExchangeIntents.OnRefresh -> updateList()
            ExchangeIntents.OnLoadingDialog -> { _dialogState.update { ShowLoadingDialog }}
            is ExchangeIntents.OnFailureDialog -> { _dialogState.update { ShowErrorDialog(intent.msg, intent.exception) }}
            ExchangeIntents.OnFilter -> { toggleSort() }
        }
    }
}