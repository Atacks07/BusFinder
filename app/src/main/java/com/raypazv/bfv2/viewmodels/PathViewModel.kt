package com.raypazv.bfv2.viewmodels

import androidx.lifecycle.*
import com.raypazv.bfv2.data.DataState
import com.raypazv.bfv2.data.position.PositionUpdate
import com.raypazv.bfv2.repositories.PathRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PathViewModel @Inject constructor(
  private val pathRepository: PathRepository,
  private val savedStateHandle: SavedStateHandle
) : ViewModel() {


  private val _dataState: MutableLiveData<DataState<Any>> = MutableLiveData()

  val dataState: LiveData<DataState<Any>> get () = _dataState

  fun setStateEvent(pathStateEvent: PathStateEvent, vararg params: Any?) {
    viewModelScope.launch {
      when (pathStateEvent) {
        is PathStateEvent.GetAllPathsEvent -> {
          pathRepository.getAllPaths().onEach { dataState ->
            _dataState.value = dataState
          }.launchIn(viewModelScope)
        }
        is PathStateEvent.GetPathByIdEvent -> {
          pathRepository.getPathById(params[0] as Int).onEach { dataState ->
            _dataState.value = dataState
          }.launchIn(viewModelScope)
        }
        is PathStateEvent.PositionCheckEvent -> {
          pathRepository.positionCheck( params[0] as String, params[1] as PositionUpdate).onEach { dataState ->
            _dataState.value = dataState
          }.launchIn(viewModelScope)
        }
        is PathStateEvent.StopSharingEvent -> {
          pathRepository.stopSharing(params[0] as String).onEach { dataState ->
            _dataState.value = dataState
          }.launchIn(viewModelScope)
        }
        is PathStateEvent.None -> {
          println("none")
        }
      }
    }
  }
}

sealed class PathStateEvent {
  object GetAllPathsEvent: PathStateEvent()
  object GetPathByIdEvent: PathStateEvent()
  object PositionCheckEvent: PathStateEvent()
  object StopSharingEvent: PathStateEvent()
  object None: PathStateEvent()
}