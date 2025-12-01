package com.zamfir.intercambista.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamfir.intercambista.data.repository.CoinsRepository
import com.zamfir.intercambista.presentation.screen.commom.NavigationEvents
import com.zamfir.intercambista.presentation.screen.main.navigation.MainScreenNavState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository : CoinsRepository) : ViewModel() {

    private val _navState = MutableStateFlow<MainScreenNavState>(MainScreenNavState())
    val navState : StateFlow<MainScreenNavState> = _navState

    private val _navigationEvents = MutableSharedFlow<NavigationEvents>()
    val navigationEvents : SharedFlow<NavigationEvents> = _navigationEvents

    private fun clearCurrentBaseCoin() = viewModelScope.launch {
        repository.clearCurrentBaseCoinData()
        _navigationEvents.emit(NavigationEvents.GoToCoinSelectionScreen)
    }

    fun onIntent(intent : MainScreenIntent) = viewModelScope.launch{
        when(intent){
            is MainScreenIntent.NavigateTo -> { _navState.update { it.copy(current = intent.destination) }}
            MainScreenIntent.OnReturnToCoinSelection -> { clearCurrentBaseCoin() }
        }
    }
}