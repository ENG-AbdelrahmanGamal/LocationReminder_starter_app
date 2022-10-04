package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceHelper
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private  val TAG = "SaveReminderFragment"
 //  private  var geofenceHelper: GeofenceHelper by lazy { }
    private lateinit var geofenceHelper: GeofenceHelper
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    private lateinit var generalLocation :LatLng

    private val  GEOFENCE_RADIUS_IN_METERS = 100f
    private val  NEVER_EXPIRES = Geofence.NEVER_EXPIRE
    private lateinit var geofencingClient: GeofencingClient
    private val GEOFENCE_RADIUS = 500f
    private lateinit var reminderData : ReminderDataItem
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
//        binding.saveReminder.setOnClickListener {
//            val title = _viewModel.reminderTitle.value
//            val description = _viewModel.reminderDescription.value
//            val location = _viewModel.reminderSelectedLocationStr.value
//            val latitude = _viewModel.latitude.value
//            val longitude = _viewModel.longitude.value
//            val geofenceId = UUID.randomUUID().toString()
//            reminderData = ReminderDataItem(
//                _viewModel.reminderTitle.value,
//                _viewModel.reminderDescription.value,
//                _viewModel.reminderSelectedLocationStr.value,
//                latitude,
//                longitude
//            )
//
//            if (!_viewModel.validateEnteredData(reminderData)) {
//                return@setOnClickListener
//
//            }
//            if (latitude != null && longitude != null && !TextUtils.isEmpty(title))
//             generalLocation= LatLng(latitude, longitude)
//        //    geofencingClient = LocationServices.getGeofencingClient(requireActivity())
//
//            addGeofence(generalLocation, GEOFENCE_RADIUS, geofenceId)
//
//            _viewModel.validateAndSaveReminder(ReminderDataItem(title,description,location, latitude,longitude))
//
//            _viewModel.navigateList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//                if(it){
//                    view.findNavController().navigate(R.id.action_saveReminderFragment_to_reminderListFragment)
//                    _viewModel.navigateList()
//                }
//            })
//
//        }
        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            generalLocation=LatLng(latitude!!,longitude!!)

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request



            reminderData = ReminderDataItem(
                _viewModel.reminderTitle.value,
                _viewModel.reminderDescription.value,
                _viewModel.reminderSelectedLocationStr.value,
                latitude,
                longitude
            )


            if (!_viewModel.validateEnteredData(reminderData)) {
                return@setOnClickListener

            }
            requestForegroundAndBackgroundLocationPermissions(LatLng(latitude!!,longitude!!),geofencingClient)

        }


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 29) {
            // We don't rely on the result code, but just check the location setting again

            deviceLocationSettingsStartGeofences(false,
                location = generalLocation,
                geofencingClient = geofencingClient
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun deviceLocationSettingsStartGeofences(
        resolve: Boolean = true,
        location: LatLng,
        geofencingClient: GeofencingClient
    ) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        29,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            } else {
                deviceLocationSettingsStartGeofences(
                    location = location,
                    geofencingClient = geofencingClient
                )

                // Explain user why app needs this permission
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofence(
                    LatLng(location.latitude, location.longitude),)
            }
        }
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions(
        location: LatLng,
        geofencingClient: GeofencingClient
    ): Boolean {
        var option = -1
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            deviceLocationSettingsStartGeofences(
                location = location,
                geofencingClient = geofencingClient
            )
            return true
        }

        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE

            }

            else -> {

                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            }

        }
        requestPermissions(
            permissionsArray,
            resultCode
        )
        return false
    }
    @SuppressLint("MissingPermission")
    private fun addGeofence(
        latLng: LatLng,
        radius: GeofencingClient,
        geofenceId: String) {
//        val geofence: Geofence = geofenceHelper.getGeofence(geofenceId, latLng, radius,
//            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
//        )
//        val geofencingRequest: GeofencingRequest = geofenceHelper.getGeofencingRequest(geofence)
 //      val pendingIntent: PendingIntent? = geofenceHelper.getGeofencePendingIntent()
        val geofence = Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER )

            .build()
         val geofenceIntent: PendingIntent by lazy {
            val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val geofencingRequest=  GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofenceIntent)
            .addOnSuccessListener(OnSuccessListener<Void?> {
                // Toast.makeText(context,"geofence added",Toast.LENGTH_LONG).show()
                Log.d(TAG, "Geofence Added")
            })
            .addOnFailureListener(OnFailureListener { e ->
                val errorMessage: String = geofenceHelper.getErrorString(e)
                Toast.makeText(context, "Please give background location permission", Toast.LENGTH_LONG).show()
                Log.d(TAG, "fail in creating geofence: $errorMessage")
            })
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

}
