package com.raypazv.bfv2.util

import android.content.Context
import android.content.SharedPreferences

class Settings(private val context: Context) {

  init {
    this.readSettings()
  }

  private lateinit var settings: HashMap<String, Any>
  private lateinit var sharedPreferences: SharedPreferences
  private lateinit var preferencesEditor: SharedPreferences.Editor

  private fun readSettings() {
    settings = hashMapOf(
      "keepScreenOn" to false,
      "moveCamera" to false,
      "keepSessionActive" to true,
      "theme" to "system",
      "routeColor" to "#FF076A"
    )

    sharedPreferences = context.getSharedPreferences("BusFinderPreferences", Context.MODE_PRIVATE)
    if (!sharedPreferences.contains("keepScreenOn")) {
      this.setupDefaultSettings()
    }
  }

  private fun setupDefaultSettings() {

  }
}