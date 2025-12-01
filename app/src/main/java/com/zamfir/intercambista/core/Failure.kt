package com.zamfir.intercambista.core

sealed class Failure {
    data class Network(val code : Int, val msg : String) : Failure()
    data class Unknown(val message: String?) : Failure()
}