package com.zamfir.intercambista.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zamfir.intercambista.data.database.entity.Currency

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCurrencies(currencies : List<Currency>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCurrency(currency: Currency)

    @Query("SELECT * FROM CURRENCY ORDER BY INFO ASC")
    fun getCurrencies() : List<Currency>?

    @Query("SELECT * FROM CURRENCY WHERE IS_FAVORITED = 1")
    fun getFavoritesCurrencies() : List<Currency>?

    @Query("SELECT CODE FROM CURRENCY WHERE IS_FAVORITED = 1")
    fun getFavoritesCurrenciesCode() : List<String>?

    @Query("SELECT c.CODE FROM CURRENCY c LEFT JOIN CURRENCY_HISTORY h ON c.CODE = h.IN_CODE AND DATE(h.CREATED_AT) >= DATE('now', 'localtime') WHERE h.IN_CODE IS NULL AND c.IS_FAVORITED = 1")
    fun getCurrenciesToUpdate() : List<String>?

    @Query("UPDATE CURRENCY SET FLAG = :flagUrl, SYMBOL = :symbol WHERE CODE = :code")
    fun updateCurrencyInfo(flagUrl : String, symbol : String, code : String)

    @Query("SELECT * FROM CURRENCY WHERE CODE LIKE :code")
    fun getCurrencyByCode(code : String) : Currency?

    @Query("UPDATE CURRENCY SET IS_FAVORITED = 1 WHERE CODE IN (:favorites)")
    fun saveFavoritesCurrencies(favorites : List<String>)

    @Query("UPDATE CURRENCY SET IS_FAVORITED = 0")
    fun resetFavorites()

    @Query("UPDATE CURRENCY SET IS_FAVORITED = 0 WHERE CODE = :code")
    fun removeFavorite(code : String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceCoin(coin : Currency)
}