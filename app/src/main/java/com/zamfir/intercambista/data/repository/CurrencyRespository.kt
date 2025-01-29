package com.zamfir.intercambista.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.zamfir.intercambista.data.database.AppDatabase
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.data.rest.service.ExchageRatesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRespository @Inject constructor(private val exchageRatesService: ExchageRatesService, private val appDatabase: AppDatabase) {

    suspend fun fetchAvaliableCurrencies(): Unit = withContext(Dispatchers.IO){
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

    suspend fun getCurrencies() = withContext(Dispatchers.IO){
        return@withContext appDatabase.currencyDao().getCurrencies()
    }
}