package de.hhufscs.campusguesser.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shashank.sony.fancytoastlib.FancyToast
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.AssetService
import de.hhufscs.campusguesser.core.Level
import de.hhufscs.campusguesser.core.LevelService
import de.hhufscs.campusguesser.creator.CreatorActivity
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
    private lateinit var guessButton: TextView
    private lateinit var scoreView: TextView
    private lateinit var pointsAddedView: TextView


    private lateinit var guessImage: ImageView
    private lateinit var iconOverlay: ItemizedIconOverlay<OverlayItem>
    private lateinit var level: Level
    private var guessMarker: OverlayItem? = null
    private var currentlyGuessing = true

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("CampusGuesser", "Started GuessActivity")

        // OSM will das
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        setContentView(R.layout.activity_guess)
        // TODO: DELEET

        findViewById<Button>(R.id.btn_creator).setOnClickListener {
            startActivity(Intent(applicationContext, CreatorActivity::class.java))
        }



        setUpOSMMap()
        setupIconOverlay()
        setupMapGuessItemListener()
        setUpGuessButton()
        scoreView = findViewById(R.id.score)
        pointsAddedView = findViewById(R.id.addedPoints)

        val levelService = LevelService()

        level = levelService.getRandomLevel()

        nextGuess()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpGuessButton() {
        guessButton = findViewById(R.id.btn_guess)
        guessButton.setOnTouchListener { v, event ->

            if (guessMarker == null) {
                FancyToast.makeText(
                    this,
                    "Make a guess first",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    false
                ).show()
                return@setOnTouchListener false
            }



            if (currentlyGuessing) {
                lockGuess()
                guessButton.setText(R.string.next_guess)
            } else {
                guessButton.setText(R.string.guess)
                resetOverlays()
                setupMapGuessItemListener()
                nextGuess()
            }

            currentlyGuessing = !currentlyGuessing

            false
        }
    }

    private fun nextGuess() {
        val currentGuess = level.getCurrentGuess()

        val assetService = AssetService()

        hideAddedPointsText()

        guessImage = findViewById(R.id.guess_image)

        guessImage.setImageDrawable(
            BitmapDrawable(
                assetService.getBitmapFromAssets(
                    currentGuess.guessImage.rawPath,
                    this
                )
            )
        )
    }

    private fun hideAddedPointsText() {
        pointsAddedView.animate().alpha(0f).setDuration(300).withEndAction {
            pointsAddedView.visibility = View.INVISIBLE
        }.start()
    }

    private fun lockGuess() {


        if (guessMarker == null) throw IllegalStateException("No Guess provided!")

        disableMapGestureDetector()

        val currentGuess = level.getCurrentGuess()
        val guessLocation = guessMarker!!.point
        val guessResult = level.guess(guessLocation)

        scoreView.text = level.getPoints().toString()

        showAddedPointsText()

        pointsAddedView.text = "+%d".format(guessResult.earnedPoints)


        val actualLocation = currentGuess.geoPoint

        map.overlays.add(0, Polygon().apply {
            points = LinkedList(listOf(actualLocation as GeoPoint, guessLocation as GeoPoint))
        })

        val actualMarker = OverlayItem("Actual", "", actualLocation)
        iconOverlay.addItem(actualMarker)
        refreshMap()
    }

    private fun showAddedPointsText() {
        pointsAddedView.visibility = View.VISIBLE
        pointsAddedView.animate().alpha(1f).setDuration(300).start()
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
        if (guessMarker != null) {
            iconOverlay.removeItem(guessMarker)
        }

        guessMarker = OverlayItem("The Spot!", "", newLocation)
        guessMarker!!.setMarker(applicationContext.getDrawable(R.drawable.baseline_location_on_24))
        iconOverlay.addItem(guessMarker);
    }

    private fun setUpOSMMap() {
//        requestPermissionsIfNecessary(
//            arrayOf(
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//        )
        map = findViewById<View>(R.id.guess_map) as MapView

        map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
        resetMapFocus()
    }

    private fun resetMap() {
        resetMapFocus()
        resetOverlays()
    }

    private fun resetOverlays() {
        iconOverlay.removeAllItems()
        guessMarker = null
        map.overlays.removeIf { it is Polygon }
    }

    private fun disableMapGestureDetector() {
        map.overlays.removeIf {
            it is MapEventsOverlay
        }
    }

    private fun resetMapFocus() {
        map.controller.apply {
            setZoom(19.0)
            setCenter(GEOPOINT_HHU)
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