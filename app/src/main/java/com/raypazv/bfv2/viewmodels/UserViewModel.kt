package com.raypazv.bfv2.viewmodels

import androidx.lifecycle.*
import com.raypazv.bfv2.data.DataState
import com.raypazv.bfv2.data.users.LoginForm
import com.raypazv.bfv2.data.users.LoginResponse
import com.raypazv.bfv2.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val _dataState: MutableLiveData<DataState<LoginResponse>> = MutableLiveData()

  val dataState: LiveData<DataState<LoginResponse>> get() = _dataState

  fun setStateEvent(userStateEvent: UserStateEvent, loginForm: LoginForm) {
    viewModelScope.launch {
      when (userStateEvent) {
        is UserStateEvent.LoginEvent -> {
          userRepository.login(loginForm).onEach { dataState ->
            _dataState.value = dataState
          }.launchIn(viewModelScope)
        }

        is UserStateEvent.None -> {
          println("None")
        }
      }
    }
  }
}

sealed class UserStateEvent {
  object LoginEvent: UserStateEvent()
  object None: UserStateEvent()
}
