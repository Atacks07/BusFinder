package com.raypazv.bfv2.data.users

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LoginResponse(

  @Expose
  @SerializedName("ok")
  val ok: Boolean?,

  @Expose
  @SerializedName("errorCode")
  val errorCode: Int?,

  @Expose
  @SerializedName("user")
  val user: User?,

  @Expose
  @SerializedName("token")
  val token: String?

) : Parcelable
