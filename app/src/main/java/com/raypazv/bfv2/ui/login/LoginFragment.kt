package com.raypazv.bfv2.ui.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.raypazv.bfv2.App
import com.raypazv.bfv2.R
import com.raypazv.bfv2.data.DataState
import com.raypazv.bfv2.data.users.LoginForm
import com.raypazv.bfv2.data.users.LoginResponse
import com.raypazv.bfv2.data.users.User
import com.raypazv.bfv2.databinding.FragmentLoginBinding
import com.raypazv.bfv2.viewmodels.UserStateEvent
import com.raypazv.bfv2.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

  private lateinit var binding: FragmentLoginBinding

  private val userViewModel by viewModels<UserViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

    binding.emailTextInput.editText!!.addTextChangedListener {
      binding.emailTextInput.error = null
    }

    binding.passwordTextInput.editText!!.addTextChangedListener {
      binding.passwordTextInput.error = null
    }

    binding.loginMaterialButton.setOnClickListener {
      binding.emailTextInput.error = null
      binding.passwordTextInput.error = null

      val email = binding.emailTextInput.editText!!.text.toString()
      val password = binding.passwordTextInput.editText!!.text.toString()

      if (email.isNotEmpty() && password.isNotEmpty()) {
        val loginForm = LoginForm(email, password)
        sendLoginData(loginForm)
      }

      if (email.isEmpty()) {
        binding.emailTextInput.error = getString(R.string.emailErrorText)
      }

      if (password.isEmpty()) {
        binding.passwordTextInput.error = getString(R.string.passwordErrorText)
      }
    }

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    this.subscribeToObservers()
  }

  private fun subscribeToObservers() {
    userViewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
      when (dataState) {
        is DataState.Success<LoginResponse> -> {
          if (dataState.data.ok!!) {
            this.login(dataState.data)
          } else {
            if (dataState.data.errorCode == 0) {
              Snackbar.make(
                binding.mainLoginLayout,
                R.string.wrongCredentialsText,
                Snackbar.LENGTH_LONG
              ).show()
            } else if (dataState.data.errorCode == 1) {
              Snackbar.make(
                binding.mainLoginLayout,
                "Already logged in", //TODO Change for string resource
                Snackbar.LENGTH_LONG
              ).show()
            }
          }
        }
        is DataState.Loading -> {
          println("Loading")
        }
        is DataState.Error -> {
          println("Error - ${dataState.exception::class.simpleName}")
          println("Error message - ${dataState.exception.message}")
        }
      }
    })
  }

  private fun sendLoginData(loginForm: LoginForm) {
    userViewModel.setStateEvent(UserStateEvent.LoginEvent, loginForm)
  }

  private fun login (loginResponse: LoginResponse) {
    (requireActivity().application as App).user = loginResponse.user!!
    (requireActivity().application as App).token = loginResponse.token!!

    val user: User = loginResponse.user

    val sharedPreferences = requireContext().getSharedPreferences("BusFinderPreferences", Context.MODE_PRIVATE)
    val preferencesEditor = sharedPreferences.edit()

    preferencesEditor.putInt("userId", user.id!!)
    preferencesEditor.putString("userName", user.name!!)
    preferencesEditor.putString("userEmail", user.email!!)
    preferencesEditor.putBoolean("userDriver", user.isDriver!!)

    val pathsString = mutableSetOf<String>()

    user.paths!!.forEach { path ->
      pathsString.add(path.toString())
    }

    preferencesEditor.putStringSet("userPaths", pathsString)
    preferencesEditor.putString("token", loginResponse.token)

    preferencesEditor.apply()


    val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)

    findNavController().popBackStack()
  }
}
