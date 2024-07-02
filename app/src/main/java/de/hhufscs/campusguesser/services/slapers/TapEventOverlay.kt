package de.hhufscs.campusguesser.services.slapers

import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay



class TapEventOverlay(onLocationSelected: (GeoPoint) -> Unit) :
    MapEventsOverlay(
        object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(tappedLocation: GeoPoint): Boolean {

                onLocationSelected(tappedLocation)

                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean = false
        }
    ) {

}
