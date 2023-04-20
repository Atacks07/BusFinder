package com.raypazv.bfv2.util

import kotlin.math.*

class Distance {
  companion object {
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
      val degreesToRadians = (Math.PI / 180)

      val deltaLongitude = (lon2 - lon1) * degreesToRadians
      val deltaLatitude = (lat2 - lat1) * degreesToRadians

      val a = sin(deltaLatitude / 2).pow(2) + cos(lat1 * degreesToRadians) * cos(lat2 * degreesToRadians) * sin(deltaLongitude / 2).pow(2)
      val c = 2 * atan2(sqrt(a), sqrt(1 - a))
      val d = 6367 * c

      return d * 1000
    }
  }
}