package com.raypazv.bfv2.data.users

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(

  @Expose
  @SerializedName("id")
  var id: Int?,

  @Expose
  @SerializedName("name")
  var name: String?,

  @Expose
  @SerializedName("email")
  var email: String?,

  @Expose
  @SerializedName("driver")
  var isDriver: Boolean?,

  @Expose
  @SerializedName("paths")
  var paths: Array<Int>?
) : Parcelable