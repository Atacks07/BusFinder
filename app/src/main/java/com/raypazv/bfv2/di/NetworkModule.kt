package com.raypazv.bfv2.di

import android.content.Context
import com.raypazv.bfv2.network.APIService
import com.raypazv.bfv2.network.NetworkConnectionInterceptor
import com.raypazv.bfv2.util.NetworkConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Provides
  fun provideBaseURL(): String = NetworkConstants.BASE_URL

  @Provides
  fun providesLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

  @Provides
  fun providesNetworkConnectionInterceptor(@ApplicationContext context: Context): NetworkConnectionInterceptor = NetworkConnectionInterceptor(context)

  @Provides
  fun providesOkHttpClient(logger: HttpLoggingInterceptor, networkConnectionInterceptor: NetworkConnectionInterceptor): OkHttpClient {
    val okHttpClient = OkHttpClient.Builder()
    okHttpClient.addInterceptor(logger)
    okHttpClient.addInterceptor(networkConnectionInterceptor)
    okHttpClient.callTimeout(10, TimeUnit.SECONDS)
    okHttpClient.connectTimeout(10, TimeUnit.SECONDS)
    okHttpClient.writeTimeout(10, TimeUnit.SECONDS)
    okHttpClient.readTimeout(10, TimeUnit.SECONDS)
    return okHttpClient.build()
  }

  @Provides
  fun providesConverterFactory(): Converter.Factory = GsonConverterFactory.create()

  @Provides
  fun providesRetrofit(baseURL: String, converterFactory: Converter.Factory, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder().baseUrl(baseURL).addConverterFactory(converterFactory).client(okHttpClient).build()

  @Provides
  fun providesAPIService(retrofit: Retrofit): APIService = retrofit.create(APIService::class.java)

}