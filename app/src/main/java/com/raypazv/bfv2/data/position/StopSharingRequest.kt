package com.raypazv.bfv2.data.position

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StopSharingRequest(

  @Expose
  @SerializedName("idSocket")
  val idSocket: String
): Parcelable
