package com.raypazv.bfv2.repositories

import com.raypazv.bfv2.data.DataState
import com.raypazv.bfv2.data.paths.Path
import com.raypazv.bfv2.data.position.PositionUpdate
import com.raypazv.bfv2.data.position.StopSharingRequest
import com.raypazv.bfv2.network.APIService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PathRepository @Inject constructor(private val apiService: APIService) {
  suspend fun getAllPaths(): Flow<DataState<List<Path>>> = flow {
    emit(DataState.Loading)
    try {
      val paths = apiService.getAllPaths()
      emit(DataState.Success(paths))
    } catch (exception: Exception) {
      emit(DataState.Error(exception))
    }
  }

  suspend fun getPathById(id: Int): Flow<DataState<Path>> = flow {
    emit(DataState.Loading)
    try {
      val paths = apiService.getPathById(id)
      emit(DataState.Success(paths[0]))
    } catch (exception: Exception) {
      emit(DataState.Error(exception))
    }
  }

  suspend fun positionCheck(token: String, positionUpdate: PositionUpdate): Flow<DataState<String>> = flow {
    emit(DataState.Loading)
    try {
      val positionCheck = apiService.positionCheck(token, positionUpdate)
      emit(DataState.Success("PositionCheck=${positionCheck}"))
    } catch (exception: Exception) {
      emit(DataState.Error(exception))
    }
  }

  suspend fun stopSharing(idSocket: String): Flow<DataState<String>> = flow {
    emit(DataState.Loading)
    try {
      val stopSharingResponse = apiService.stopSharing(StopSharingRequest(idSocket))
      emit(DataState.Success("StopSharing=${stopSharingResponse}"))
    } catch (exception: Exception) {
      emit(DataState.Error(exception))
    }
  }
}
