package com.zamfir.intercambista.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zamfir.intercambista.data.database.dao.CurrencyDao
import com.zamfir.intercambista.data.database.dao.CurrencyHistoryDao
import com.zamfir.intercambista.data.database.entity.Currency
import com.zamfir.intercambista.data.database.entity.CurrencyHistory

@Database(entities = [CurrencyHistory::class, Currency::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    companion object{
        const val DATABASE_NAME = "INTERCAMBISTA_DB"
    }

    abstract fun currencyDao() : CurrencyDao
    abstract fun historyDao() : CurrencyHistoryDao
}