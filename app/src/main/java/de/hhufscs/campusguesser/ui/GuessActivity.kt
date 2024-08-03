package de.hhufscs.campusguesser.ui

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View.INVISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.shashank.sony.fancytoastlib.FancyToast
import com.skydoves.progressview.ProgressViewAnimation
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.GuessResult
import de.hhufscs.campusguesser.core.Level
import de.hhufscs.campusguesser.databinding.ActivityGuessBinding
import de.hhufscs.campusguesser.services.factories.LocalLevelFactory
import de.hhufscs.campusguesser.services.factories.OnlineLevelFactory
import de.hhufscs.campusguesser.ui.menu.MenuActivity
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polygon
import java.util.LinkedList
import kotlin.math.abs


val GEOPOINT_HHU = GeoPoint(51.18885, 6.79551)

class GuessActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private lateinit var binding: ActivityGuessBinding
    private lateinit var iconOverlay: ItemizedIconOverlay<OverlayItem>
    private lateinit var level: Level
    private var guessMarker: OverlayItem? = null
    private var online = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Log.i("CampusGuesser", "Started GuessActivity")

        // OSM will das
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        binding = ActivityGuessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpOSMMap()
        setupIconOverlay()
        setupMapGuessItemListener()
        setUpGuessButtons()
        val seconds = 10
        Thread {
            for (i in 1 until seconds + 1) {
                binding.progress.apply {
                    post {
                        labelText = "${seconds - i}s"
                        progressAnimation = ProgressViewAnimation.BOUNCE
                        progress = 100F / seconds * (i)
                        // progressAnimate()
                    }
                    Thread.sleep(1000)
                }
            }
        }.start()


        this.online = intent.getBooleanExtra("online", false)
        if (!online) {
            val localLevelFactory = LocalLevelFactory(baseContext)
            level = localLevelFactory.getLevelWithNLocalGuesses(10)
            firstGuess()
        } else {
            val onlineLevelFactory = OnlineLevelFactory()
            onlineLevelFactory.getLevelWithNOnlineGuesses(10) {
                level = it
                firstGuess()
            }
        }
    }

    private fun firstGuess() {
        if (!level.isANewGuessLeft()) {
            Log.d("Campusguesser", "leider keine Daten vorhanden du Opfer")
            FancyToast.makeText(baseContext, "keine Bilder verfügbar du Klon", Toast.LENGTH_LONG)
                .show()
            finish()
            return
        }
        nextGuess()
    }

    private fun setUpGuessButtons() {
        binding.btnGuess.setOnClickListener { v ->

            if (!userMadeGuess()) {
                FancyToast.makeText(
                    this,
                    "Make a guess first",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    false
                ).show()
                return@setOnClickListener
            }

            lockGuess()
        }

        binding.btnGuess2.setOnClickListener {

            if (!level.isANewGuessLeft()) {
                showEndScreen()
                return@setOnClickListener
            }

            binding.guessedPopup.transitionToStart()
            nextGuess()
            resetOverlays()
            resetMapFocus()
            setupMapGuessItemListener()
        }
    }

    private fun nextGuess() {
        val currentGuess = level.getCurrentGuess()


        currentGuess.getPicture {
            binding.guessImage.setImageDrawable(
                BitmapDrawable(it)
            )

        }

    }

    private fun lockGuess() {

        if (!userMadeGuess()) throw IllegalStateException("No Guess provided!")

        disableMapGestureDetector()
        binding.playerBackgroundView.visibility = INVISIBLE

        val currentGuess = level.getCurrentGuess()
        val guessLocation = guessMarker!!.point
        level.guess(guessLocation) { it ->

            updateUIPointsToReflectGuessResult(it)
            currentGuess.getLocation {
                drawLinePolygonOnMap(it, guessLocation)

                currentGuess.getPicture() { image ->
                    val imageDrawable = BitmapDrawable(image)
                    imageDrawable.setTargetDensity(10)
                    addIconToMapAtLocationWithDrawable(it, imageDrawable.mutate())
                }
                refreshMap()
                setMapInteractionEnabled(false)
            }

        }
    }

    private fun showEndScreen() {


        binding.btnGuess.post {
            binding.btnGuess.setOnClickListener {
                startActivity(Intent(this, MenuActivity::class.java))
            }
        }


    }

    private fun updateUIPointsToReflectGuessResult(guessResult: GuessResult) {

        binding.pointsReached.text = resources.getString(R.string.points_reached,1, guessResult.points)

        updateCumulativeScorePoints()
        showGuessResultInfoCard()
        updateCumulativeScorePoints()

        val boundingBox = BoundingBox.fromGeoPointsSafe(
            listOf(
                guessResult.guessedSpot as GeoPoint,
                guessResult.actualSpot as GeoPoint
            )
        )

        val longWidth = abs(boundingBox.lonEast - boundingBox.lonWest)
        val latWidth = abs(boundingBox.latNorth - boundingBox.latSouth)
        boundingBox.lonEast += longWidth * .1
        boundingBox.lonWest -= longWidth * .1

        binding.guessMap.zoomToBoundingBox(boundingBox, true)
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
        binding.guessMap.overlays.add(0, Polygon().apply {
            points = LinkedList(listOf(from as GeoPoint, to as GeoPoint))
            strokeColor = resources.getColor(R.color.skyBlue)
            strokeWidth = 10F
        })
    }

    private fun userMadeGuess() = guessMarker != null

    private fun showGuessResultInfoCard() {
        binding.guessedPopup.transitionToEnd()
    }

    private fun setupMapGuessItemListener() {
        binding.guessMap.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
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
        binding.guessMap.invalidate()
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

        binding.guessMap.overlays.add(iconOverlay)
    }

    private fun setGuessMarkerTo(newLocation: GeoPoint) {
        removeOldGuessMarker()
        addGuessMarkerTo(newLocation)
    }

    private fun addGuessMarkerTo(newLocation: GeoPoint) {
        guessMarker = OverlayItem("The Spot!", "", newLocation)

        val drawable =
            AppCompatResources.getDrawable(applicationContext, R.drawable.location_marker_24)

        guessMarker!!.setMarker(drawable)
        iconOverlay.addItem(guessMarker)
    }

    private fun removeOldGuessMarker() {
        if (userMadeGuess()) {
            iconOverlay.removeItem(guessMarker)
        }
    }

    private fun setUpOSMMap() {

        binding.guessMap.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }

        resetMapFocus()
    }

    private fun resetMapFocus() {
        binding.guessMap.controller.apply {
            setZoom(18.0)
            setCenter(GEOPOINT_HHU)
        }
    }

    private fun setMapInteractionEnabled(newState: Boolean) {
        binding.guessMap.apply {
            setMultiTouchControls(newState)
            isUserInteractionEnabled = false
            isFlingEnabled = newState
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
        binding.guessMap.overlays.removeIf { it is Polygon }
    }

    private fun disableMapGestureDetector() {
        binding.guessMap.overlays.removeIf {
            it is MapEventsOverlay
        }
    }

    private fun updateCumulativeScorePoints() {
        binding.points.text = resources.getString(R.string.points, level.getPoints())
    }


    public override fun onResume() {
        super.onResume()
        binding.guessMap.onResume()
    }

    public override fun onPause() {
        super.onPause()
        binding.guessMap.onPause()
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

