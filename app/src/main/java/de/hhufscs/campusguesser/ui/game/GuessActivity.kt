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
import android.os.Handler
import android.os.Looper
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
import de.hhufscs.campusguesser.services.factories.OnlineLevelFactory
import de.hhufscs.campusguesser.ui.game.endscreen.EndScreenActivity
import de.hhufscs.campusguesser.ui.game.endscreen.GsonFactory
import de.hhufscs.campusguesser.ui.game.util.PausableTimedTask
import de.hhufscs.campusguesser.ui.util.AnimatedLoadingPopUp
import de.hhufscs.campusguesser.ui.util.AnimatedPopup
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polygon
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class GuessActivity : AppCompatActivity() {
    companion object {
        val GEOPOINT_HHU = GeoPoint(51.18885, 6.79551)
        val GEOPOINT_LLANFAIRPWLLGWYNGYLLGOGERYCHWYRNDROBWLLLLANTYSILIOGOGOGOCH = GeoPoint(53.22069, -4.20932)
    }

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private val PROGESS_TIME_MILLIS = 30_000L

    private lateinit var binding: ActivityGuessBinding
    private lateinit var level: Level
    private lateinit var loadingPopUp: AnimatedLoadingPopUp

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

    private var guessSubmitted = false;

    private lateinit var progressBarTask: PausableTimedTask

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Log.i("CampusGuesser", "Started GuessActivity")

        // OSM will das
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        binding = ActivityGuessBinding.inflate(layoutInflater)
        initOfflineMap()
        setContentView(binding.root)
        loadingPopUp = AnimatedLoadingPopUp(binding.root)
        loadingPopUp.show(1)
        setUpOSMMap()
        setupIconOverlay()
        enableMapPointGestureDetector()
        setUpGuessButtons()
        initProgressBar()
        binding.imageView.setOnClickListener() { finish() }

        var onlineuuid = intent.getStringExtra("uuid")


        val n = intent.getIntExtra("count",10)
        val onlineLevelFactory = OnlineLevelFactory()
        if (onlineuuid == null) {
            onlineLevelFactory.getLevelWithNOnlineGuesses(n) {
                level = it
                nextGuess()
            }
        } else {
            onlineLevelFactory.getLevelByUUID(onlineuuid) {
                level = it
                nextGuess()
            }
        }
    }

    private fun initOfflineMap() {
        //Achtung, hier wird gepfuscht :(
        val file = File(cacheDir.absolutePath + "hhu_map.zip")
        if(!file.exists()){
            val outputStream = FileOutputStream(file)
            val inputStream = assets.open("hhu_map.zip")
            var byteArray: ByteArray = ByteArray(1024)
            var len = inputStream.read(byteArray)
            while(len != -1){
                outputStream.write(byteArray)
                len = inputStream.read(byteArray)
            }
        }
        val tileProvider3000 = OfflineTileProvider(SimpleRegisterReceiver(this), Array<File>(1){File(cacheDir.absolutePath + "hhu_map.zip")})
        binding.guessMap.tileProvider = tileProvider3000
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
        binding.btnLockGuess.setOnClickListener {
            if(!guessSubmitted){
                lockGuess()
                guessSubmitted = true
            }
        }
    }

    private fun nextGuess() {
        if (!level.isANewGuessLeft()) {
            transitionToEndActivity()
            return
        }
        binding.playerBackgroundView.visibility = VISIBLE
        loadingPopUp.show()
        loadNextGuessPicture {
            Handler(Looper.getMainLooper()).postDelayed({
                runOnUiThread {
                    resetMapFocus()
                    Log.i("Rmovein", "daw")
                    loadingPopUp.hideAndRemove {
                        progressBarTask.restart()
                    }
                    guessSubmitted = false
                    resetOverlays()
                    setMapInteractionEnabled(true)
                }
            }, 1000)
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

        val userMadeGuess = userMadeGuess()
        if (!userMadeGuess) {
            showUnsuccessfulGuessInfoPopUp()
            addGuessMarkerTo(GEOPOINT_LLANFAIRPWLLGWYNGYLLGOGERYCHWYRNDROBWLLLLANTYSILIOGOGOGOCH)
        }
        val guessLocation = guessMarker!!.point

        val currentGuess = level.getCurrentGuess()
        level.guess(guessLocation) { it ->

            if(userMadeGuess){
                showSuccessfulGuessInfoPopUp(it)
            }
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

        val endIntent = Intent(baseContext, EndScreenActivity::class.java)

        endIntent.putExtra(
            "result",
            GsonFactory.gsonForProperUseWithIGeoPoint()
                .toJson(level.getLevelResult())
        )

        startActivity(endIntent)
        finish()
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
            description = "Du hast nicht gesetzt. Dein Guess wurde deswegen automatisch nach Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch gesetzt. Du hast 0 Punkte erhalten"
            extraTextRight = "%d/%d".format(level.getGuessesMadeCount(), level.getGuessCount())
        }.show()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUIPointsToReflectGuessResult(guessResult: GuessResult) {

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
        iconOverlay.addItem(guessMarker!!)
    }

    private fun removeOldGuessMarker() {
        if (userMadeGuess()) {
            iconOverlay.removeItem(guessMarker)
        }
    }

    private fun userMadeGuess() = guessMarker != null

    private fun setUpOSMMap() {

        binding.guessMap.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }

        resetMapFocus()
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

