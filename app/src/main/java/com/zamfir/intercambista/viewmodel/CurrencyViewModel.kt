package com.zamfir.intercambista.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamfir.intercambista.data.repository.CountryRepository
import com.zamfir.intercambista.data.repository.CurrencyRespository
import com.zamfir.intercambista.viewmodel.state.CurrencyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoadingState(val progress : Int = -1)


@HiltViewModel
class CurrencyViewModel @Inject constructor(private val repository : CurrencyRespository, private val countriesRepository: CountryRepository) : ViewModel() {

    private val _uiCurrencyState = MutableStateFlow(CurrencyState())
    val uiCurrencyState : StateFlow<CurrencyState> = _uiCurrencyState.asStateFlow()

    private val _uiLoadingState = MutableStateFlow(LoadingState())
    val uiLoadingState : StateFlow<LoadingState> = _uiLoadingState.asStateFlow()

    fun fetchCountries() = viewModelScope.launch{
        _uiLoadingState.value = LoadingState(1)

        repository.fetchAvaliableCurrencies()

        _uiLoadingState.value = LoadingState(2)

        countriesRepository.fetchCountryByCurrency()

        _uiLoadingState.value = LoadingState(-1)
    }

    fun getCountries() = viewModelScope.launch {
        _uiCurrencyState.value = CurrencyState(result = repository.getCurrencies() ?: listOf())
    }

    fun filterCurrency(query : String) = viewModelScope.launch{
        val itens =  repository.getCurrencies() ?: listOf()
        if(query.isBlank()) _uiCurrencyState.value = CurrencyState(result = itens)
        else _uiCurrencyState.value = CurrencyState(result = itens.filter { it.info.contains(query, ignoreCase = true) })
    }
}