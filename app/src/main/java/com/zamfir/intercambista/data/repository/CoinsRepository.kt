package com.zamfir.intercambista.data.repository

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zamfir.intercambista.core.Either
import com.zamfir.intercambista.data.database.AppDatabase
import com.zamfir.intercambista.data.database.dto.ExchangeDto
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.data.database.entity.CurrencyHistory
import com.zamfir.intercambista.data.rest.dto.restcountries.CountriesInfo
import com.zamfir.intercambista.data.rest.dto.restcountries.CountryApiResponseDTO
import com.zamfir.intercambista.data.rest.service.ExchageRatesService
import com.zamfir.intercambista.data.rest.service.RestCountriesService
import com.zamfir.intercambista.util.Constants.DEFAULT_LOCALE
import com.zamfir.intercambista.util.Constants.EMPTY_STRING
import com.zamfir.intercambista.util.Constants.JSON_CODE_KEY
import com.zamfir.intercambista.util.Constants.JSON_HIGH_KEY
import com.zamfir.intercambista.util.Constants.SHARED_KEY_BASE_CURRENCY
import com.zamfir.intercambista.util.Constants.UPDATE_DATETIME_FORMAT
import com.zamfir.intercambista.util.convertToLocalDateTime
import com.zamfir.intercambista.util.getDateNowFormatted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.component1
import kotlin.collections.component2

