package com.raypazv.bfv2.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.raypazv.bfv2.R
import com.raypazv.bfv2.main.MainActivity
import com.raypazv.bfv2.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

  @ServiceScoped
  @Provides
  fun providesFusedLocationProviderClient(@ApplicationContext app: Context) =
    FusedLocationProviderClient(app)

  @ServiceScoped
  @Provides
  fun providesMainActivityPendingIntent(@ApplicationContext app: Context): PendingIntent {

    val intentFlags = if (Build.VERSION.SDK_INT > 23) {
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
      PendingIntent.FLAG_UPDATE_CURRENT
    }

    return PendingIntent.getActivity(
      app,
      0,
      Intent(app, MainActivity::class.java),
      intentFlags
    )
  }
  @ServiceScoped
  @Provides
  fun providesBaseNotificationBuilder(
    @ApplicationContext app: Context,
    pendingIntent: PendingIntent
  ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
    .setAutoCancel(false)
    .setOngoing(true)
    .setSmallIcon(R.drawable.ic_location)
    .setContentTitle(app.getString(R.string.app_name))
    .setContentText(app.getString(R.string.notificationText))
    .setContentIntent(pendingIntent)
}