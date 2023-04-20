package com.raypazv.bfv2.util

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.util.Property
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MarkerAnimation {
  companion object {
    fun animateMarker(marker: Marker, finalPosition: LatLng, latLngInterpolator: LatLngInterpolator) {
      val typeEvaluator = object : TypeEvaluator<LatLng> {
        override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
          return latLngInterpolator.interpolate(fraction, startValue, endValue)
        }
      }

      val property = Property.of(Marker::class.java, LatLng::class.java, "position")
      val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
      animator.duration = 500
      animator.start()
    }
  }
}
