package com.raypazv.bfv2.data.paths

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Point (
  @Expose
  @SerializedName("lat")
  val latitude: Double?,

  @Expose
  @SerializedName("lon")
  val longitude: Double?
): Parcelable
