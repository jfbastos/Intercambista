package com.zamfir.intercambista.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zamfir.intercambista.data.database.dao.CurrencyDao
import com.zamfir.intercambista.data.database.entity.Country
import com.zamfir.intercambista.data.database.entity.Currency

@Database(entities = [Country::class, Currency::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object{
        const val DATABASE_NAME = "INTERCAMBISTA_DB"
    }


    abstract fun currencyDao() : CurrencyDao
}