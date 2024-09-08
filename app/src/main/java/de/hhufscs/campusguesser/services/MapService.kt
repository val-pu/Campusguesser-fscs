package de.hhufscs.campusguesser.services

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.services.factories.OverlayFactory
import de.hhufscs.campusguesser.services.slapers.TapEventOverlay
import de.hhufscs.campusguesser.ui.game.GuessActivity
import org.osmdroid.api.IGeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem

class MapService(
    val mapView: MapView,
    val baseFocusPoint: IGeoPoint = GuessActivity.GEOPOINT_HHU,
    val baseZoom: Double = 18.0
) {
    private val context = mapView.context
    private val overlays = mapView.overlays

    private lateinit var iconOverlay: ItemizedIconOverlay<OverlayItem>
    private var tapOverlay: MapEventsOverlay? = null

    private var locationSelectorMarker: OverlayItem? = null

    private var locationSelectedListener: ((IGeoPoint) -> Unit)? = null

    init {
        setUpOSM()
        resetMapFocus()
        setupIconOverlay()
    }

    fun resetMapFocus() {
        setFocusTo(baseFocusPoint)
    }

    fun setFocusTo(focusPoint: IGeoPoint, zoom: Double = baseZoom) {
        mapView.controller.apply {
            setZoom(zoom)
            animateTo(focusPoint)
        }
    }

    fun isLocationSelectorEnabled() = tapOverlay != null

    fun enableLocationSelector(onLocationSelectedListener: (IGeoPoint) -> Unit) {
        locationSelectedListener = onLocationSelectedListener

        setupLocationSelectorListener()
    }

    fun disableLocationSelector() {
        removeMapOverlay(tapOverlay!!)

        tapOverlay = null
        locationSelectedListener = null
    }

    fun moveLocationSelectorMarkerTo(IGeoPoint: IGeoPoint) {
        setLocationSelectorMarkerTo(IGeoPoint)
    }

    fun refreshMap() {
        mapView.invalidate()
    }


    private fun setUpOSM() {
        mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    private fun setupLocationSelectorListener() {

        tapOverlay = TapEventOverlay { tappedLocation ->

            setLocationSelectorMarkerTo(tappedLocation)
            locationSelectedListener!!.invoke(tappedLocation)

            refreshMap()
        }

        overlays.add(tapOverlay)
    }

    private fun setLocationSelectorMarkerTo(newLocation: IGeoPoint) {
        removeLocationSelectorMarker()
        addGuessMarkerTo(newLocation)
    }

    private fun addGuessMarkerTo(newLocation: IGeoPoint) {
        locationSelectorMarker = OverlayItem("The Spot!", "", newLocation)


        locationSelectorMarker!!.apply {


            setMarker(locationMarkerDrawable())
            iconOverlay.addItem(this)
        }

    }

    private fun locationMarkerDrawable(): BitmapDrawable {
        val bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val c = Canvas(bm)
        val p = Paint()
        p.color = context.getColor(R.color.skyBlue)
        c.drawCircle(50F, 50F, 50F, p)
        return BitmapDrawable(context.resources, bm)
    }

    private fun removeLocationSelectorMarker() {
        iconOverlay.removeItem(locationSelectorMarker)
    }


    private fun setupIconOverlay() {
        iconOverlay = OverlayFactory.simpleIconOverlay(context)
        overlays.add(iconOverlay)
    }

    private fun removeMapOverlay(overlay: Overlay) {
        overlays.remove(overlay)
    }

}