package de.hhufscs.campusguesser.services

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint

class LocationService(val activity: Activity) {
    private val context = activity.applicationContext
    private val locationClient = LocationServices.getFusedLocationProviderClient(activity)
    private val permissionService = PermissionService(activity)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onSuccess: (IGeoPoint?) -> Unit) {

        if (!permissionService.locationAccessGranted()) {
            // Sollte nie passieren xd
            assert(false)
        }

        val cancellationTokenSource = CancellationTokenSource()

        locationClient.getCurrentLocation(

            CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(0)
                .build(),

            cancellationTokenSource.token

        ).addOnSuccessListener { location: Location? ->
            onSuccess(
                if (location == null) null
                else GeoPoint(location)
            )
        }
    }

}