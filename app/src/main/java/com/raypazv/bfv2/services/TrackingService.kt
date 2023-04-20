package com.raypazv.bfv2.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.raypazv.bfv2.R
import com.raypazv.bfv2.util.Constants
import com.raypazv.bfv2.util.Constants.ACTION_PAUSE_SERVICE
import com.raypazv.bfv2.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.raypazv.bfv2.util.Constants.ACTION_STOP_SERVICE
import com.raypazv.bfv2.util.Constants.FASTEST_LOCATION_UPDATE_INTERVAL
import com.raypazv.bfv2.util.Constants.LOCATION_UPDATE_INTERVAL
import com.raypazv.bfv2.util.Constants.NOTIFICATION_CHANNEL_ID
import com.raypazv.bfv2.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.raypazv.bfv2.util.Constants.NOTIFICATION_ID
import com.raypazv.bfv2.util.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {

  var isFirst = true

  var serviceKilled = false

  @Inject
  lateinit var fusedLocationProviderClient: FusedLocationProviderClient

  @Inject
  lateinit var baseNotificationBuilder: NotificationCompat.Builder

  lateinit var currentNotificationBuilder: NotificationCompat.Builder

  companion object {
    val isTracking = MutableLiveData<Boolean>()
    val currentLocation = MutableLiveData<Location>()
  }

  val locationCallback = object : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
      super.onLocationResult(locationResult)

      if (isTracking.value!!) {
        locationResult?.locations?.let { locations ->
          for (location in locations) {
            currentLocation.postValue(location)
          }
        }
      }
    }
  }

  override fun onCreate() {
    super.onCreate()
    currentNotificationBuilder = baseNotificationBuilder

    postInitialValues()
    fusedLocationProviderClient = FusedLocationProviderClient(this)

    isTracking.observe(this, Observer {
      updateLocationTracking(it)
      updateNotificationState(it)
    })
  }

  private fun killService() {
    serviceKilled = true
    isFirst = true
    pauseForegroundService()
    postInitialValues()
    stopForeground(true)
    stopSelf()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    intent?.let {
      when (it.action) {
        ACTION_START_OR_RESUME_SERVICE -> {
          if (isFirst) {
            startForegroundService()
            isFirst = false
            println("Tracking Service: Service started")
          } else {
            startForegroundService()
            println("Tracking Service: Service resumed")
          }
        }

        ACTION_PAUSE_SERVICE -> {
          pauseForegroundService()
          println("Tracking service: Service paused")
        }

        ACTION_STOP_SERVICE -> {
          killService()
        }
      }
    }
    return super.onStartCommand(intent, flags, startId)
  }

  private fun pauseForegroundService() {
    isTracking.postValue(false)
  }

  private fun startForegroundService() {
    isTracking.postValue(true)

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createNotificationChannel(notificationManager)
    }

    startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(notificationManager: NotificationManager) {
    val channel = NotificationChannel(
      NOTIFICATION_CHANNEL_ID,
      NOTIFICATION_CHANNEL_NAME,
      NotificationManager.IMPORTANCE_DEFAULT
    )

    notificationManager.createNotificationChannel(channel)
  }

  private fun postInitialValues() {
    isTracking.postValue(false)

    val location = Location("")
    location.latitude = 28.4073205
    location.longitude = -106.866998

    currentLocation.postValue(location)
  }

  private fun updateNotificationState(isTracking: Boolean) {
    val notificationActionText = if (isTracking) getString(R.string.notificationPauseText) else getString(R.string.notificationResumeText)
    var icon = R.drawable.ic_pause

    val intentFlags = if (Build.VERSION.SDK_INT > 23) {
      FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    } else {
      FLAG_UPDATE_CURRENT
    }

    println("Intent flags $intentFlags")

    val pendingIntent = if (isTracking) {
      val pauseIntent = Intent(this, TrackingService::class.java).apply {
        action = ACTION_PAUSE_SERVICE
      }
      PendingIntent.getService(this, 1, pauseIntent, intentFlags)
    } else {
      val resumeIntent = Intent(this, TrackingService::class.java).apply {
        action = ACTION_START_OR_RESUME_SERVICE
      }

      icon = R.drawable.ic_play
      PendingIntent.getService(this, 2, resumeIntent, intentFlags)
    }

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
      isAccessible = true
      set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
    }

    if (!serviceKilled) {
      currentNotificationBuilder = baseNotificationBuilder.addAction(icon, notificationActionText, pendingIntent)
      notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
    }
  }

  @SuppressLint("MissingPermission")
  private fun updateLocationTracking(isTracking: Boolean) {
    if (isTracking) {
      if (TrackingUtility.hasLocationPermissions(this)) {
        val locationRequest = LocationRequest().apply {
          interval = LOCATION_UPDATE_INTERVAL
          fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL
          priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
      } else {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
      }
    }
  }
}
