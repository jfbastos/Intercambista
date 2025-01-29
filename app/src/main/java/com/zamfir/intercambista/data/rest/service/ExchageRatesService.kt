package com.zamfir.intercambista.data.rest.service

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchageRatesService {

    @GET("json/last/{query}")
    fun fetchDataByCurrencyComparation(@Path("query") query : String) : Call<JsonObject>?

    @GET("json/available/uniq")
    fun fetchAvaliableCurrencies() : Call<JsonObject>?



}