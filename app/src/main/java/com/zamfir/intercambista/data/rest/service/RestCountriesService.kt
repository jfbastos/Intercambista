package com.zamfir.intercambista.data.rest.service

import com.google.gson.JsonArray
import retrofit2.Call
import retrofit2.http.GET

interface RestCountriesService {

    @GET("all?fields=currencies,flags")
    fun fetchDataByCountryCurrency() : Call<JsonArray>?
}