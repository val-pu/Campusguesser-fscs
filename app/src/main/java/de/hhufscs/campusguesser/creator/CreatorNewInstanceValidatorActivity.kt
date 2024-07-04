package de.hhufscs.campusguesser.creator

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.services.AssetService
import de.hhufscs.campusguesser.services.LocationService
import de.hhufscs.campusguesser.services.MapService
import de.hhufscs.campusguesser.services.PermissionService
import de.hhufscs.campusguesser.services.factories.JSONObjectFactory
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import java.io.File
import java.util.UUID

class CreatorNewInstanceValidatorActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    val contract = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->

        // TODO: Was kluges so, falls das schiefläuft

    }
    private lateinit var btnCreate: Button
    private lateinit var locationTextView: TextView
    private var selectedLocation: IGeoPoint? = null
    private var resultBM: Bitmap? = null
    private lateinit var resultImagePath: String
    private lateinit var mapService: MapService
    private lateinit var locationService: LocationService
    private lateinit var permissionService: PermissionService
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator_new_instance_validator)

        image = findViewById(R.id.image)
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )


        locationService = LocationService(this)
        permissionService = PermissionService(this)
        mapService = MapService(findViewById(R.id.map))

        mapService.enableLocationSelector {
            selectedLocation = it
        }


        btnCreate = findViewById(R.id.btn_create)
        locationTextView = findViewById(R.id.location)

        btnCreate.setOnClickListener {

            if (selectedLocation != null) {
                saveCreatedGuess()
                return@setOnClickListener
            }

            if (permissionService.locationAccessGranted()) {
                takePhoto()
            } else {
                permissionService.requestLocationPermissionAndDo(
                    contract
                )
            }
        }

    }

    private fun takePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val fileName = "temp"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            val imageFile = File.createTempFile(fileName, ".png", storageDir)
            resultImagePath = imageFile.absolutePath

            val imageUri = FileProvider.getUriForFile(
                this,
                "de.hhufscs.campusguesser.fileprovider",
                imageFile
            )

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: Exception) {
            Log.e("CampusGuesser", "Error initialising Camera: $e")
        }
    }

    private fun saveCreatedGuess() {
        val fileName = UUID.randomUUID().toString()
        val jsonObject = JSONObjectFactory.coordinates(selectedLocation!!)
        AssetService.saveJSONToStorage(jsonObject, "$fileName.json", applicationContext)
        AssetService.saveBitmapToInternalStorage(
            "$fileName.png",
            resultBM!!,
            applicationContext
        )
        startActivity(Intent(this, CreatorActivity::class.java))
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (imageCaptureResult(requestCode, resultCode)) {
            val imageBitmap = BitmapFactory.decodeFile(resultImagePath)
            resultBM = imageBitmap
            displayImage(imageBitmap)
        }
    }

    private fun imageCaptureResult(requestCode: Int, resultCode: Int) =
        requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK


    @SuppressLint("MissingPermission")
    private fun displayImage(imageBitmap: Bitmap) {
        image.setImageBitmap(imageBitmap)

        locationService.getCurrentLocation {location ->
            if (location != null) {
                mapService.moveLocationSelectorMarkerTo(location)
                displayLocation(location)
            }
        }
    }

    fun displayLocation(location: IGeoPoint) {
        this.selectedLocation = GeoPoint(location)
        locationTextView.text = "AUS GPS: $location"
    }


    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 42
    }




}