@Singleton
class CoinsRepository @Inject constructor(
    private val context: Context,
    private val exchangeRatesService: ExchageRatesService,
    private val countriesRest: RestCountriesService,
    private val appDatabase: AppDatabase
){

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    suspend fun isCurrencyAlreadySelected() = withContext(Dispatchers.IO){
        return@withContext prefs.getString(SHARED_KEY_BASE_CURRENCY, EMPTY_STRING) != ""
    }

    suspend fun getCurrencies() = withContext(Dispatchers.IO){
        return@withContext appDatabase.currencyDao().getCurrencies()
    }

    fun fetchCurrencies() : Flow<Either<String, String>> = flow {
        try {
            emit(Either.Right("Realizando o download das moedas..."))

            val requestExchanges = fetchExchanges()

            if(requestExchanges.isSuccessful){
                val availableCountriesInApi = Gson().fromJson(requestExchanges.body(), hashMapOf<String, String>()::class.java)

                availableCountriesInApi.forEach{ (code, info) ->
                    if(checkIfCoinExists(code)) persistCurrency(code, info)
                }

                emit(Either.Right("Realizando o download de informações das moedas..."))

                val requestCountriesInfo = fetchCountryInfo()

                if (requestCountriesInfo.isSuccessful) {
                    val type = object : TypeToken<CountriesInfo>() {}.type
                    val countriesInfo =
                        Gson().fromJson<CountriesInfo>(requestCountriesInfo.body().toString(), type)

                    countriesInfo.forEach { countryInfo ->
                        val countryCurrencyCode = countryInfo.currencyInfo.keys.firstOrNull() ?: ""
                        updateCurrency(countryInfo, countryCurrencyCode)
                    }
                }else{
                    emit(Either.Left("Falha ao realizar o download das informações. Detalhes: ${requestExchanges.message()}"))
                }
            }else{
                emit(Either.Left("Falha ao realizar o download das moedas. Detalhes: ${(requestExchanges.errorBody() as ResponseBody).string()}"))
            }
        }catch (e : Exception){
            emit(Either.Left("Falha ao realizar o download das moedas. \n\nDetalhes: ${e.stackTraceToString()}"))
        }

    }

    private suspend fun updateCurrency(
        countryInfo: CountryApiResponseDTO,
        countryCurrencyCode: String
    ) = withContext(Dispatchers.IO) {
        appDatabase.currencyDao().updateCurrencyInfo(
            flagUrl = countryInfo.flags.png,
            symbol = countryInfo.currencyInfo.values.map { it.symbol }.firstOrNull() ?: "",
            code = countryCurrencyCode
        )
    }

    private suspend fun persistCurrency(code: String, info: String) = withContext(Dispatchers.IO){
        appDatabase.currencyDao().addCurrency(Currency(code = code, info = info))
    }

    private suspend fun fetchExchanges() = withContext(Dispatchers.IO) { exchangeRatesService.fetchAvaliableCurrencies()?.execute() ?: throw RuntimeException("Impossible to fetch currencies. Detail: Request is null") }

    private suspend fun fetchCountryInfo() = withContext(Dispatchers.IO) { countriesRest.fetchDataByCountryCurrency()?.execute() ?: throw RuntimeException("Impossible to fetch currencies infos. Detail: Request is null") }

    private suspend fun checkIfCoinExists(coinCode : String) : Boolean = withContext(Dispatchers.IO){
        if(coinCode == "BRL") return@withContext true
        return@withContext exchangeRatesService.fetchDataByCurrencyComparation(coinCode)?.execute()?.isSuccessful == true
    }

    suspend fun hasFavorites() : Boolean = withContext(Dispatchers.IO){
        return@withContext !appDatabase.currencyDao().getFavoritesCurrencies().isNullOrEmpty()
    }

    suspend fun saveBaseCurrency(baseCurrencyCode : String) = withContext(Dispatchers.IO){
        prefs.edit { putString(SHARED_KEY_BASE_CURRENCY, baseCurrencyCode) }
    }

    suspend fun getBaseCurrency() : Currency? = withContext(Dispatchers.IO){
        val code = prefs.getString(SHARED_KEY_BASE_CURRENCY, EMPTY_STRING) ?: EMPTY_STRING
        return@withContext appDatabase.currencyDao().getCurrencyByCode(code)
    }

    private suspend fun createRequestCodeCombination() : List<String> = withContext(Dispatchers.IO){

        val baseCode = prefs.getString(SHARED_KEY_BASE_CURRENCY, EMPTY_STRING) ?: EMPTY_STRING

        var favCurrenciesCode = appDatabase.currencyDao().getFavoritesCurrenciesCode().takeIf { !it.isNullOrEmpty() } ?: return@withContext listOf()

        if(favCurrenciesCode.contains(baseCode)){
            appDatabase.currencyDao().removeFavorite(baseCode)
            favCurrenciesCode = appDatabase.currencyDao().getFavoritesCurrenciesCode().takeIf { !it.isNullOrEmpty() } ?: return@withContext listOf()
        }

        var currenciesToUpdate = appDatabase.currencyDao().getCurrenciesToUpdate()

        val exchangeHistory = appDatabase.historyDao().getAllHistory()

        if(exchangeHistory.isNullOrEmpty()) currenciesToUpdate = favCurrenciesCode

        return@withContext currenciesToUpdate!!.map { "$it-$baseCode" }
    }

    suspend fun fetchCurrencyExchange() = withContext(Dispatchers.IO){
        return@withContext runCatching {
            val baseCode = prefs.getString(SHARED_KEY_BASE_CURRENCY, EMPTY_STRING) ?: EMPTY_STRING

            createRequestCodeCombination().forEach { codeCombination ->
                val response = exchangeRatesService.fetchDataByCurrencyComparation(codeCombination)?.execute()

                if(response?.isSuccessful == false){
                    val codeIn = codeCombination.split("-")[0]

                    val history = CurrencyHistory(
                        baseCode = baseCode,
                        inCode = codeIn,
                        value = "",
                        createAt = LocalDateTime.now().getDateNowFormatted()
                    )

                    saveExchangeHistory(history)
                }

                val bodyOfCombination = response?.body()?.get(codeCombination.replace("-",EMPTY_STRING))?.toString() ?: EMPTY_STRING

                if(bodyOfCombination.isNotBlank()){
                    val exchangeObject  = JSONObject(bodyOfCombination)

                    val codeIn = exchangeObject.get(JSON_CODE_KEY)
                    val value = exchangeObject.get(JSON_HIGH_KEY)

                    val history = CurrencyHistory(
                        baseCode = baseCode,
                        inCode = codeIn as String,
                        value = value as String,
                        createAt = LocalDateTime.now().getDateNowFormatted()
                    )

                    saveExchangeHistory(history)
                }
            }
        }
    }

    private fun saveExchangeHistory(currencyHistory: CurrencyHistory) = CoroutineScope(Dispatchers.IO).launch {
        val existentHistory : CurrencyHistory? = appDatabase.historyDao().getHistoryByCode(currencyHistory.inCode)

        if(existentHistory != null){
            appDatabase.historyDao().updateHistory(currencyHistory.value, currencyHistory.createAt, currencyHistory.inCode)
        }else{
            appDatabase.historyDao().insert(currencyHistory)
        }
    }

    suspend fun getFavoritesCurrenciesRates() = withContext(Dispatchers.IO){
        val exchanges = mutableListOf<ExchangeDto>()

        appDatabase.currencyDao().getFavoritesCurrencies()?.forEach { coin ->
            val history = appDatabase.historyDao().getHistoryByCode(coin.code)

            exchanges.add(
                ExchangeDto(
                    info = coin.info,
                    code = coin.code,
                    rate = history?.value?.ifBlank { "s/ cotação" } ?: "s/ cotação",
                    flag = coin.flag,
                    symbol = coin.symbol
                )
            )
        }

        exchanges.sortByDescending { it.rate != "s/ cotação" }

        return@withContext exchanges
    }

    suspend fun getLastUpdate() : String = withContext(Dispatchers.IO){
        val lastUpdate = appDatabase.historyDao().getLastUpdateTime() ?: EMPTY_STRING
        val lastUpdateAsLocalDate = lastUpdate.convertToLocalDateTime() ?: return@withContext EMPTY_STRING

        val formatter = DateTimeFormatter.ofPattern(UPDATE_DATETIME_FORMAT, DEFAULT_LOCALE)
        val formattedDateTime = lastUpdateAsLocalDate.format(formatter)

        return@withContext formattedDateTime
    }

    suspend fun clearCurrentBaseCoinData() = withContext(Dispatchers.IO){
        appDatabase.historyDao().clear()

        prefs.edit { remove(SHARED_KEY_BASE_CURRENCY) }
    }

    suspend fun toggleFavorite(coin : Currency) = withContext(Dispatchers.IO){
        val editedCoin = coin.copy(favorited = coin.favorited.not())

        appDatabase.currencyDao().toggleFavorite(editedCoin.code, editedCoin.favorited)

        return@withContext editedCoin
    }
}