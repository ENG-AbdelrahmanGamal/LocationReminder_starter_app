package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val REQUEST_CODE_BACKGROUND = 102929
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 12433
class SelectLocationFragment : BaseFragment() ,OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private val TAG = "SelectLocationFragment"
    private val REQUEST_LOCATION_PERMISSION = 1

    var Point : PointOfInterest? =null
    var latitude : Double = 0.0
    var longitude : Double = 0.0
    var title = ""
    var isLocationSelected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location

        binding.saveCurrentLocation.setOnClickListener{
            if(Point!= null) {
                onLocationSelected()
                Toast.makeText(context,"location was selected!", Toast.LENGTH_LONG).show()

            }else{
                Toast.makeText(context,"please Select a location  first!", Toast.LENGTH_LONG).show()
            }
        }
      //  onLocationSelected()

        return binding.root
    }



    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setPoiClick(map)
        enableMyLocatin()
        // marker to location when user select
        setPoiClick(map)
        // create a style to map
        mapStyle(map)

        setMapLongClick(map)


    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.latitude.value = latitude
        _viewModel.longitude.value = longitude
        _viewModel.selectedPOI.value = Point
        _viewModel.reminderSelectedLocationStr.value = title
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }

        private fun mapStyle(map: GoogleMap) {
        try {
            val success =
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.map_style))
            if (!success)
                Log.d(TAG, "Done")
        } catch (e: Resources.NotFoundException) {
            Log.d(TAG, " error is  ", e)

        }
    }
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        else -> super.onOptionsItemSelected(item)

    }
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            val zoomLevel = 15f
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, zoomLevel))
            Point = poi
            latitude= poi.latLng.latitude
            longitude = poi.latLng.longitude
            title = poi.name
        }
        isLocationSelected=true
    }
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnPoiClickListener {it->
            val snippet = String.format(
                Locale.getDefault(), "${it.latLng.latitude},${it.latLng.longitude}",
                it.latLng.latitude, it.latLng.longitude
            )
            map.addMarker(MarkerOptions().position(it.latLng).title(it.name)
                .snippet(snippet))
            val pointMarker =map.addMarker(
                MarkerOptions().position(it.latLng).title(it.name)
            )
          pointMarker.showInfoWindow()

            val zoomLevel = 15f
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(it.latLng, zoomLevel))
            Point = it
            latitude= it.latLng.latitude
            longitude = it.latLng.longitude
            title = it.name

        }
    }
private fun enableMyLocatin() {
    if (isPermissionGranted())
    {
        map.isMyLocationEnabled = true

        if (ActivityCompat.checkSelfPermission(requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(requireActivity(),Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        map.setMyLocationEnabled(true)
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                checkDeviceLocationSettings()
            } else {
                requestQPermission()
            }

        }
    } else {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }
}
    @TargetApi(Build.VERSION_CODES.Q)
    private fun requestQPermission() {
        val hasForegroundPermission = ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasForegroundPermission) {
            val hasBackgroundPermission = ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasBackgroundPermission) {
                checkDeviceLocationSettings()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_CODE_BACKGROUND
                )
            }
        }
    }
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val requestBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity()!!)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(requestBuilder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        requireActivity()!!, REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(requireView(), R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }

        }

}

}
