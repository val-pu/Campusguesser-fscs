package de.hhufscs.campusguesser.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toRectF
import de.hhufscs.campusguesser.R

class RoundedConstraintLayout(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {
    val cornerRadiusDP = 10.5F
    val cornerRadiusInPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        cornerRadiusDP,
        resources.displayMetrics
    ).toInt()
    val mOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {

            val left = 0
            val top = 0;
            val right = view.width
            val bottom = view.height


            outline.setRoundRect(left, top, right, bottom, cornerRadiusInPx.toFloat())

        }
    }

    @ColorInt
    val mainColor: Int
    @ColorInt
    val borderColor: Int
    val borderWidth: Float

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedConstraintLayout,0,0)
        mainColor = attributes.getColor(R.styleable.RoundedConstraintLayout_mainColor, Color.LTGRAY)
        borderColor =
            attributes.getColor(R.styleable.RoundedConstraintLayout_borderColor, Color.GRAY)
        borderWidth =
            attributes.getDimension(R.styleable.RoundedConstraintLayout_borderWidth, 2F)


        attributes.recycle()

        setBackgroundColor(Color.LTGRAY) // Damit was gezeichnet wird, seems stoopid, ist aber notwendig

    }

    override fun onViewAdded(view: View?) {
        super.onViewAdded(view)
        outlineProvider = mOutlineProvider
        clipToOutline = true
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val rect = Rect(0, 0, width, height).toRectF()


        canvas.drawColor(borderColor)
        val p = Paint()
        p.color = mainColor

        val borderWidthInPx = resources.displayMetrics.density * borderWidth

        rect.inset(borderWidthInPx, borderWidthInPx)
        canvas.drawRoundRect(
            rect,
            cornerRadiusInPx-borderWidthInPx,
            cornerRadiusInPx-borderWidthInPx,
            p
        )
        super.onDraw(canvas)
    }

}