package com.zamfir.intercambista.data.repository

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zamfir.intercambista.data.database.AppDatabase
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.data.database.entity.CurrencyHistory
import com.zamfir.intercambista.data.enums.FetchCurrencyInfoStages
import com.zamfir.intercambista.data.rest.dto.restcountries.CountriesInfo
import com.zamfir.intercambista.data.rest.service.ExchageRatesService
import com.zamfir.intercambista.data.rest.service.RestCountriesService
import com.zamfir.intercambista.util.Constants.EMPTY_STRING
import com.zamfir.intercambista.util.Constants.JSON_CODE_KEY
import com.zamfir.intercambista.util.Constants.JSON_HIGH_KEY
import com.zamfir.intercambista.util.Constants.SHARED_KEY_BASE_CURRENCY
import com.zamfir.intercambista.util.getDateNowFormatted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

interface FetchCurrenciesInfoCallback{
    fun onProgress(stage : FetchCurrencyInfoStages)
    fun onFinish(currencies : List<Currency>, baseCurrency: Currency? = null)
    fun onError(ex : Exception)
}

@Singleton
class ExchangeRepository @Inject constructor(
    private val context : Context,
    private val exchangeRatesService: ExchageRatesService,
    private val restCountryService: RestCountriesService,
    private val appDatabase: AppDatabase
){

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    suspend fun prepareDatabaseInfo(callback: FetchCurrenciesInfoCallback) = withContext(Dispatchers.IO){
        try{
            callback.onProgress(FetchCurrencyInfoStages.LOADING_DB_CURRENCIES)
            val currentCurrencies = appDatabase.currencyDao().getCurrencies()

            if(!currentCurrencies.isNullOrEmpty()){
                callback.onFinish(currentCurrencies)
                return@withContext
            }

            callback.onProgress(FetchCurrencyInfoStages.LOADING_AVAILABLE_CURRENCIES)
            fetchCurrencies()

            callback.onProgress(FetchCurrencyInfoStages.LOADING_INFO_CURRENCIES)
            fetchCurrenciesInfos()

            callback.onProgress(FetchCurrencyInfoStages.LOADING_DB_CURRENCIES)

            val baseCurrency = getCurrencyByCode(getBaseCurrencyPreference())

            callback.onFinish(appDatabase.currencyDao().getCurrencies() ?: listOf(), baseCurrency)
        }catch (e : Exception){
            callback.onError(e)
        }

    }

    private fun fetchCurrencies(){
        val coinsWithExchange = mutableListOf<Currency>()
        val request = exchangeRatesService.fetchAvaliableCurrencies()?.execute() ?: throw RuntimeException("Impossible to fetch currencies. Detail: Request is null")

        if(request.isSuccessful){
            val availableCountriesInApi = Gson().fromJson(request.body(), hashMapOf<String, String>()::class.java)

            availableCountriesInApi.forEach{ (code, info) ->
                if(checkIfCoinExists(code)){
                    coinsWithExchange.add(Currency(code = code, info = info))
                }
            }

            appDatabase.currencyDao().addCurrencies(coinsWithExchange)
        }
    }

    private fun fetchCurrenciesInfos(){

        val request = restCountryService.fetchDataByCountryCurrency()?.execute() ?: throw RuntimeException("Impossible to fetch currencies infos. Detail: Request is null")

        if(request.isSuccessful){
            val type = object : TypeToken<CountriesInfo>() {}.type
            val countriesInfo = Gson().fromJson<CountriesInfo>(request.body().toString(), type)

            countriesInfo.forEach { countryInfo ->
                val countryCurrencyCode = countryInfo.currencyInfo.keys.firstOrNull() ?: ""
                appDatabase.currencyDao().updateCurrencyInfo(
                    flagUrl = countryInfo.flags.png,
                    symbol = countryInfo.currencyInfo.values.map { it.symbol }.firstOrNull() ?: "",
                    code = countryCurrencyCode
                )
            }
        }
    }

    private fun checkIfCoinExists(coinCode : String) : Boolean{
        if(coinCode == "BRL") return true
        return exchangeRatesService.fetchDataByCurrencyComparation(coinCode)?.execute()?.isSuccessful ?: false
    }

    suspend fun fetchCurrencyExchangeOnFavorites(currenciesToUpdate: List<String>, onFinish: (() -> Unit)?) = withContext(Dispatchers.IO){
        try{
            val baseCode = prefs.getString(SHARED_KEY_BASE_CURRENCY, EMPTY_STRING) ?: EMPTY_STRING
            val requestBuild = currenciesToUpdate.map { "$it-$baseCode" }

            requestBuild.forEach { codeCombination ->
                val response = exchangeRatesService.fetchDataByCurrencyComparation(codeCombination)?.execute()

                val bodyOfCombination = response?.body()?.get(codeCombination.replace("-",EMPTY_STRING))?.toString() ?: EMPTY_STRING

                if(bodyOfCombination.isNotBlank()){
                    val exchangeObject  = JSONObject(bodyOfCombination)

                    val codeIn = exchangeObject.get(JSON_CODE_KEY)
                    val value = exchangeObject.get(JSON_HIGH_KEY)

                    val history = CurrencyHistory(
                        baseCode = baseCode,
                        inCode = codeIn as String,
                        value = value as String,
                        createAt = LocalDateTime.now().getDateNowFormatted()
                    )

                    saveExchangeHistory(history)
                }
            }
        }catch (e : Exception){
            Log.e("DEBUG", "Failed to update $currenciesToUpdate. Details : ${e.stackTraceToString()}")
        }finally {
            onFinish?.invoke()
        }
    }


    suspend fun changeBaseCurrency(onFinish: (() -> Unit)) = withContext(Dispatchers.IO){
        appDatabase.historyDao().clear()

        fetchCurrencyExchange{
            onFinish.invoke()
        }
    }

    suspend fun fetchCurrencyExchange(onFinish : (() -> Unit)? = null) = withContext(Dispatchers.IO){
        try{
            /*val favCurrenciesCode = appDatabase.currencyDao().getFavoritedCurrencies()?.map { it.code } ?: return@withContext

            var currenciesToUpdate = appDatabase.currencyDao().getCurrenciesToUpdate()?.map { it.code } ?: return@withContext

            val exchangeHistory = appDatabase.historyDao().getAllHistory()

            if(favCurrenciesCode.isEmpty()) return@withContext

            if(exchangeHistory.isEmpty()) currenciesToUpdate = favCurrenciesCode

            if(currenciesToUpdate.isEmpty()) onFinish?.invoke()

            val baseCode = prefs.getString(SHARED_KEY_BASE_CURRENCY, EMPTY_STRING) ?: EMPTY_STRING
            val requestBuild = currenciesToUpdate.map { "$it-$baseCode" }

            requestBuild.forEach { codeCombination ->
                val response = exchangeRatesService.fetchDataByCurrencyComparation(codeCombination)?.execute()

                val bodyOfCombination = response?.body()?.get(codeCombination.replace("-",EMPTY_STRING))?.toString() ?: EMPTY_STRING

                if(bodyOfCombination.isNotBlank()){
                    val exchangeObject  = JSONObject(bodyOfCombination)

                    val codeIn = exchangeObject.get(JSON_CODE_KEY)
                    val value = exchangeObject.get(JSON_HIGH_KEY)

                    val history = CurrencyHistory(
                        baseCode = baseCode,
                        inCode = codeIn as String,
                        value = value as String,
                        createAt = LocalDateTime.now().getDateNowFormatted()
                    )

                    saveExchangeHistory(history)
                }
            }*/
        }catch (e : Exception){
            Log.e("DEBUG", "Failed to update all currencies. Details : ${e.stackTraceToString()}")
        }finally {
            onFinish?.invoke()
        }
    }


    private fun saveExchangeHistory(currencyHistory: CurrencyHistory) = CoroutineScope(Dispatchers.IO).launch {
        val existentHistory : CurrencyHistory? = appDatabase.historyDao().getHistoryByCode(currencyHistory.inCode)

        if(existentHistory != null){
            appDatabase.historyDao().updateHistory(currencyHistory.value, currencyHistory.createAt, currencyHistory.inCode)
        }else{
            appDatabase.historyDao().insert(currencyHistory)
        }
    }

    suspend fun getFavCurrencies() = withContext(Dispatchers.IO){
        return@withContext appDatabase.currencyDao().getFavoritesCurrencies()
    }

    suspend fun getValueByCurrency(code : String) = withContext(Dispatchers.IO){
        return@withContext appDatabase.historyDao().getHistoryByCode(code)
    }

    suspend fun getCurrencies() = withContext(Dispatchers.IO){
        return@withContext appDatabase.currencyDao().getCurrencies()
    }

    suspend fun saveBaseCurrency(baseCurrencyCode : String) = withContext(Dispatchers.IO){
        prefs.edit { putString(SHARED_KEY_BASE_CURRENCY, baseCurrencyCode) }
    }

    suspend fun getBaseCurrencyPreference() : String = withContext(Dispatchers.IO){
        return@withContext ""
    }

    suspend fun getCurrencyByCode(code : String ) : Currency? = withContext(Dispatchers.IO){
        return@withContext null
    }

    suspend fun getBaseCurrency() : Currency? = withContext(Dispatchers.IO){
        val code = prefs.getString(SHARED_KEY_BASE_CURRENCY, EMPTY_STRING) ?: EMPTY_STRING
        return@withContext appDatabase.currencyDao().getCurrencyByCode(code)
    }

    suspend fun saveFavoritesCurrencies(favoritesCurrencies : List<String>) = withContext(Dispatchers.IO){
        return@withContext runCatching {
            appDatabase.currencyDao().resetFavorites()
            appDatabase.currencyDao().saveFavoritesCurrencies(favoritesCurrencies)
        }
    }

    suspend fun getLastUpdate() : String = withContext(Dispatchers.IO){
        /*val lastUpdate = appDatabase.historyDao().getAllHistory().firstOrNull()?.createAt ?: EMPTY_STRING
        val lastUpdateAsLocalDate = lastUpdate.convertToLocalDateTime() ?: return@withContext EMPTY_STRING

        val formatter = DateTimeFormatter.ofPattern(UPDATE_DATETIME_FORMAT, DEFAULT_LOCALE)
        val formattedDateTime = lastUpdateAsLocalDate.format(formatter)*/

        return@withContext "formattedDateTime"
    }

}