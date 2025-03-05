package com.zamfir.intercambista.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zamfir.intercambista.data.database.AppDatabase
import com.zamfir.intercambista.data.rest.dto.restcountries.CountriesInfo
import com.zamfir.intercambista.data.rest.dto.restcountries.CountryApiResponseDTO
import com.zamfir.intercambista.data.rest.service.RestCountriesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CountryRepository @Inject constructor(private val restCountryService: RestCountriesService, private val appDatabase: AppDatabase){

    suspend fun fetchCountryByCurrency(onFinish : () -> Unit) = withContext(Dispatchers.IO){

        val request = restCountryService.fetchDataByCountryCurrency()?.execute() ?: return@withContext

        if(request.isSuccessful){
            val gson = Gson()
            val type = object : TypeToken<CountriesInfo>() {}.type
            val values = gson.fromJson<CountriesInfo>(request.body().toString(), type)

            saveCurrencyInfo(values)

            onFinish.invoke()
        }
    }

    private fun saveCurrencyInfo(countryInfo: ArrayList<CountryApiResponseDTO>){
        val currencies = appDatabase.currencyDao().getCurrencies()

        if(currencies.isNullOrEmpty()) return
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