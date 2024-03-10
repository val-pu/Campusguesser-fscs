package de.hhufscs.campusguesser.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.Level
import de.hhufscs.campusguesser.core.LevelService
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polygon
import java.lang.IllegalStateException
import java.util.LinkedList


class GuessActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private val GEOPOINT_HHU = GeoPoint(51.18885, 6.79551)

    private lateinit var map: MapView
    private lateinit var iconOverlay: ItemizedIconOverlay<OverlayItem>
    private lateinit var level: Level
    private var guessMarker: OverlayItem? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OSM will das
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        setContentView(R.layout.activity_guess)

        setUpOSMMap()
        setupIconOverlay()
        setupMapGuessItemListener()

        val levelService = LevelService()

        level = levelService.getRandomLevel()


        val guessButton = findViewById<Button>(R.id.btn_guess)
        guessButton.setOnClickListener {
            lockGuess()
        }


    }

    private fun lockGuess() {

        if(guessMarker == null) throw IllegalStateException("No Guess provided!")

        val currentGuess = level.getCurrentGuess()
        val guessLocation = guessMarker!!.point
        level.guess(guessLocation)

        val actualLocation = currentGuess.geoPoint

        map.overlays.add(0, Polygon().apply {
            points = LinkedList(listOf(actualLocation as GeoPoint, guessLocation as GeoPoint))
        })

        val actualMarker = OverlayItem("Actual", "", actualLocation)
        iconOverlay.addItem(actualMarker)

        refreshMap()
    }

    private fun setupMapGuessItemListener() {
        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(tappedLocation: GeoPoint): Boolean {
                setGuessMarkerTo(tappedLocation)
                refreshMap()
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }))
    }

    private fun refreshMap() {
        map.invalidate()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupIconOverlay() {
        val item = OverlayItem(
            "Title",
            "Description",
            GeoPoint(51.18885, 6.79551)
        )

        item.setMarker(resources.getDrawable(R.drawable.baseline_location_on_24))
        val items = ArrayList<OverlayItem>()
        items.add(
            item
        )

        iconOverlay = ItemizedIconOverlay(
            items,
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
        if (guessMarker != null) {
            iconOverlay.removeItem(guessMarker)
        }

        guessMarker = OverlayItem("The Spot!", "", newLocation)
        iconOverlay.addItem(guessMarker);
    }

    private fun setUpOSMMap() {
        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        map = findViewById<View>(R.id.guess_map) as MapView

        map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)

            controller.apply {
                setZoom(19.0)
                setCenter(GEOPOINT_HHU)
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        map.onResume()
    }

    public override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        for (i in grantResults.indices) {
            permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray<String>(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray<String>(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}