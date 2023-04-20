package com.raypazv.bfv2.util

import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.sign

interface LatLngInterpolator {
  public fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng

  class LinearFixed : LatLngInterpolator {
    override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
      val lat = (b.latitude - a.latitude) * fraction + a.latitude
      var lonDelta = b.longitude - a.longitude

      if (abs(lonDelta) > 180) {
        lonDelta -= sign(lonDelta) * 360
      }

      val lon = lonDelta * fraction + a.longitude
      return LatLng(lat, lon)
    }
  }
}