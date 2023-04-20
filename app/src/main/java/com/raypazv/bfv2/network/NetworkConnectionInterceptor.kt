package com.raypazv.bfv2.network

import android.content.Context
import android.net.ConnectivityManager
import okhttp3.Interceptor
import okhttp3.Response

class NetworkConnectionInterceptor(val context: Context): Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    if (!isConnected()) {
      println("No connectivity")
    }

    val builder = chain.request().newBuilder()
    return chain.proceed(builder.build())
  }

  private fun isConnected(): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return (networkInfo != null && networkInfo.isConnected)
  }
}
