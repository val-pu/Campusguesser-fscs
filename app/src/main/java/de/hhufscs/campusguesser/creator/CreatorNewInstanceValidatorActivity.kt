package de.hhufscs.campusguesser.creator

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.services.AssetService
import de.hhufscs.campusguesser.ui.GEOPOINT_HHU
import org.json.JSONObject
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.OverlayItem
import java.io.File
import java.util.LinkedList
import java.util.UUID

class CreatorNewInstanceValidatorActivity : AppCompatActivity() {
    private lateinit var iconOverlay: ItemizedIconOverlay<OverlayItem>
    private lateinit var guessMarker: OverlayItem
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var image: ImageView
    private lateinit var btnCreate: Button
    private lateinit var locationTextView: TextView
    private var location: GeoPoint? = null
    private var resultBM: Bitmap? = null
    private lateinit var resultImagePath: String
    private lateinit var map: MapView
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_creator_new_instance_validator)

        image = findViewById(R.id.image)
        map = findViewById(R.id.map)

        setUpOSMMap()

        setupIconOverlay()
        setupMapGuessItemListener()
        addGuessMarkerTo(GEOPOINT_HHU)


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

                val fileName = "temp"
                val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                try {
                    val imageFile = File.createTempFile(fileName, ".png", storageDir)
                    resultImagePath = imageFile.absolutePath

                    val imageUri = FileProvider.getUriForFile(this, "de.hhufscs.campusguesser.fileprovider", imageFile)

                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)

                } catch (e: Exception) {
                    Log.e("CampusGuesser", "Error initialising Camera: $e")
                }

            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

    }

    private fun setUpOSMMap() {


        map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
        resetMapFocus()
    }

    private fun resetMapFocus() {
        map.controller.apply {
            setZoom(18.0)
            setCenter(GEOPOINT_HHU)
        }
    }


    private fun setupMapGuessItemListener() {
        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(tappedLocation: GeoPoint): Boolean {
                setGuessMarkerTo(tappedLocation)
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }))
    }

    private fun setupIconOverlay() {

        iconOverlay = ItemizedIconOverlay(
            LinkedList<OverlayItem>(),
            object : OnItemGestureListener<OverlayItem?> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    return false
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    return false
                }
            }, this
        )

        map.overlays.add(iconOverlay)
    }

    private fun setGuessMarkerTo(newLocation: GeoPoint) {
        removeOldGuessMarker()
        location = newLocation
        addGuessMarkerTo(newLocation)
    }

    private fun addGuessMarkerTo(newLocation: GeoPoint) {
        guessMarker = OverlayItem("The Spot!", "", newLocation)

        val drawable =
            AppCompatResources.getDrawable(applicationContext, R.drawable.baseline_location_on_24)

        guessMarker.setMarker(drawable)
        iconOverlay.addItem(guessMarker)
    }

    private fun removeOldGuessMarker() {
        guessMarker.let {
            iconOverlay.removeItem(guessMarker)
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

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY).setMaxUpdateAgeMillis(0).build(),
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                setGuessMarkerTo(GeoPoint(location))
                displayLocation(location)
            }
        }
    }

    fun displayLocation(location: Location) {
        this.location = GeoPoint(location)
        locationTextView.text = "AUS GPS: $location"

    }


    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 42
    }

}