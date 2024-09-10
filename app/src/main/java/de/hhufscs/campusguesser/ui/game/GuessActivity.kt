package de.hhufscs.campusguesser.ui.game

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toDrawable
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.GuessResult
import de.hhufscs.campusguesser.core.Level
import de.hhufscs.campusguesser.databinding.ActivityGuessBinding
import de.hhufscs.campusguesser.services.factories.LocalLevelFactory
import de.hhufscs.campusguesser.services.factories.OnlineLevelFactory
import de.hhufscs.campusguesser.ui.game.endscreen.EndScreenActivity
import de.hhufscs.campusguesser.ui.game.endscreen.GsonFactory
import de.hhufscs.campusguesser.ui.game.util.PausableTimedTask
import de.hhufscs.campusguesser.ui.util.AnimatedLoadingPopUp
import de.hhufscs.campusguesser.ui.util.AnimatedPopup
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
import kotlin.math.min
import kotlin.math.roundToInt


val GEOPOINT_HHU = GeoPoint(51.18885, 6.79551)

class GuessActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private val PROGESS_TIME_MILLIS = 10_000L

    private lateinit var binding: ActivityGuessBinding
    private lateinit var level: Level

    private lateinit var iconOverlay: ItemizedIconOverlay<OverlayItem>
    private var guessMarker: OverlayItem? = null
        set(value) {
            field = value
            binding.btnLockGuess.setPrimaryColor(
                if (value == null) R.color.button_accent
                else R.color.very_successful_green
            )
            //binding.btnLockGuess.lockPress = value == null
            binding.btnLockGuess.setText(
                if (value == null) R.string.make_a_guess
                else R.string.lock_guess
            )
        }

    private lateinit var progressBarTask: PausableTimedTask

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
        enableMapPointGestureDetector()
        setUpGuessButtons()
        initProgressBar()

        this.online = intent.getBooleanExtra("online", false)
        if (!online) {
            val localLevelFactory = LocalLevelFactory(baseContext)
            level = localLevelFactory.getLevelWithNLocalGuesses(10)
            nextGuess()
        } else {
            val onlineLevelFactory = OnlineLevelFactory()
            onlineLevelFactory.getLevelWithNOnlineGuesses(10) {
                level = it
                nextGuess()
            }
        }
        AnimatedLoadingPopUp(binding.root)
    }

    private fun initProgressBar() {

        binding.progress.max = PROGESS_TIME_MILLIS.toFloat()

        progressBarTask = object : PausableTimedTask(1000, PROGESS_TIME_MILLIS) {
            override fun onFinish() {
                runOnUiThread {
                    binding.progress.progress = PROGESS_TIME_MILLIS.toFloat()
                    binding.progress.labelText = "0s"
                    lockGuess()
                }
            }

            override fun onTick(timePassedInMs: Long) {
                runOnUiThread {
                    binding.progress.progress = timePassedInMs.toFloat()
                    binding.progress.labelText =
                        "${(PROGESS_TIME_MILLIS / 1000.0 - timePassedInMs / 1000.0).roundToInt()}s"
                }
            }
        }
    }

    private fun setUpGuessButtons() {
        binding.btnLockGuess.setOnClickListener { lockGuess() }
    }

    private fun nextGuess() {
        if (!level.isANewGuessLeft()) {
            transitionToEndActivity()
            return
        }
        binding.playerBackgroundView.visibility = VISIBLE
        val loadingAnimation = AnimatedLoadingPopUp(binding.root)
        loadNextGuessPicture {
            runOnUiThread {
                Log.i("Rmovein","daw")
                loadingAnimation.hideAndRemove {
                    progressBarTask.restart()
                }
                resetOverlays()
                resetMapFocusAndAnimate()
                setMapInteractionEnabled(true)

            }
        }
    }

    private fun loadNextGuessPicture(onSuccess: () -> Unit = { }) {
        level.getCurrentGuess()
            .getPicture {
                binding.guessImage
                    .setImageDrawable(it.toDrawable(resources))
                onSuccess()
            }
    }

    @UiThread
    private fun lockGuess() {
        binding.playerBackgroundView.visibility = INVISIBLE
        progressBarTask.pause()

        if (!userMadeGuess()) {
            showUnsuccessfulGuessInfoPopUp()
            return
        }

        val currentGuess = level.getCurrentGuess()
        val guessLocation = guessMarker!!.point
        level.guess(guessLocation) { it ->

            updateUIPointsToReflectGuessResult(it)
            currentGuess.getLocation {
                drawLinePolygonOnMap(it, guessLocation)

                currentGuess.getPicture() { bm ->
                    val radius = min(bm.width, bm.height) / 2F
                    val image =
                        Bitmap.createBitmap(bm.width, bm.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(image)
                    val circlePath = Path()
                    circlePath.addCircle(
                        canvas.width / 2F,
                        canvas.height / 2F,
                        radius,
                        Path.Direction.CW
                    )
                    canvas.clipPath(circlePath)
                    canvas.drawBitmap(bm, 0F, 0F, Paint())
                    val imageDrawable = image.toDrawable(resources)
                    imageDrawable.setTargetDensity(30)

                    addIconToMapAtLocationWithDrawable(it, imageDrawable.mutate())
                }
                refreshMap()
                setMapInteractionEnabled(false)
            }

        }
    }

    private fun transitionToEndActivity() {

        val endIntent = Intent(this, EndScreenActivity::class.java)

        endIntent.putExtra(
            "result",
            GsonFactory.gsonForProperUseWithIGeoPoint()
                .toJson(level.getLevelResult())
        )

        startActivity(endIntent)
    }

    private fun showSuccessfulGuessInfoPopUp(guessResult: GuessResult) {
        AnimatedPopup(binding.root as ViewGroup) {
            mainColor = R.color.back
            buttonColor = R.color.very_successful_green
            buttonText = "WEITER"
            onClickListener = OnClickListener {
                nextGuess()
            }
            description = resources.getString(
                    R.string.points_reached,
                    guessResult.getDistance(),
                    guessResult.points
                )
            extraTextRight = "%d/%d".format(level.getGuessesMadeCount(), level.getGuessCount())
        }.show()
    }

    private fun showUnsuccessfulGuessInfoPopUp() {
        AnimatedPopup(binding.root as ViewGroup) {
            mainColor = R.color.back
            buttonColor = R.color.very_unsuccessful_red
            buttonText = "WEITER"
            onClickListener = OnClickListener {
                nextGuess()
            }
            description = "Du hast 0 Punkte erhalten."
            extraTextRight = "%d/%d".format(level.getGuessesMadeCount(), level.getGuessCount())
        }.show()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUIPointsToReflectGuessResult(guessResult: GuessResult) {

        showSuccessfulGuessInfoPopUp(guessResult)
        updateCumulativeScorePoints()


        val boundingBox = BoundingBox.fromGeoPointsSafe(
            listOf(
                guessResult.guessedSpot as GeoPoint,
                guessResult.actualSpot as GeoPoint
            )
        )

        val longWidth = abs(boundingBox.lonEast - boundingBox.lonWest)
        val latWidth = abs(boundingBox.latNorth - boundingBox.latSouth)
        boundingBox.lonEast += longWidth * .2
        boundingBox.lonWest -= longWidth * .2
        boundingBox.latSouth -= latWidth * .4
        boundingBox.latNorth += latWidth * .2

        binding.guessMap.zoomToBoundingBox(boundingBox, true)
    }


    private fun addIconToMapAtLocationWithDrawable(location: IGeoPoint, drawable: Drawable?) {
        val actualMarker = OverlayItem("Actual", "", location)

        drawable?.also { actualMarker.setMarker(it) }
        actualMarker.markerHotspot = OverlayItem.HotspotPlace.CENTER

        iconOverlay.addItem(actualMarker)
    }

    private fun drawLinePolygonOnMap(
        from: IGeoPoint,
        to: IGeoPoint
    ) {
        binding.guessMap.overlays.add(0, Polygon().apply {
            points = LinkedList(listOf(from as GeoPoint, to as GeoPoint))
            strokeColor = resources.getColor(R.color.back)
            strokeWidth = 20F
        })
    }

    private fun userMadeGuess() = guessMarker != null

    private fun showGuessResultInfoCard() {
    }

    private fun enableMapPointGestureDetector() {
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
        guessMarker!!.markerHotspot = OverlayItem.HotspotPlace.CENTER
        guessMarker!!.setMarker(locationMarkerDrawable())
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

    private fun resetMapFocusAndAnimate() {
        binding.guessMap.controller.apply {
            animateTo(GEOPOINT_HHU, 17.0, 500L)
        }
    }

    private fun resetMapFocus() {
        binding.guessMap.controller.apply {
            setCenter(GEOPOINT_HHU)
            setZoom(19.0)
        }
    }

    private fun setMapInteractionEnabled(newState: Boolean) {
        binding.guessMap.apply {
            isUserInteractionEnabled = newState
        }
    }

    private fun locationMarkerDrawable(): BitmapDrawable {
        val bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val c = Canvas(bm)
        val p = Paint()
        p.color = resources.getColor(R.color.skyBlue)
        c.drawCircle(50F, 50F, 30F, p)
        return BitmapDrawable(resources, bm)
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

