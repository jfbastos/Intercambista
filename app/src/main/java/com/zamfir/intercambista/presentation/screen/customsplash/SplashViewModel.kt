package com.zamfir.intercambista.presentation.screen.customsplash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamfir.intercambista.core.Either
import com.zamfir.intercambista.data.repository.CoinsRepository
import com.zamfir.intercambista.presentation.screen.commom.NavigationEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val repository: CoinsRepository) : ViewModel() {

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading("Verificando moedas"))
    val splashState: StateFlow<SplashState> = _splashState

    private val _navigationEvents = MutableSharedFlow<NavigationEvents>()
    val navigationEvents : SharedFlow<NavigationEvents> = _navigationEvents

    private val _dialogState = MutableStateFlow<SplashDialogsState>(SplashDialogsState.OnNoneShown)
    val dialogState : StateFlow<SplashDialogsState> = _dialogState

    init {
        getData()
    }

    fun getData() = viewModelScope.launch {
        if (repository.isCurrencyAlreadySelected()) {

            _splashState.update { SplashState.Loading("Atualizando moedas...") }

            repository.fetchCurrencyExchange().onSuccess {
                _splashState.update { SplashState.OnCurrencyAlreadySelected }
            }.onFailure { exception ->
                _splashState.update {
                    SplashState.OnFailure("Falha ao atualizar moedas. Detalhes : ${exception.stackTraceToString()}" )
                }
            }

            return@launch
        }

        val currencies = repository.getCurrencies()

        if (!currencies.isNullOrEmpty()) {
            delay(1000)
            _splashState.update { SplashState.OnSuccess }
        }else{
            _splashState.update { SplashState.Loading("Realizando o download das moedas...") }

            repository.fetchCurrencies().collect { fetchingStatus ->
                when (fetchingStatus) {
                    is Either.Left -> {
                        _splashState.update { SplashState.OnFailure(fetchingStatus.value) }
                    }

                    is Either.Right -> {
                        _splashState.update { SplashState.Loading(fetchingStatus.value) }
                    }
                }
            }

            val fetchedCurrencies = repository.getCurrencies()

            if(!fetchedCurrencies.isNullOrEmpty()) {
                _splashState.update { SplashState.OnSuccess }
            }
        }
    }

    private fun showErrorDialog(msg : String) = viewModelScope.launch {
        _dialogState.update { SplashDialogsState.OnShowErrorDialog (msg)}
    }

    private fun dismissDialogs() = viewModelScope.launch {
        _dialogState.update { SplashDialogsState.OnNoneShown }
    }

    fun onIntent(intent : SplashIntents) = viewModelScope.launch{
        when(intent){
            SplashIntents.OnNavigateCoinSelection -> _navigationEvents.emit(NavigationEvents.GoToCoinSelectionScreen)
            SplashIntents.OnNavigateMainScreen -> _navigationEvents.emit(NavigationEvents.GoToMainScreen)
            is SplashIntents.OnFailure ->  showErrorDialog(intent.msg)
            SplashIntents.OnDismissDialog -> dismissDialogs()
        }
    }
}