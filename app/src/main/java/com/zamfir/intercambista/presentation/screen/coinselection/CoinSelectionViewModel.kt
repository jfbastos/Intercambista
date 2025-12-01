package com.zamfir.intercambista.presentation.screen.coinselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.data.repository.CoinsRepository
import com.zamfir.intercambista.presentation.screen.commom.NavigationEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinSelectionViewModel @Inject constructor(private val repository: CoinsRepository) : ViewModel(){

    private val _currenciesState = MutableStateFlow<CoinSelectionState>(CoinSelectionState.Loading)

    private val _navigationEvents = MutableSharedFlow<NavigationEvents>()
    val navigationEvents : SharedFlow<NavigationEvents> = _navigationEvents

    private val _searchQuery = MutableStateFlow("")

    private val _dialogState = MutableStateFlow<CoinSelectionDialogsState>(CoinSelectionDialogsState.OnNoneShown)
    val dialogState : StateFlow<CoinSelectionDialogsState> =_dialogState

    val currenciesState : StateFlow<CoinSelectionState> =
        combine(_searchQuery, _currenciesState) { query, state ->
            when(state){
                is CoinSelectionState.OnSuccess -> {
                    val filtered = if (query.isBlank()) {
                        state.currencies
                    } else {
                        state.currencies.filter { it.info.contains(query, ignoreCase = true) }
                    }

                    CoinSelectionState.OnSuccess(filtered)
                }
                else -> {
                    state
                }
            }
        }
        .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CoinSelectionState.Loading
    )

    init { loadData() }

    fun loadData() = viewModelScope.launch {
        try {
            val currencies = repository.getCurrencies()

            delay(1000)

            if(!currencies.isNullOrEmpty()){
                _currenciesState.update { CoinSelectionState.OnSuccess(currencies) }
            }else{
                _currenciesState.update { CoinSelectionState.OnFailure("Nenhuma moeda encontrada no banco.") }
            }
        }catch (e : Exception){
            _currenciesState.update { CoinSelectionState.OnFailure("Falha ao obter moedas!", e) }
        }
    }

    private fun showDialogCoinSelection(currency : Currency) = viewModelScope.launch {
        _dialogState.update { CoinSelectionDialogsState.OnShowSelectionConfirmation(currency) }
    }

    private fun showFailureDialog(msg: String, exception: Exception?) = viewModelScope.launch {
        _dialogState.update { CoinSelectionDialogsState.OnShowErrorDialog(msg, exception) }
    }

    private fun dismissDialog() = viewModelScope.launch {
        _dialogState.update { CoinSelectionDialogsState.OnNoneShown }
    }

    private fun saveCoinAndNavigate(baseCoinCode : String) = viewModelScope.launch{
        _dialogState.update { CoinSelectionDialogsState.OnNoneShown }
        repository.saveBaseCurrency(baseCoinCode)

        if(repository.hasFavorites()){
            _dialogState.update { CoinSelectionDialogsState.OnShowLoadingFavoritesInfo }
            repository.fetchCurrencyExchange().onSuccess {
                _dialogState.update { CoinSelectionDialogsState.OnNoneShown }
                _navigationEvents.emit(NavigationEvents.GoToMainScreen)
            }.onFailure { throwable ->
                _dialogState.update { CoinSelectionDialogsState.OnShowErrorDialog("Falha ao cotas das moedas favoritadas. \n\nDetalhes: ${throwable.message}.") }
            }
        }else{
            _navigationEvents.emit(NavigationEvents.GoToMainScreen)
        }
    }

    fun onIntent(intents : CoinSelectionIntents) = viewModelScope.launch{
        when(intents){
            is CoinSelectionIntents.OnSearchCoin -> { _searchQuery.value = intents.query }
            is CoinSelectionIntents.OnCoinSelectionConfirmed -> { saveCoinAndNavigate(intents.code) }
            is CoinSelectionIntents.OnShowConfirmationDialog -> { showDialogCoinSelection(intents.currency) }
            CoinSelectionIntents.OnDismissDialog -> { dismissDialog() }
            is CoinSelectionIntents.OnShowErrorDialog -> showFailureDialog(intents.msg, intents.exception)
        }
    }
}