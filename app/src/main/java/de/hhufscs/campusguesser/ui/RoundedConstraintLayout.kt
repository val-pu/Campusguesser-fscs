package de.hhufscs.campusguesser.ui

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import androidx.constraintlayout.widget.ConstraintLayout

class RoundedConstraintLayout(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {

    val mOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {

            val left = 0
            val top = 0;
            val right = view.width
            val bottom = view.height
            val cornerRadiusDP = 8f
            val cornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                cornerRadiusDP,
                resources.displayMetrics
            ).toInt()

            outline.setRoundRect(left, top, right, bottom, cornerRadius.toFloat())

        }
    }

    override fun onViewAdded(view: View?) {
        super.onViewAdded(view)
        outlineProvider = mOutlineProvider
        clipToOutline = true
    }

}