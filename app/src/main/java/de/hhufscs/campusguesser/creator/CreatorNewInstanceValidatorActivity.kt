package de.hhufscs.campusguesser.creator

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.AssetService
import org.json.JSONObject
import java.util.UUID

class CreatorNewInstanceValidatorActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var image: ImageView
    private lateinit var btnCreate: Button
    private lateinit var locationTextView: TextView
    private var location: Location? = null
    private var resultBM: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_creator_new_instance_validator)

        image = findViewById(R.id.image)
        btnCreate = findViewById(R.id.btn_create)
        locationTextView = findViewById(R.id.location)

        btnCreate.setOnClickListener {

            if (location != null) {
                val fileName = UUID.randomUUID().toString()
                val jsonObject = JSONObject()

                jsonObject.put("longitude", location!!.longitude)
                jsonObject.put("latitude", location!!.latitude)
                AssetService.saveJSONToStorage(jsonObject, "$fileName.json", applicationContext)
                AssetService.saveBitmapToInternalStorage("$fileName.png", resultBM!!, applicationContext)
                startActivity(Intent(this, CreatorActivity::class.java))
                return@setOnClickListener
            }

            if (locationAccessPermitted()) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean -> }

    private fun locationAccessPermitted() = ContextCompat.checkSelfPermission(
        baseContext,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (imageCaptureResult(requestCode, resultCode)) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            resultBM = imageBitmap
            displayImage(imageBitmap)
        }
    }

    private fun imageCaptureResult(requestCode: Int, resultCode: Int) =
        requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK


    @SuppressLint("MissingPermission")
    private fun displayImage(imageBitmap: Bitmap) {
        image.setImageBitmap(imageBitmap)

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY).setMaxUpdateAgeMillis(0).build(),
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                displayLocation(location)
            }
        }
    }

    fun displayLocation(location: Location) {
        this.location = location
        locationTextView.text = location.toString()

    }


    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 42
    }

}