package de.hhufscs.campusguesser.services.factories

import android.content.Context
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import java.util.LinkedList

object OverlayFactory {
    fun simpleIconOverlay(context: Context): ItemizedIconOverlay<OverlayItem> {
        return ItemizedIconOverlay(
            LinkedList<OverlayItem>(),
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem?> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    return false
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    return false
                }
            }, context
        )
    }
}