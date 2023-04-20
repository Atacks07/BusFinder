package com.raypazv.bfv2.ui.main.modules

import com.google.android.gms.maps.model.LatLng
import com.raypazv.bfv2.data.paths.Point

class PathsModule {
  companion object {

    fun createPathPositions(points: Array<Point>): ArrayList<LatLng> {
      val positions = ArrayList<LatLng>()
        for (point in points) {
          val position = LatLng(point.latitude!!, point.longitude!!)
          positions.add(position)
        }

      return positions
    }
  }
}
