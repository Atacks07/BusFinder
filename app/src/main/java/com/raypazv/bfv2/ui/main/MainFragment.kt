package com.raypazv.bfv2.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.location.Location
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.transition.AutoTransition
import android.transition.TransitionManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.raypazv.bfv2.App
import com.raypazv.bfv2.R
import com.raypazv.bfv2.data.DataState
import com.raypazv.bfv2.data.paths.Path
import com.raypazv.bfv2.data.paths.PathAdapter
import com.raypazv.bfv2.data.paths.Point
import com.raypazv.bfv2.data.position.PositionUpdate
import com.raypazv.bfv2.data.users.User
import com.raypazv.bfv2.databinding.FragmentMainBinding
import com.raypazv.bfv2.services.TrackingService
import com.raypazv.bfv2.ui.main.modules.OptionsModule
import com.raypazv.bfv2.ui.main.modules.PathsModule
import com.raypazv.bfv2.util.*
import com.raypazv.bfv2.viewmodels.PathStateEvent
import com.raypazv.bfv2.viewmodels.PathViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.internal.notify
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Math.pow
import java.lang.Math.round
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@AndroidEntryPoint
class MainFragment : Fragment(), PathAdapter.OnPathClicked {

  private lateinit var socket: Socket

  private var map: GoogleMap? = null
  private var markers: ArrayList<Marker> = ArrayList()

  private lateinit var binding: FragmentMainBinding

  private lateinit var optionsMenu: PopupMenu

  private lateinit var fusedLocationClient: FusedLocationProviderClient

  private val pathViewModel by viewModels<PathViewModel>()

  private lateinit var pathAdapter: PathAdapter
  private lateinit var paths: ArrayList<Path>
  private var selectedPathId = -1
  private var selectedPathIndex = -1

  private lateinit var currentDrawnPath: Polyline

  private lateinit var sharedPreferences: SharedPreferences
  private lateinit var preferencesEditor: SharedPreferences.Editor

  private var isInfoWindowVisible = false

  private var isTracking = false

  private var lastUserPosition: LatLng? = null
  private var elapsedTime: Long = 0

  private var time = 7200.0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

    binding.optionsFAB.setOnClickListener { optionsMenu.show() }

    val bottomSheetBehavior = BottomSheetBehavior.from(binding.pathsBottomSheet)

