package de.hhufscs.campusguesser.creator

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import de.hhufscs.campusguesser.R

class CreatorActivity : AppCompatActivity() {
    private lateinit var btnCreate: Button
    private lateinit var image: ImageView
    private lateinit var locationText: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnCreate = findViewById(R.id.btn_create)
        image = findViewById(R.id.image)
        locationText = findViewById(R.id.location)


        setUpButtons()

    }

    private fun setUpButtons() {
        btnCreate.setOnClickListener { _ ->

            if (locationAccessPermitted()) {
                val cameraIntent = Intent(ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, Companion.REQUEST_IMAGE_CAPTURE)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (imageCaptureResult(requestCode, resultCode)) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            displayImage(imageBitmap)
        }
    }

    @SuppressLint("MissingPermission")
    private fun displayImage(imageBitmap: Bitmap) {
        image.setImageBitmap(imageBitmap)

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY).setMaxUpdateAgeMillis(0).build(),
            cancellationTokenSource.token
        )
            .addOnSuccessListener { location: Location? ->
                locationText.text = location?.accuracy.toString()
            }
    }

    private fun imageCaptureResult(requestCode: Int, resultCode: Int) =
        requestCode == Companion.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK


    // Permissions


    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean -> }

    private fun locationAccessPermitted() = ContextCompat.checkSelfPermission(
        baseContext,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 42
    }


}