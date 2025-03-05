package com.zamfir.intercambista.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zamfir.intercambista.data.database.entity.Currency

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCurrency(currencies : List<Currency>)

    @Query("SELECT * FROM CURRENCY ORDER BY INFO ASC")
    fun getCurrencies() : List<Currency>?

    @Query("SELECT * FROM CURRENCY WHERE IS_FAVORITED = 1")
    fun getFavoritedCurrencies() : List<Currency>?

    @Query("UPDATE CURRENCY SET FLAG = :flagUrl, SYMBOL = :symbol WHERE ID = :id")
    fun updateCurrencyInfo(flagUrl : String, symbol : String, id : Long)

    @Query("SELECT * FROM CURRENCY WHERE CODE LIKE :code")
    fun getCurrencyByCode(code : String) : Currency?

    @Query("UPDATE CURRENCY SET IS_FAVORITED = 1 WHERE CODE IN (:favorites)")
    fun saveFavoritesCurrencies(favorites : List<String>)

    @Query("UPDATE CURRENCY SET IS_FAVORITED = 0")
    fun resetFavorites()
}