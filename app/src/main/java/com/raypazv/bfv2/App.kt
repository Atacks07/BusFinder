package com.raypazv.bfv2

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.raypazv.bfv2.data.users.User
import com.raypazv.bfv2.util.NetworkConstants
import dagger.hilt.android.HiltAndroidApp
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI

@HiltAndroidApp
class App: Application() {

  lateinit var socket: Socket

  private lateinit var sharedPreferences: SharedPreferences

  var user = User(-1, "", "", false, arrayOf())
  var token = ""

  override fun onCreate() {
    super.onCreate()

    try {
      val uri = URI.create(NetworkConstants.BASE_URL)
      val options = IO.Options.builder().build()

      socket = IO.socket(uri, options)
    } catch (exception: Exception) {
      exception.printStackTrace()
    }

    sharedPreferences = this.getSharedPreferences("BusFinderPreferences", Context.MODE_PRIVATE)
    this.retrieveUserData()
  }

  private fun retrieveUserData() {
    if (sharedPreferences.contains("userId")) {
      this.user.id = sharedPreferences.getInt("userId", -1)
      this.user.name = sharedPreferences.getString("userName", "")
      this.user.email = sharedPreferences.getString("userEmail", "")
      this.user.isDriver = sharedPreferences.getBoolean("userDriver", false)

      val pathsString = sharedPreferences.getStringSet("userPaths", mutableSetOf())

      if (pathsString!!.size > 0) {
        val pathsPre = mutableListOf<Int>()

        pathsString.forEach { path ->
          pathsPre.add(path.toInt())
        }

        val paths = Array(pathsPre.size) { i -> -1}

        println(paths.size)

        for (i in paths.indices) {
          paths[i] = pathsPre[i]
        }

        this.user.paths = paths
      } else {
        this.user.paths = arrayOf()
      }

      this.token = sharedPreferences.getString("token", "")!!
    }
  }
}
