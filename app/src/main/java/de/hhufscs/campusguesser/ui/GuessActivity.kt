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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.jsibbold.zoomage.ZoomageView
import com.shashank.sony.fancytoastlib.FancyToast
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
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polygon
import java.util.LinkedList


val GEOPOINT_HHU = GeoPoint(51.18885, 6.79551)

class GuessActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private lateinit var binding: ActivityGuessBinding
    private lateinit var iconOverlay: ItemizedIconOverlay<OverlayItem>
    private lateinit var level: Level
    private var guessMarker: OverlayItem? = null
    private var currentlyGuessing = true
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
        setUpGuessButton()

        this.online = intent.getBooleanExtra("online", false)
        if(!online) {
            val localLevelFactory = LocalLevelFactory(baseContext)
            level = localLevelFactory.getLevelWithNLocalGuesses(10)
            firstGuess()
        } else {
            val onlineLevelFactory = OnlineLevelFactory()
            onlineLevelFactory.getLevelWithNOnlineGuesses(10){
                level = it
                firstGuess()
            }
        }
    }

    private fun firstGuess(){
        if(!level.isANewGuessLeft()){
            Log.d("Campusguesser", "leider keine Daten vorhanden du Opfer")
            FancyToast.makeText(baseContext, "keine Bilder verfügbar du Klon", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        nextGuess()
    }

    @SuppressLint("ClickableViewAccessibility")
     private fun setUpGuessButton() {
        binding.btnGuess.setOnTouchListener { v, event ->

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
                binding.btnGuess.setText(R.string.next_guess)
            } else {
                resetOverlays()
                resetMapFocus()
                binding.btnGuess.setText(R.string.guess)
                setupMapGuessItemListener()
                nextGuess()
            }

            currentlyGuessing = !currentlyGuessing

            true
        }
    }

    private fun nextGuess() {
        val currentGuess = level.getCurrentGuess()


        hideAddedPointsText()


        currentGuess.getPicture {
            binding.guessImage.setImageDrawable(
                BitmapDrawable(it)
            )
        }

    }

    private fun lockGuess() {

        if (!guessPresent()) throw IllegalStateException("No Guess provided!")

        disableMapGestureDetector()

        val currentGuess = level.getCurrentGuess()
        val guessLocation = guessMarker!!.point
        level.guess(guessLocation) {

            updateUIPointsToReflectGuessResult(it)
            currentGuess.getLocation {
                drawLinePolygonOnMap(it, guessLocation)

                currentGuess.getPicture() { image ->
                    val imageDrawable = BitmapDrawable(image)
                    imageDrawable.setTargetDensity(10)
                    addIconToMapAtLocationWithDrawable(it, imageDrawable.mutate())
                }


                refreshMap()
            }
        }
    }

    private fun showEndScreen() {
        // val guessedGuesses = level.guessedGuesses

// TODO       guessedGuesses.forEach {
//
//            addGuessMarkerTo(GeoPoint(it.guessedSpot))
//
//            val originalGuess = it.guess
//
//            guessRepository.getPictureForGuess(originalGuess) { bitmap ->
//
//                val image = bitmap!!.toDrawable(resources)
//
//                image.setTargetDensity(10)
//                addIconToMapAtLocationWithDrawable(it.guess.geoPoint, image)
//                drawLinePolygonOnMap(it.guessedSpot, originalGuess.geoPoint)
//
//            }
//        }


        binding.btnGuess.setText(R.string.weiter)


        binding.guessImage.visibility = INVISIBLE

        binding.btnGuess.post {
            binding.btnGuess.setOnClickListener {
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
        binding.guessMap.overlays.add(0, Polygon().apply {
            points = LinkedList(listOf(from as GeoPoint, to as GeoPoint))
        })
    }

    private fun guessPresent() = guessMarker != null

    private fun showAddedPointsText() {
        // TODO
    }

    private fun hideAddedPointsText() {
        // TODO
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
        if (guessPresent()) {
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
        // TODO
    }

    private fun setAddedPointsTextFromGuessResult(guessResult: GuessResult) {
        // TODO
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

