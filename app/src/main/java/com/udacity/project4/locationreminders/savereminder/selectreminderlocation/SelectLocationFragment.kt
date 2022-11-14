package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    private  val TAG = "SelectLocationFragment"
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLocation: LatLng = LatLng(33.00, 15.00)
    private var selected_Location_Description: String? = null
    private lateinit var binding: FragmentSelectLocationBinding
    private var mPoi: PointOfInterest? = null
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveCurrentLocation.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        setMapLongClick(map)
        setPoiClick(map)
        enableMyLocation()
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            selectedLocation = latLng
            selected_Location_Description = "random Location"
        }
    }
//when user using POI on the map
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            selectedLocation = poi.latLng
            selected_Location_Description = poiMarker?.title
        }
    }

/*I didn't use map stile in the previous submitted
so i create stile map use JSON object similar at the lesson
and call it on the map ready function
  */
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            // Permissions are granted
            getMyLocation()
            map.setMyLocationEnabled(true)
            Toast.makeText(context, "Location permission is granted.", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            Toast.makeText(requireContext(), R.string.permission_denied_explanation,
                Toast.LENGTH_SHORT).show()

        }
    }

    private fun getMyLocation() {
      //  TODO("Not yet implemented")
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
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
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location.let {
                        if (it != null) {
                            val userLocation = LatLng(it.latitude, it.longitude)
                            val snippet = String.format(
                                Locale.getDefault(),
                                "Lat: %1$.5f, Long: %2$.5f",
                                it.latitude,
                                it.longitude
                            )
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                            map.addMarker(
                                MarkerOptions().position(userLocation).title("Your Location")
                                    .snippet(snippet)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            )
                        } else {
                            getMyLocation()
                        }

                    }

        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    /*
    on save button pressed
    this fun created until to enable user to zoom in when user  open map to choose
    from  map if you need to select from POI or any random area from map

    */
    private fun onLocationSelected() {
        //when User choose from POI
        if ( mPoi!= null || !mPoi?.name.isNullOrEmpty()) {
            // send  the selected location details to view model.
            _viewModel.selectedPOI.value= mPoi
            _viewModel.reminderSelectedLocationStr.value = mPoi!!.name
            _viewModel.latitude.value = mPoi!!.latLng.latitude
            _viewModel.longitude.value = mPoi!!.latLng.longitude
            Log.d("test","selectedPOI = ${_viewModel.selectedPOI.value!!.name}")
            // navigate back to add location fragment screen
            _viewModel.navigationCommand.value = NavigationCommand.Back
            //when User choose from any area from Map
            // send back the selected location details to view model
        }else if(selected_Location_Description != null){
            _viewModel.reminderSelectedLocationStr.value = selected_Location_Description
            _viewModel.latitude.value = this.selectedLocation.latitude
            _viewModel.longitude.value = this.selectedLocation.longitude
            // navigate back to add location fragment screen
            _viewModel.navigationCommand.value = NavigationCommand.Back

        } else {
            //display toast to user when not select anyLocation
            Toast.makeText(requireContext(), getString(R.string.select_poi), Toast.LENGTH_LONG)
                .show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    enableMyLocation()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    enableMyLocation()
                }

                else -> {
                    Log.i("Permission: ", "Denied")
                    Toast.makeText(
                        context,"Location permission was not granted.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}