package com.zamfir.intercambista.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.zamfir.intercambista.util.DeviceUtils.hasConnection
import com.zamfir.intercambista.viewmodel.CurrencyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel : CurrencyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            viewModel.getBaseCurrencyAsLiveData(this@MainActivity.hasConnection())
        }

        viewModel.uiBaseCurrencyLiveData.observe(this){ currencyState ->
            if(currencyState.baseCurrency != null) startActivity(Intent(this, CurrencyActivity::class.java))
            else startActivity(Intent(this, FirstAccessActivity::class.java))
            finish()
        }
    }
}