package com.zamfir.intercambista.data.rest.service

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RestCountriesService {

    @GET("all?fields=currencies,flags")
    fun fetchDataByCountryCurrency() : Call<JsonArray>?
}