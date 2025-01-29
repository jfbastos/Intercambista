package com.zamfir.intercambista.data.rest.dto.restcountries

import com.google.gson.annotations.SerializedName

typealias CountriesInfo = ArrayList<CountryApiResponseDTO>

data class CountryApiResponseDTO(
    @SerializedName("flags") val flags : Flag,
    @SerializedName("currencies") val currencyInfo : Map<String, CurrencyInfo>
)
