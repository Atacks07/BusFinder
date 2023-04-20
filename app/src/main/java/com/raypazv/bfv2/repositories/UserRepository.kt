package com.raypazv.bfv2.repositories

import com.raypazv.bfv2.data.DataState
import com.raypazv.bfv2.data.users.LoginForm
import com.raypazv.bfv2.data.users.LoginResponse
import com.raypazv.bfv2.network.APIService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepository @Inject constructor(private val apiService: APIService) {

  suspend fun login(loginForm: LoginForm): Flow<DataState<LoginResponse>> = flow {
    emit(DataState.Loading)
    try {
      val loginResponse = apiService.login(loginForm)
      emit(DataState.Success(loginResponse))
    } catch (exception: Exception) {
      emit(DataState.Error(exception))
    }
  }
}
