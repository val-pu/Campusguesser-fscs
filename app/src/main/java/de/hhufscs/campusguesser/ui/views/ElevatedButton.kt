package de.hhufscs.campusguesser.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.WHITE
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.graphics.toRectF
import com.google.android.material.button.MaterialButton
import de.hhufscs.campusguesser.R

class ElevatedButton(context: Context?, attrs: AttributeSet?) :
    MaterialButton(
        context!!, attrs
    ), View.OnTouchListener {
    val backPaint: Paint = Paint()
    val backPaintSecondary: Paint = Paint()
    var elevationPadding = 14
    val radius = resources.getDimension(R.dimen.default_radius)
    var isTouched = false
    var lockPress = false
        set(value) {
            field = value

            if(value) {
                isTouched = true
            } else {
                isTouched = false
            }
        }
    init {
        val attributes = context!!.obtainStyledAttributes(attrs, R.styleable.ElevatedView, 0, 0)

        backPaint.color = attributes.getColor(R.styleable.ElevatedView_buttonColor, resources.getColor(R.color.button_accent))
        backPaintSecondary.color = attributes.getColor(R.styleable.ElevatedView_buttonColorAccent, resources.getColor(R.color.secondary_btn))
        setTextColor(WHITE)
        setOnTouchListener(this)
        attributes.recycle()
    }

    fun setAccentColor(@ColorRes color: Int) {
        backPaintSecondary.color = resources.getColor(color)
    }

    fun setPrimaryColor(@ColorRes color: Int) {
        backPaint.color = resources.getColor(color)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        elevationPadding = if (!isTouched) 10 else 0
        val topOffset = if (isTouched) 10 else 0
        val secondaryRect = Rect(
            0, topOffset, width, height
        ).toRectF()
        paint.baselineShift = elevationPadding
        text = text
        canvas.drawRoundRect(
            secondaryRect,
            elevationPadding.toFloat() + radius,
            elevationPadding.toFloat() + radius,
            backPaintSecondary
        )
        val mainRect = Rect(
            0,
            topOffset,
            width - 0,
            height - elevationPadding
        ).toRectF()
        canvas.drawRoundRect(mainRect, radius, radius, backPaint)

        super.onDraw(canvas)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if(lockPress) {
            return true
        }
        if (event!!.action == ACTION_UP) isTouched = false
        if (event!!.action == ACTION_DOWN) isTouched = true
        invalidate()
        return false
    }

}