    binding.setPathImageButton.setOnClickListener {
      if (this.isTracking) {
        binding.setPathImageButton.isEnabled = false
      } else {
        if (selectedPathIndex != -1) {
          drawPath(selectedPathIndex)
          bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
      }
    }

    sharedPreferences =
      requireContext().getSharedPreferences("BusFinderPreferences", Context.MODE_PRIVATE)

    binding.shareLocationFAB.setOnClickListener {
      val builder = MaterialAlertDialogBuilder(requireContext())

      if (this.selectedPathId != -1) {
        if (this.isTracking) {
          val dialog = builder.setTitle(R.string.stopLocationSharingDialogTitleText)
            .setMessage(R.string.stopLocationSharingDialogMessageText)
            .setNeutralButton(R.string.cancelText) { _, _, ->}
            .setPositiveButton(R.string.acceptText) { _, _, ->
              this.socket.emit("stop-sharing", this.socket.id())
              sendCommandToService(Constants.ACTION_STOP_SERVICE)
            }.create()

          dialog.show()
        } else {
          val dialog = builder.setTitle(R.string.shareLocationDialogTitleText)
            .setMessage(R.string.shareLocationDialogMessageText)
            .setNeutralButton(R.string.cancelText) { _, _ -> }
            .setPositiveButton(R.string.acceptText) { _, _ ->
              if (true) {
                this.toggleTracking(this.isTracking)
              }
              // TODO: Non bus driver location sharing
//              fusedLocationClient.
            }.create()

          dialog.show()
        }
      } else {
        val dialog = builder.setTitle(R.string.selectPathDialogTitleText)
          .setMessage(R.string.selectPathDialogMessageText)
          .setPositiveButton(R.string.acceptText) { _, _ ->}
          .create()

        dialog.show()
      }
    }

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.socket = (requireActivity().application as App).socket
    this.socket.connect()

    this.requestPermissions()

    optionsMenu =
      OptionsModule.createPopup(
        requireContext(),
        binding.optionsFAB,
        R.menu.options_menu,
        (requireActivity().application as App).user.name!!.isNotEmpty()
      )

    optionsMenu.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.loginMenuItem -> {
          view.findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
        }

        R.id.logoutMenuItem -> {
          logout()
          optionsMenu =
            OptionsModule.createPopup(
              requireContext(),
              binding.optionsFAB,
              R.menu.options_menu,
              (requireActivity().application as App).user.name!!.isNotEmpty()
            )
        }

        R.id.settingsMenuitem -> {
          view.findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }
      }
      true
    }

    binding.mapView.onCreate(savedInstanceState)
    binding.mapView.getMapAsync {
      map = it

      val markerImage =
        BitmapFactory.decodeResource(resources, R.drawable.marker).scale(160, 160, false)

      for (i in 0..10) {
        val marker = map!!.addMarker(
          MarkerOptions().position(LatLng(28.406826, -106.865818))
            .icon(BitmapDescriptorFactory.fromBitmap(markerImage))
        )!!
        marker.isVisible = false

        this.markers.add(marker)
      }

      map!!.setOnMarkerClickListener {
        this.toggleInfo()
        true
      }

      map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(28.707406030863893, -106.106759560585128), 15.0f))
      map!!.setPadding(50, 50, 50, 100)

      socket.on("update-position", onPositionUpdate)
      socket.on("remove-from-list", onRemoveFromList)
    }
    this.subscribeToObservers()
    pathViewModel.setStateEvent(PathStateEvent.GetAllPathsEvent, null)

    pathAdapter = PathAdapter(ArrayList(), this)

    binding.pathsRecyclerView.layoutManager = LinearLayoutManager(context)
    binding.pathsRecyclerView.adapter = pathAdapter

    if ((requireActivity().application as App).user.name!!.isEmpty()) {
      binding.shareLocationFAB.visibility = View.INVISIBLE
    }

    /*this.selectedPathId = sharedPreferences.getInt("selectedPath", -1)

    if (this.selectedPathId != -1) {
      binding.currentPathTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
      binding.currentPathTextView.text = sharedPreferences.getString("selectedPathName", "")
      binding.infoRouteNameTextView.text = sharedPreferences.getString("selectedPathName", "")

      if (this::paths.isInitialized) {
        for ((i, path) in paths.withIndex()) {
          if (path.id == this.selectedPathId) {
            this.selectedPathIndex = i - 1
          }
        }
        drawPath(this.selectedPathIndex)
      }
    }*/
  }

  private fun subscribeToObservers() {
    pathViewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
      when (dataState) {
        is DataState.Success<Any> -> {
          when (dataState.data) {
            is List<*> -> {
              val pathList = ArrayList(dataState.data as ArrayList<Path>)
              println("path list aaaa")
              println(pathList)
              pathAdapter.submitList(pathList)
              pathAdapter.notifyDataSetChanged()
              this.paths = pathList
            }
          }
        }

        is DataState.Loading -> {
          println("Loading paths")
        }

        is DataState.Error -> {
          println("Error: ${dataState.exception::class.simpleName}")
          println(dataState.exception.message)
        }
      }
    })

    TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
      updateTracking(it)
    })

    TrackingService.currentLocation.observe(viewLifecycleOwner, Observer {
      val newPosition = LatLng(it.latitude, it.longitude)

      val positionUpdate = PositionUpdate(it.latitude, it.longitude, this.selectedPathId, this.socket.id(), (requireActivity().application as App).user.isDriver!!, null)
      println(Gson().toJson(positionUpdate))
      socket.emit("update-position", Gson().toJson(positionUpdate))

    })
  }

  override fun onPause() {
    super.onPause()
    binding.mapView.onPause()
  }

  override fun onResume() {
    super.onResume()
    binding.mapView.onResume()
  }

  override fun onStart() {
    super.onStart()
    binding.mapView.onStart()
  }

  override fun onStop() {
    super.onStop()
    binding.mapView.onStop()
  }

  override fun onDestroy() {
    super.onDestroy()
    binding.mapView.onDestroy()
  }

  override fun onLowMemory() {
    super.onLowMemory()
    binding.mapView.onLowMemory()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    binding.mapView.onSaveInstanceState(outState)
  }

  private fun requestPermissions() {
    if (TrackingUtility.hasLocationPermissions(requireContext())) {
      return
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      EasyPermissions.requestPermissions(
        this,
        getString(R.string.permissionRationaleText),
        Constants.REQUEST_CODE_LOCATION_PERMISSIONS,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
      )
    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
      EasyPermissions.requestPermissions(
        this,
        getString(R.string.permissionRationaleText),
        Constants.REQUEST_CODE_LOCATION_PERMISSIONS,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
      )
    } else {
      EasyPermissions.requestPermissions(
        this,
        getString(R.string.permissionRationaleText),
        Constants.REQUEST_CODE_LOCATION_PERMISSIONS,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE
      )

      EasyPermissions.requestPermissions(
        this,
        getString(R.string.permissionRationaleText),
        Constants.REQUEST_CODE_LOCATION_PERMISSIONS,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
      )
    }
    return
  }

  override fun onPathClicked(idPath: Int, pathIndex: Int) {
    setPath(idPath, pathIndex)
  }

  private fun setPath(idPath: Int, pathIndex: Int) {

    this.selectedPathId = idPath
    this.selectedPathIndex = pathIndex
    binding.currentPathTextView.text = this.paths[pathIndex].name
    binding.infoRouteNameTextView.text = this.paths[pathIndex].name
    binding.currentPathTextView.setTextColor(
      ContextCompat.getColor(
        requireContext(),
        R.color.accent
      )
    )
  }

  private fun drawPath(pathIndex: Int) {
    println("Path index $pathIndex")
    if (this::paths.isInitialized) {
      val positions = PathsModule.createPathPositions(this.paths[pathIndex].points!!)

      binding.mapView.getMapAsync {
        if (this::currentDrawnPath.isInitialized) {
          this.currentDrawnPath.remove()
        }

        currentDrawnPath = map!!.addPolyline(
          PolylineOptions().clickable(false)
            .color(ContextCompat.getColor(requireContext(), R.color.accent)).width(20f)
            .addAll(positions)
        )
      }
    }
  }

  private fun logout() {
    val builder = MaterialAlertDialogBuilder(requireContext())

    val dialog = builder.setTitle(R.string.logoutDialogTitleText)
      .setMessage(R.string.logoutDialogMessageText)
      .setNeutralButton(R.string.cancelText) { _, _ ->
        println("Cancelled")
      }
      .setPositiveButton(R.string.acceptText) { _, _ ->
        println("Accepted")

        (requireActivity().application as App).user = User(-1, "", "", false, arrayOf())
        binding.shareLocationFAB.visibility = View.INVISIBLE
//        createPopup(binding.optionsButton)

        preferencesEditor = sharedPreferences.edit()
        preferencesEditor.remove("userId")
        preferencesEditor.remove("userName")
        preferencesEditor.remove("userEmail")
        preferencesEditor.remove("userDriver")
        preferencesEditor.remove("userPaths")

        preferencesEditor.apply()
      }
      .create()

    dialog.show()
  }

  private fun toggleTracking(isTracking: Boolean) {
    if (isTracking) {
      sendCommandToService(Constants.ACTION_PAUSE_SERVICE)
    } else {
      sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
    }
  }

  private fun updateTracking(isTracking: Boolean) {
    this.isTracking = isTracking

    if (!isTracking) {
      binding.shareLocationFAB.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_location))
    } else {
      binding.shareLocationFAB.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause))
    }
  }

  private fun toggleInfo() {
    val transition = AutoTransition()
    transition.duration = 150

    if (this.isInfoWindowVisible) {
      TransitionManager.beginDelayedTransition(binding.moreInfoCardView, transition)
      binding.moreInfoCardView.visibility = View.GONE
    } else {
      TransitionManager.beginDelayedTransition(binding.moreInfoCardView, transition)
      binding.moreInfoCardView.visibility = View.VISIBLE
    }

    this.isInfoWindowVisible = !this.isInfoWindowVisible
  }

  private fun hideInfo() {
    val transition = AutoTransition()
    transition.duration = 150

    TransitionManager.beginDelayedTransition(binding.moreInfoCardView, transition)
    binding.moreInfoCardView.visibility = View.GONE

    this.isInfoWindowVisible = false
  }

  private fun sendCommandToService(action: String) = Intent(requireContext(), TrackingService::class.java).also {
    it.action = action
    requireContext().startService(it)
  }

  private val onPositionUpdate = Emitter.Listener { args ->
    val positionUpdate = Gson().fromJson(args[0].toString(), PositionUpdate::class.java) as PositionUpdate

    if (positionUpdate.idPath == this.selectedPathId) {

      if (this.isInfoWindowVisible) {
        // I need the user's location and the positionupdate location in separate variables
        val busPosition = LatLng(positionUpdate.latitude, positionUpdate.longitude)
//        val busPosition = LatLng(28.414667, -106.871055)

        if (this.lastUserPosition == null) {
          elapsedTime = SystemClock.elapsedRealtime()
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
          if (location != null) {
            lastUserPosition = LatLng(location.latitude, location.longitude)
//            lastUserPosition = LatLng(28.414065, -106.884822)
            val distance = calculateDistance(lastUserPosition!!, busPosition)

            time = distance / 2.5
          }
        }
      }

      requireActivity().runOnUiThread {
        val newPosition = LatLng(positionUpdate.latitude, positionUpdate.longitude)
        this.markers[0].isVisible = true
        MarkerAnimation.animateMarker(this.markers[0], newPosition, LatLngInterpolator.LinearFixed())

        if (this.isInfoWindowVisible) {
          if (time >= 7200) {
            binding.infoRouteTimeTextView.text = ""
          } else {
            val timeText = "${(time / 60).roundToInt()} min"
            binding.infoRouteTimeTextView.text = timeText
          }
        }
      }
    }
  }

  private val onRemoveFromList = Emitter.Listener { args ->
    println("Remove from list $args")
    requireActivity().runOnUiThread {

      for (i in 0..10) {
        this.markers[i].isVisible = false
        this.markers[i].position = LatLng(28.406826, -106.865818)
      }

      this.hideInfo()
    }
  }

  private fun calculateDistance(userLocation: LatLng, busLocation: LatLng): Double {
    println(userLocation.toString())

    val points = this.paths[this.selectedPathIndex].points!!
    val nearestPointToUserIndex = findNearestPoint(points, Point(userLocation.latitude, userLocation.longitude))
    println(nearestPointToUserIndex)

    val nearestPointToBusIndex = findNearestPoint(points, Point(busLocation.latitude, busLocation.longitude))
    println(nearestPointToBusIndex)

    if (nearestPointToUserIndex > nearestPointToBusIndex) {
      var totalDistance = 0.0

      for (i in nearestPointToBusIndex until nearestPointToUserIndex) {
        val point1 = points[i]
        val point2 = points[i + 1]

        totalDistance += Distance.calculateDistance(point1.latitude!!, point1.longitude!!, point2.latitude!!, point2.longitude!!)
        println(totalDistance)
        println(i)
      }

      return totalDistance
    } else {
      return 0.0
    }
  }

  private fun findNearestPoint(points: Array<Point>, targetPoint: Point): Int {

    var smallestDistanceIndex = -1
    var smallestDistance: Double = Double.POSITIVE_INFINITY
    for (i in points.indices) {
      val point = points[i]
      val xComponent = (targetPoint.latitude!! - point.latitude!!).pow(2.0)
      val yComponent = (targetPoint.longitude!! - point.longitude!!).pow(2.0)
      val distance = sqrt(xComponent + yComponent)

      if (distance < smallestDistance) {
        smallestDistance = distance
        smallestDistanceIndex = i
      }
    }

    return smallestDistanceIndex
  }
}
