package com.zamfir.intercambista.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.data.repository.CountryRepository
import com.zamfir.intercambista.data.repository.CurrencyRespository
import com.zamfir.intercambista.viewmodel.state.BaseCurrencyState
import com.zamfir.intercambista.viewmodel.state.CurrencyState
import com.zamfir.intercambista.viewmodel.state.SaveBaseCurrencyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(private val repository : CurrencyRespository, private val countriesRepository: CountryRepository) : ViewModel() {

    private val _uiCurrenciesState = MutableStateFlow(CurrencyState())
    val uiCurrenciesState : StateFlow<CurrencyState> = _uiCurrenciesState.asStateFlow()

    private val _uiBaseCurrencyLiveData = MutableLiveData<Event<BaseCurrencyState>>()
    val uiBaseCurrencyLiveData : LiveData<Event<BaseCurrencyState>> = _uiBaseCurrencyLiveData

    private val _uiSaveCurrencyState = MutableStateFlow(SaveBaseCurrencyState())
    val uiSaveCurrencyState : StateFlow<SaveBaseCurrencyState> = _uiSaveCurrencyState

    private val _uiBaseCurrencyState = MutableStateFlow(BaseCurrencyState())
    val uiBaseCurrencyState : StateFlow<BaseCurrencyState> = _uiBaseCurrencyState

    private val _uiFavCurrenciesState = MutableStateFlow(listOf<Currency>())
    val uiFavCurrenciesState : StateFlow<List<Currency>> = _uiFavCurrenciesState

    private val _uiSavingFavCurrenciesState = MutableStateFlow(false)
    val uiSavingFavCurrenciesState : StateFlow<Boolean> = _uiSavingFavCurrenciesState

    fun getCountries() = viewModelScope.launch {
        _uiCurrenciesState.value = CurrencyState(loadingStage = 1)
        val dbCurrencies = repository.getCurrencies()

        if(dbCurrencies.isNullOrEmpty()){
            repository.fetchAvailableCurrencies {
                viewModelScope.launch {
                    _uiCurrenciesState.value = CurrencyState(loadingStage = 2)
                    countriesRepository.fetchCountryByCurrency {
                        _uiCurrenciesState.value = CurrencyState(loadingStage = -1)
                    }
                }
            }
        }else{
            _uiCurrenciesState.value = CurrencyState(result = dbCurrencies)
        }
    }

    fun filterCurrency(query : String) = viewModelScope.launch{
        val itens =  repository.getCurrencies() ?: listOf()
        if(query.isBlank()) _uiCurrenciesState.value = CurrencyState(result = itens)
        else _uiCurrenciesState.value = CurrencyState(result = itens.filter { it.info.contains(query, ignoreCase = true) })
    }

    fun setBaseCurrency(currency : Currency) = viewModelScope.launch {
        try{
            repository.saveBaseCurrency(currency.code)
            _uiSaveCurrencyState.value = SaveBaseCurrencyState(isSuccess = true)
        }catch (e : Exception){
            _uiSaveCurrencyState.value = SaveBaseCurrencyState(exception = e)
        }
    }

    fun getBaseCurrencyAsLiveData() = viewModelScope.launch {
        val baseCurrencyPreference = repository.getBaseCurrencyPreference()

        baseCurrencyPreference.collectLatest { baseCurrencyCode ->
            if(baseCurrencyCode.isBlank()) {
                _uiBaseCurrencyLiveData.value = Event(BaseCurrencyState(baseCurrency = null))
            }else{
                val moeda = repository.getCurrencyByCode(baseCurrencyCode)
                _uiBaseCurrencyLiveData.value = Event(BaseCurrencyState(baseCurrency = moeda))
            }
        }
    }

    fun saveFavCurrencies(selectedCurrencies : List<String>) = viewModelScope.launch {
        repository.saveFavoritedCurrencies(selectedCurrencies).onSuccess {
            _uiSavingFavCurrenciesState.value = true
        }
    }

    fun requestUpdateCurrencies() = viewModelScope.launch {
        repository.fetchCurrencyExchange()
    }

    fun getBaseCurrencyAsStateFlow() = viewModelScope.launch {
        val baseCurrencyPreference = repository.getBaseCurrencyPreference()

        baseCurrencyPreference.collectLatest { baseCurrencyCode ->
            if(baseCurrencyCode.isBlank()) {
                _uiBaseCurrencyState.value = BaseCurrencyState(baseCurrency = null)
            }else{
                val moeda = repository.getCurrencyByCode(baseCurrencyCode)
                _uiBaseCurrencyState.value = BaseCurrencyState(baseCurrency = moeda)
            }
        }
    }

    fun getFavCurrencies() = viewModelScope.launch {
        _uiFavCurrenciesState.value = repository.getFavCurrencies() ?: listOf()
    }
}