package com.raypazv.bfv2.data.paths

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Path (

  @Expose
  @SerializedName("idPath")
  val id: Int?,

  @Expose
  @SerializedName("name")
  val name: String?,

  @Expose
  @SerializedName("points")
  val points: Array<Point>?
): Parcelable
