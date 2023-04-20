package com.raypazv.bfv2.ui.main.modules

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.children
import com.raypazv.bfv2.R

class OptionsModule {

  companion object {
    fun createPopup(context: Context, view: View, layout: Int, showLogout: Boolean): PopupMenu {

      val popup = PopupMenu(context, view)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        popup.gravity = Gravity.END
      }

      val menuInflater = popup.menuInflater
      menuInflater.inflate(layout, popup.menu)


      if (showLogout) {
        for (item in popup.menu.children) {
          if (item.itemId == R.id.loginMenuItem) {
            item.isVisible = false
          }
        }
      } else {
        for (item in popup.menu.children) {
          if (item.itemId == R.id.logoutMenuItem) {
            item.isVisible = false
          }
        }
      }

      return popup
    }
  }
}