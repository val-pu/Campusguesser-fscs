package de.hhufscs.campusguesser.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.toRect
import de.hhufscs.campusguesser.R

class RoundedImageView(context: Context, attrs: AttributeSet?) :
    RoundedConstraintLayout(context, attrs) {

    var drawableHeightToViewHeightRatio = 0f
    lateinit var orignalDrawableBounds: Rect

    @SuppressLint("UseCompatLoadingForDrawables")
    var drawable: Drawable = resources.getDrawable(R.drawable.round_mode_edit_outline_24)
        set(value) {
            field = value
            orignalDrawableBounds = field.bounds
            drawableHeightToViewHeightRatio = drawable.bounds.height() / height.toFloat()
            invalidate()
        }

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, 0, 0)

        val drawableID = attributes.getResourceId(
            R.styleable.RoundedImageView_imageSrc,
            R.drawable.round_mode_edit_outline_24
        );

        drawable = AppCompatResources.getDrawable(context, drawableID)!!

        attributes.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {


        super.onDraw(canvas)
        val clipPath = Path()

        val dx = orignalDrawableBounds.width() * drawableHeightToViewHeightRatio - width

        val boundsRect = RectF(
            borderWidthInPx- dx/2,
            borderWidthInPx,
            width - borderWidthInPx + dx/2,
            height - borderWidthInPx
        )

        clipPath.addRoundRect(
            boundsRect,
            cornerRadiusInPx - borderWidthInPx,
            cornerRadiusInPx - borderWidthInPx,
            Path.Direction.CW
        )



        drawable.bounds = boundsRect.toRect()
        canvas.clipPath(clipPath)
        drawable.draw(canvas)


    }
}