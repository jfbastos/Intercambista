package com.zamfir.intercambista.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.zamfir.intercambista.data.database.AppDatabase
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.data.rest.service.ExchageRatesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRespository @Inject constructor(
    private val dataStore : DataStore<Preferences>,
    private val exchageRatesService: ExchageRatesService,
    private val appDatabase: AppDatabase) {

    suspend fun fetchAvailableCurrencies(onFinish : () -> Unit) = withContext(Dispatchers.IO){
        val currentCurrencies = appDatabase.currencyDao().getCurrencies()

        if(!currentCurrencies.isNullOrEmpty()){
            onFinish.invoke()
            return@withContext
        }

        val request = exchageRatesService.fetchAvaliableCurrencies()?.execute() ?: return@withContext

        if(request.isSuccessful){
            val values = Gson().fromJson(request.body(), hashMapOf<String, String>()::class.java)
            persistAvailableCountries(values)
            onFinish.invoke()
        }else{
            Log.e("DEBUG", "Failed to request : ${request.code()}")
        }
    }

     private fun persistAvailableCountries(countries : HashMap<String, String>) {

         val coinsWithExchage = mutableListOf<Currency>()

         countries.forEach{ (code, info) ->
             if(checkIfCoinExists(code)){
                 coinsWithExchage.add(Currency(code = code, info = info))
             }
         }

        appDatabase.currencyDao().addCurrency(coinsWithExchage)
    }

    private fun checkIfCoinExists(coinCode : String) : Boolean{
        if(coinCode == "BRL") return true
        return exchageRatesService.fetchDataByCurrencyComparation(coinCode)?.execute()?.isSuccessful ?: false
    }

    suspend fun fetchCurrencyExchange() = withContext(Dispatchers.IO){
        val favCurrenciesCode = appDatabase.currencyDao().getFavoritedCurrencies()?.map { it.code } ?: return@withContext

        dataStore.data.map { prefences -> prefences[stringPreferencesKey("BASE_CURRENCY")] ?: "" }.collectLatest { baseCode ->
            val requestBuild = favCurrenciesCode.map { "$it-$baseCode" }

            requestBuild.forEach { codeCombination ->
                exchageRatesService.fetchDataByCurrencyComparation(codeCombination)?.enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        Log.d("DEBUG", "Response code : ${response.code()}, Response Body : ${response.body()}")
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                    }
                })
            }

        }
    }

    suspend fun getFavCurrencies() = withContext(Dispatchers.IO){
        return@withContext appDatabase.currencyDao().getFavoritedCurrencies()
    }

    suspend fun getCurrencies() = withContext(Dispatchers.IO){
        return@withContext appDatabase.currencyDao().getCurrencies()
    }

    suspend fun saveBaseCurrency(baseCurrencyCode : String) = withContext(Dispatchers.IO){
       dataStore.edit { settings ->
            settings[stringPreferencesKey("BASE_CURRENCY")] = baseCurrencyCode
       }
    }

    suspend fun getBaseCurrencyPreference() : Flow<String> = withContext(Dispatchers.IO){
        return@withContext dataStore.data.map { prefences -> prefences[stringPreferencesKey("BASE_CURRENCY")] ?: "" }
    }

    suspend fun getCurrencyByCode(code : String ) : Currency? = withContext(Dispatchers.IO){
        return@withContext appDatabase.currencyDao().getCurrencyByCode(code)
    }

    suspend fun saveFavoritedCurrencies(favoritesCurrencies : List<String>) = withContext(Dispatchers.IO){
        return@withContext runCatching {
            appDatabase.currencyDao().resetFavorites()
            appDatabase.currencyDao().saveFavoritesCurrencies(favoritesCurrencies)
        }
    }
}