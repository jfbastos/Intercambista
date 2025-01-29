package com.zamfir.intercambista.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import com.zamfir.intercambista.data.database.AppDatabase
import com.zamfir.intercambista.data.rest.dto.restcountries.CountriesInfo
import com.zamfir.intercambista.data.rest.dto.restcountries.CountryApiResponseDTO
import com.zamfir.intercambista.data.rest.service.RestCountriesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class CountryRepository @Inject constructor(private val restCountryService: RestCountriesService, private val appDatabase: AppDatabase){

    suspend fun fetchCountryByCurrency() = withContext(Dispatchers.IO){

        restCountryService.fetchDataByCountryCurrency()?.enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                try{
                    if(response.isSuccessful){
                        val gson = Gson()
                        val type = object : TypeToken<CountriesInfo>() {}.type
                        val values = gson.fromJson<CountriesInfo>(response.body().toString(), type)

                        saveCurrencyInfo(values)
                    }
                }catch (e : Exception){
                    Log.e("DEBUG", "Failing in validating ${e.stackTraceToString()}")
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                Log.d("DEBUG", "Response : $t")
            }
        })
    }

    private fun saveCurrencyInfo(countryInfo: ArrayList<CountryApiResponseDTO>) = CoroutineScope(Dispatchers.IO).launch {
        val currencies = appDatabase.currencyDao().getCurrencies()

        if(currencies.isNullOrEmpty()) return@launch
        try{
            countryInfo.forEach { countryInfo ->
                val countryCurrencyCode = countryInfo.currencyInfo.keys.firstOrNull()
                currencies.firstOrNull { it.code == countryCurrencyCode }?.let { currency ->
                    appDatabase.currencyDao().updateCurrencyInfo(
                        flagUrl = countryInfo.flags.png,
                        symbol = countryInfo.currencyInfo.values.map { it.symbol }.firstOrNull() ?: "",
                        id = currency.id
                    )
                }
            }
        }catch (e : Exception){
            Log.e("DEBUG", "Failing in validating ${e.stackTraceToString()}")
        }
    }
}