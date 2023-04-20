package com.raypazv.bfv2.data.users

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LoginForm(
  val email: String?,
  val password: String?
) : Parcelable
