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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    suspend fun fetchAvailableCurrencies(): Unit = withContext(Dispatchers.IO){
        val currentCurrencies = appDatabase.currencyDao().getCurrencies()

        if(!currentCurrencies.isNullOrEmpty()) return@withContext

        exchageRatesService.fetchAvaliableCurrencies()?.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if(response.isSuccessful){
                    val values = Gson().fromJson(response.body(), hashMapOf<String, String>()::class.java)
                    persistAvailableCountries(values)
                }else{
                    Log.e("DEBUG", "Failed to request : ${response.code()}")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("DEBUG", "Failed : $t")
            }
        })
    }



     private fun persistAvailableCountries(countries : HashMap<String, String>) = CoroutineScope(Dispatchers.IO).launch{
        val currencyMapped = countries.map {
            Currency(
                code = it.key,
                info = it.value
            )
        }

        appDatabase.currencyDao().addCurrency(currencyMapped)
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
}