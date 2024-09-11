package de.hhufscs.campusguesser.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import de.hhufscs.campusguesser.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class LoadingAnimation(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    val ballPaint = Paint()
    val fps = 25
    val timePerBall = 600L

    init {
        ballPaint.color = resources.getColor(R.color.back_secondary)
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val oneFourth = width / 4f
        val radius = oneFourth * .2
        val ballActive = (System.currentTimeMillis() % (3 * timePerBall)) / timePerBall
        val x = (System.currentTimeMillis() % timePerBall) / timePerBall.toFloat() * PI + PI
        ballPaint.alpha = (-sin(x)*40F + 60).toInt()
        canvas.drawCircle(
            oneFourth * (ballActive + 1),
            (height / 2F + sin(x) * height * .2).toFloat(), ((-.1F*sin(x)+1)*radius).toFloat(), ballPaint
        )
        ballPaint.alpha = 60
        repeat(3) {
            if (it.toLong() != ballActive) {


                canvas.drawCircle(
                    oneFourth * (it + 1),
                    height / 2F, radius.toFloat(), ballPaint
                )
            }
        }


        postInvalidateDelayed((1000 / fps).toLong())
        super.onDraw(canvas)
    }

}