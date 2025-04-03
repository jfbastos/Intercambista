package com.zamfir.intercambista.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object DeviceUtils {

    fun Context.hasConnection(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return capabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

}