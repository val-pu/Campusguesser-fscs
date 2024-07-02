package de.hhufscs.campusguesser.services

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class PermissionService(val activity: Activity) {


    fun locationAccessGranted() = ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    fun requestLocationPermissionAndDo(requestPermissionLauncher: ActivityResultLauncher<String>) {
        requestPermissionLauncher.launch(
            Manifest.permission.ACCESS_FINE_LOCATION
        );
    }
}