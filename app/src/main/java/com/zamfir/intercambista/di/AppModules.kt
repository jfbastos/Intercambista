package com.zamfir.intercambista.di

import android.content.Context
import androidx.room.Room
import com.zamfir.intercambista.data.database.AppDatabase
import com.zamfir.intercambista.data.repository.CountryRepository
import com.zamfir.intercambista.data.repository.CurrencyRespository
import com.zamfir.intercambista.data.rest.service.ExchageRatesService
import com.zamfir.intercambista.data.rest.service.RestCountriesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RestExchange

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RestCountry

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    //region Exchange
    @Singleton
    @RestExchange
    @Provides
    fun provideRetrofitExchange() : Retrofit {
        val logginInterceptor = HttpLoggingInterceptor()
        logginInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(logginInterceptor).build()
        return Retrofit.Builder().baseUrl("https://economia.awesomeapi.com.br/").addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build()
    }

    @Provides
    fun provideRetrofitExchageRatesService(@RestExchange exchageRatesRetrofit: Retrofit) : ExchageRatesService {
        return exchageRatesRetrofit.create(ExchageRatesService::class.java)
    }

    @Provides
    fun provideCurrencyRepository(exchageRatesService: ExchageRatesService,appDatabase: AppDatabase) = CurrencyRespository(exchageRatesService ,appDatabase)
    //endregion


    //region Country
    @Singleton
    @RestCountry
    @Provides
    fun provideRetrofitCountryInfo() : Retrofit {
        val logginInterceptor = HttpLoggingInterceptor()
        logginInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(logginInterceptor).build()
        return Retrofit.Builder().baseUrl(" https://restcountries.com/v3.1/").addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build()
    }

    @Provides
    fun provideRetrofitCountryService(@RestCountry countryRetrofit : Retrofit) : RestCountriesService{
        return countryRetrofit.create(RestCountriesService::class.java)
    }

    @Provides
    fun provideCountryRepository(countriesRest : RestCountriesService, appDatabase: AppDatabase) = CountryRepository(countriesRest, appDatabase)

    //endregion

    @Provides
    fun provideDatabase(@ApplicationContext context : Context) : AppDatabase{
        return Room.databaseBuilder(context = context, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()
    }

}