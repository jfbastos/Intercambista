package com.zamfir.intercambista.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "COUNTRY")
data class Country(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ID") var id : Long = 0,
    @ColumnInfo(name = "CODE") var code : String = "",
    @ColumnInfo(name = "SYMBOL") var symbol : String = "",
    @ColumnInfo(name = "FLAG_URL") var flagUrl : String = ""
)