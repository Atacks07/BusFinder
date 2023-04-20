package com.raypazv.bfv2.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import com.raypazv.bfv2.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    /*installSplashScreen().apply {

      setKeepOnScreenCondition {
        viewModel.isLoading.value
      }

      setOnExitAnimationListener {
        println("Exit splash screen")
      }
    }*/
    setContentView(R.layout.activity_main)
  }
}
