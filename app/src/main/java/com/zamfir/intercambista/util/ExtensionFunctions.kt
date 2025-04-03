package com.zamfir.intercambista.util

import com.zamfir.intercambista.util.Constants.DEFAULT_DB_DATETIME
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun String.convertToLocalDateTime() : LocalDateTime?{
    val formatter = DateTimeFormatter.ofPattern(DEFAULT_DB_DATETIME)

    try {
        return LocalDateTime.parse(this, formatter)
    } catch (e: DateTimeParseException) {
        System.err.println("Error parsing the date-time string: " + e.message)
        return null
    }
}

fun LocalDateTime.getDateNowFormatted() : String{
    return try {
        this.format(DateTimeFormatter.ofPattern(DEFAULT_DB_DATETIME))
    }catch (e : Exception){
        ""
    }
}