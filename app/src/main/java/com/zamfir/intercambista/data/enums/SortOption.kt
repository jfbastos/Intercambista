package com.zamfir.intercambista.data.enums

enum class SortOption(val value : String) {

    ASCENDING("ASC"),
    DESCENDING("DSC");

    companion object{
        fun getByValue(value : String) : SortOption{
            return when(value){
                "ASC" -> ASCENDING
                "DSC" -> DESCENDING
                else -> DESCENDING
            }
        }
    }

}