package com.zamfir.intercambista.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zamfir.intercambista.data.database.entity.CurrencyHistory

@Dao
interface CurrencyHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(history : CurrencyHistory)

    @Query("SELECT * FROM CURRENCY_HISTORY")
    fun getAllHistory() : List<CurrencyHistory>

    @Query("SELECT * FROM CURRENCY_HISTORY WHERE IN_CODE = :code")
    fun getHistoryByCode(code : String) : CurrencyHistory?

    @Query("UPDATE CURRENCY_HISTORY SET VALUE = :newValue, CREATED_AT = :updateTime WHERE IN_CODE = :code")
    fun updateHistory(newValue : String, updateTime : String, code: String)

    @Query("DELETE FROM CURRENCY_HISTORY")
    fun clear()

}