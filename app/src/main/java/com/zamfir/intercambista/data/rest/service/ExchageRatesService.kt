package com.zamfir.intercambista.data.rest.service

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface ExchageRatesService {

    @Headers("x-api-key: 6062439f9def57da78f6cf8de7044afb0ce7172885b7165c755df92c5ff06b59")
    @GET("json/last/{query}")
    fun fetchDataByCurrencyComparation(@Path("query") query : String) : Call<JsonObject>?

    @Headers("x-api-key: 6062439f9def57da78f6cf8de7044afb0ce7172885b7165c755df92c5ff06b59")
    @GET("json/available/uniq")
    fun fetchAvaliableCurrencies() : Call<JsonObject>?



}