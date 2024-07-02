package de.hhufscs.campusguesser.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.jsibbold.zoomage.ZoomageView
import com.shashank.sony.fancytoastlib.FancyToast
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.services.AssetService
import de.hhufscs.campusguesser.core.GuessResult
import de.hhufscs.campusguesser.core.Level
import de.hhufscs.campusguesser.core.LevelService
import de.hhufscs.campusguesser.ui.menu.MenuActivity
import org.osmdroid.api.IGeoPoint
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


val GEOPOINT_HHU = GeoPoint(51.18885, 6.79551)

class GuessActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private lateinit var map: MapView
    private lateinit var guessButton: TextView
    private lateinit var scoreView: TextView
    private lateinit var pointsAddedView: TextView

    private lateinit var guessImage: ZoomageView
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

        guessImage = findViewById(R.id.guess_image)
        scoreView = findViewById(R.id.score)
        pointsAddedView = findViewById(R.id.addedPoints)


        setUpOSMMap()
        setupIconOverlay()
        setupMapGuessItemListener()
        setUpGuessButton()

        val levelService = LevelService()

        level = levelService.getLevelOfAllCustomPictures(this)

        nextGuess()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpGuessButton() {
        guessButton = findViewById(R.id.btn_guess)
        guessButton.setOnTouchListener { v, event ->

            if (!guessPresent()) {
                FancyToast.makeText(
                    this,
                    "Make a guess first",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    false
                ).show()
                return@setOnTouchListener false
            }

            if(!level.isANewGuessLeft()) {
                showEndScreen()

                return@setOnTouchListener false
            }



            if (currentlyGuessing) {
                lockGuess()
                guessButton.setText(R.string.next_guess)
            } else {
                resetOverlays()
                resetMapFocus()
                guessButton.setText(R.string.guess)
                setupMapGuessItemListener()
                nextGuess()
            }

            currentlyGuessing = !currentlyGuessing

            false
        }
    }

    private fun nextGuess() {
        val currentGuess = level.getCurrentGuess()


        hideAddedPointsText()

        guessImage.setImageDrawable(
            BitmapDrawable(
                if (!level.custom)
                    AssetService.getBitmapFromAssets(
                        currentGuess.guessImage.rawPath,
                        this
                    )
                else
                    AssetService.loadBitmapFromStorage(currentGuess.guessImage.rawPath, this)
            )
        )
    }

    private fun lockGuess() {

        if (!guessPresent()) throw IllegalStateException("No Guess provided!")

        disableMapGestureDetector()

        val currentGuess = level.getCurrentGuess()
        val guessLocation = guessMarker!!.point
        val guessResult = level.guess(guessLocation)

        updateUIPointsToReflectGuessResult(guessResult)

        val actualLocation = currentGuess.geoPoint

        drawLinePolygonOnMap(actualLocation, guessLocation)
        val image =
            BitmapDrawable(AssetService.loadBitmapFromStorage(currentGuess.guessImage.rawPath, this))

        image.setTargetDensity(10)
        addIconToMapAtLocationWithDrawable(actualLocation, image.mutate())

        refreshMap()
    }

    private fun showEndScreen() {
        val guessedGuesses = level.guessedGuesses

        guessedGuesses.forEach {

            addGuessMarkerTo(GeoPoint(it.guessedSpot))

            val originalGuessDTO = it.guess

            val image = BitmapDrawable(AssetService.loadBitmapFromStorage(originalGuessDTO.guessImage.rawPath, this))

            image.setTargetDensity(10)
            addIconToMapAtLocationWithDrawable(it.guess.geoPoint, image)

            drawLinePolygonOnMap(it.guessedSpot, originalGuessDTO.geoPoint)

        }


        guessButton.setText(R.string.weiter)


        findViewById<View>(R.id.end_screen).visibility = VISIBLE
        guessImage.visibility = INVISIBLE

        guessButton.post {
            guessButton.setOnClickListener {
                startActivity(Intent(this, MenuActivity::class.java))
            }
        }


    }

    private fun updateUIPointsToReflectGuessResult(guessResult: GuessResult) {
        updateCumulativeScorePoints()
        showAddedPointsText()
        setAddedPointsTextFromGuessResult(guessResult)
    }


    private fun addIconToMapAtLocationWithDrawable(location: IGeoPoint, drawable: Drawable?) {
        val actualMarker = OverlayItem("Actual", "", location)

        drawable?.also { actualMarker.setMarker(it) }

        iconOverlay.addItem(actualMarker)
    }

    private fun drawLinePolygonOnMap(
        from: IGeoPoint,
        to: IGeoPoint
    ) {
        map.overlays.add(0, Polygon().apply {
            points = LinkedList(listOf(from as GeoPoint, to as GeoPoint))
        })
    }

    private fun guessPresent() = guessMarker != null

    private fun showAddedPointsText() {
        pointsAddedView.apply {
            animate().alpha(1f)
                .setDuration(300).withEndAction {
                    visibility = View.VISIBLE
                }.start()
        }
    }

    private fun hideAddedPointsText() {
        pointsAddedView.animate().alpha(0f).setDuration(300).withEndAction {
            pointsAddedView.visibility = View.INVISIBLE
        }.start()
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
        addGuessMarkerTo(newLocation)
    }

    private fun addGuessMarkerTo(newLocation: GeoPoint) {
        guessMarker = OverlayItem("The Spot!", "", newLocation)

        val drawable =
            AppCompatResources.getDrawable(applicationContext, R.drawable.baseline_location_on_24)

        guessMarker!!.setMarker(drawable)
        iconOverlay.addItem(guessMarker)
    }

    private fun removeOldGuessMarker() {
        if (guessPresent()) {
            iconOverlay.removeItem(guessMarker)
        }
    }

    private fun setUpOSMMap() {

        map = findViewById<View>(R.id.guess_map) as MapView

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

    private fun resetOverlays() {
        removeAllMapIcons()
        removeGuessDistanceOverlayLine()
    }



    private fun removeAllMapIcons() {
        iconOverlay.removeAllItems()
        guessMarker = null
    }

    private fun removeGuessDistanceOverlayLine() {
        map.overlays.removeIf { it is Polygon }
    }

    private fun disableMapGestureDetector() {
        map.overlays.removeIf {
            it is MapEventsOverlay
        }
    }

    private fun updateCumulativeScorePoints() {
        scoreView.text = level.getPoints().toString()
    }

    private fun setAddedPointsTextFromGuessResult(guessResult: GuessResult) {
        pointsAddedView.text = "+%d".format(guessResult.earnedPoints)
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

}