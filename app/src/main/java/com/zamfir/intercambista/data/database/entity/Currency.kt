package com.zamfir.intercambista.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CURRENCY")
data class Currency(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ID") var id : Long = 0,
    @ColumnInfo(name = "CODE") var code : String = "",
    @ColumnInfo(name = "INFO") var info : String = "",
    @ColumnInfo(name = "FLAG") var flag : String = "",
    @ColumnInfo(name = "SYMBOL") var symbol : String = "",
    @ColumnInfo(name = "IS_FAVORITED") var favorited : Boolean = false
)
