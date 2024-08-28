package de.hhufscs.campusguesser.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import org.osmdroid.views.MapView

class MapViewWithDisablebleUserInteraction(context: Context?, attrs: AttributeSet?) : MapView(context, attrs) {
    var isUserInteractionEnabled = true


    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (isUserInteractionEnabled.not()) {
            return false
        }
        return super.dispatchTouchEvent(event)
    }



}