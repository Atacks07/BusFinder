package com.raypazv.bfv2.data.position

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PositionUpdate (
  @Expose
  @SerializedName("latitude")
  val latitude: Double,

  @Expose
  @SerializedName("longitude")
  val longitude: Double,

  @Expose
  @SerializedName("idPath")
  val idPath: Int,

  @Expose
  @SerializedName("idSocket")
  val idSocket: String,

  @Expose
  @SerializedName("driver")
  val isDriver: Boolean,

  @Expose
  @SerializedName("busId")
  val busId: String?

): Parcelable