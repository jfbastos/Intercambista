package com.zamfir.intercambista.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CURRENCY_HISTORY")
data class Country(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ID") var id : Long = 0,
    @ColumnInfo(name = "BASE_CODE") var baseCode : String = "",
    @ColumnInfo(name = "IN_CODE") var inCode : String = "",
    @ColumnInfo(name = "VALUE") var value : String = "",
    @ColumnInfo(name = "CREATED_AT") var createAt : String = ""
)