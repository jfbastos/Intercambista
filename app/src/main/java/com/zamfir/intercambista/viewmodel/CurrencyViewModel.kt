package com.zamfir.intercambista.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.data.enums.FetchCurrencyInfoStages
import com.zamfir.intercambista.data.repository.CurrencyRepository
import com.zamfir.intercambista.data.repository.FetchCurrenciesInfoCallback
import com.zamfir.intercambista.viewmodel.state.BaseCurrencyState
import com.zamfir.intercambista.viewmodel.state.CurrencyState
import com.zamfir.intercambista.viewmodel.state.ExchangeState
import com.zamfir.intercambista.viewmodel.state.LoadingCurrenciesState
import com.zamfir.intercambista.viewmodel.state.SaveBaseCurrencyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(private val repository : CurrencyRepository) : ViewModel() {

    private val _uiCurrenciesListState = MutableStateFlow(CurrencyState())
    val uiCurrenciesListState : StateFlow<CurrencyState> = _uiCurrenciesListState.asStateFlow()

    private val _uiFirstLoadingCurrenciesState = MutableStateFlow(LoadingCurrenciesState())
    val uiFirstLoadingCurrenciesState : StateFlow<LoadingCurrenciesState> = _uiFirstLoadingCurrenciesState

    private val _uiBaseCurrencyLiveData = MutableLiveData<BaseCurrencyState>()
    val uiBaseCurrencyLiveData : LiveData<BaseCurrencyState> = _uiBaseCurrencyLiveData

    private val _uiSaveCurrencyState = MutableStateFlow(SaveBaseCurrencyState())
    val uiSaveCurrencyState : StateFlow<SaveBaseCurrencyState> = _uiSaveCurrencyState

    private val _uiSavingFavCurrenciesState = MutableStateFlow(false)
    val uiSavingFavCurrenciesState : StateFlow<Boolean> = _uiSavingFavCurrenciesState

    private val _uiMainScreenState = MutableStateFlow(ExchangeState())
    val uiMainScreenState : StateFlow<ExchangeState> = _uiMainScreenState

    fun getCountries() = viewModelScope.launch {
        _uiFirstLoadingCurrenciesState.value = LoadingCurrenciesState(progress = FetchCurrencyInfoStages.LOADING_DB_CURRENCIES)
        repository.prepareDatabaseInfo(object : FetchCurrenciesInfoCallback {
            override fun onProgress(stage: FetchCurrencyInfoStages) {
                viewModelScope.launch { _uiFirstLoadingCurrenciesState.value = LoadingCurrenciesState(progress = stage) }
            }

            override fun onFinish(currencies: List<Currency>, baseCurrency: Currency?) {
                viewModelScope.launch {
                    _uiFirstLoadingCurrenciesState.value = LoadingCurrenciesState(progress = null)
                    _uiCurrenciesListState.value = CurrencyState(currencies = currencies, baseCurrency =  baseCurrency)
                }
            }

            override fun onError(ex: Exception) {
                viewModelScope.launch {
                    _uiFirstLoadingCurrenciesState.value = LoadingCurrenciesState(progress = null)
                    _uiCurrenciesListState.value = CurrencyState(exception = ex)
                }
            }
        })
    }

    fun filterCurrency(query : String) = viewModelScope.launch{
        val itens =  repository.getCurrencies() ?: listOf()
        if(query.isBlank()) _uiCurrenciesListState.value = CurrencyState(currencies = itens)
        else _uiCurrenciesListState.value = CurrencyState(currencies = itens.filter { it.info.contains(query, ignoreCase = true) })
    }

    fun setBaseCurrency(currency : Currency) = viewModelScope.launch {
        try{
            repository.saveBaseCurrency(currency.code)
            _uiSaveCurrencyState.value = SaveBaseCurrencyState(isSuccess = true)
        }catch (e : Exception){
            _uiSaveCurrencyState.value = SaveBaseCurrencyState(exception = e)
        }
    }

    fun setNewBaseCurrency(currency : Currency) = viewModelScope.launch {
        try{
            repository.saveBaseCurrency(currency.code)
            repository.changeBaseCurrency {
                viewModelScope.launch { _uiSaveCurrencyState.value = SaveBaseCurrencyState(isSuccess = true) }
            }
        }catch (e : Exception){
            _uiSaveCurrencyState.value = SaveBaseCurrencyState(exception = e)
        }
    }

    fun getBaseCurrencyAsLiveData(hasConnection: Boolean) = viewModelScope.launch {
        try{
            val baseCurrencyCode = repository.getBaseCurrencyPreference()

            if(baseCurrencyCode.isBlank()) {
                _uiBaseCurrencyLiveData.value = BaseCurrencyState(baseCurrency = null)
            }else{
                val moeda = repository.getCurrencyByCode(baseCurrencyCode)

                if(hasConnection){
                    repository.fetchCurrencyExchange {
                        viewModelScope.launch { _uiBaseCurrencyLiveData.value = BaseCurrencyState(baseCurrency = moeda) }
                    }
                }else{
                    _uiBaseCurrencyLiveData.value = BaseCurrencyState(baseCurrency = moeda)
                }
            }
        }catch (e : Exception){
            Log.e("DEBUG", "Failed to ge info of first screen. ${e.stackTraceToString()}")
        }
    }

    fun saveFavCurrencies(selectedCurrencies : List<String>) = viewModelScope.launch {
        repository.saveFavoritesCurrencies(selectedCurrencies).onSuccess {
            repository.fetchCurrencyExchangeOnFavorites(selectedCurrencies) {
                viewModelScope.launch { _uiSavingFavCurrenciesState.value = true }
            }
        }.onFailure {
            Log.e("DEBUG", "Failed to save currency : ${it.stackTraceToString()}")
        }
    }

    fun getMainScreenInfo(baseCalc : Double) = viewModelScope.launch {
        try{
            val baseCurrency = repository.getCurrencyByCode(repository.getBaseCurrencyPreference())
            val favoritesCurrencies = repository.getFavCurrencies() ?: listOf()
            val valuesByCurrencies = hashMapOf<String, Double>()
            val lastUpdate = repository.getLastUpdate()

            favoritesCurrencies.map { it.code }.forEach { code ->
                valuesByCurrencies[code] = (repository.getValueByCurrency(code)?.value?.toDouble() ?: 0.0).toDouble() * baseCalc
            }

            _uiMainScreenState.value = ExchangeState(baseCurrency = baseCurrency, favoritesCurrencies = favoritesCurrencies, exchanges = valuesByCurrencies, lastUpdateDatetime = lastUpdate)

        }catch (e : Exception){
            Log.e("DEBUG", "Falha ao obter valores para tela : ${e.stackTraceToString()}")
            _uiMainScreenState.value = ExchangeState()
        }
    }
}