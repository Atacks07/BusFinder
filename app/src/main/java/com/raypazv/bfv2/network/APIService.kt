package com.raypazv.bfv2.network

import com.raypazv.bfv2.data.paths.Path
import com.raypazv.bfv2.data.position.PositionUpdate
import com.raypazv.bfv2.data.position.StopSharingRequest
import com.raypazv.bfv2.data.users.LoginForm
import com.raypazv.bfv2.data.users.LoginResponse
import com.raypazv.bfv2.util.NetworkConstants
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface APIService {

  /* Paths */

  @GET("${NetworkConstants.BASE_URL}${NetworkConstants.ALL_PATHS}")
  suspend fun getAllPaths(): List<Path>

  @GET("${NetworkConstants.BASE_URL}${NetworkConstants.ALL_PATHS}/{id}")
  suspend fun getPathById(@retrofit2.http.Path("id") id: Int): List<Path>

  /* Location sharing*/

  @POST("${NetworkConstants.BASE_URL}${NetworkConstants.POSITION_CHECK}")
  suspend fun positionCheck(@Header("token") token: String, @Body positionUpdate: PositionUpdate): Boolean

  @POST("${NetworkConstants.BASE_URL}${NetworkConstants.STOP_SHARING}")
  suspend fun stopSharing(@Body stopSharingRequest: StopSharingRequest): Boolean

  /* Login */

  @POST("${NetworkConstants.BASE_URL}${NetworkConstants.LOGIN}")
  suspend fun login(@Body loginForm: LoginForm): LoginResponse
}
