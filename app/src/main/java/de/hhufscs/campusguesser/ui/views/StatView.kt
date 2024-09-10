package de.hhufscs.campusguesser.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.WHITE
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import de.hhufscs.campusguesser.R


class StatView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    val strokePaint = Paint().apply {
        strokeWidth = 10F
        color = resources.getColor(R.color.very_successful_green)
        strokeCap = Paint.Cap.ROUND
    }

    val strokePaintBad = Paint().apply {
        strokeWidth = 10F
        color = resources.getColor(R.color.very_unsuccessful_red)
        strokeCap = Paint.Cap.ROUND
    }

    val strokePaintTop = Paint().apply {
        strokeWidth = 10F
        color = resources.getColor(R.color.top_paint_stat_view)
        strokeCap = Paint.Cap.ROUND
        setPathEffect(DashPathEffect(floatArrayOf(5f,25F), 0f))
    }

    val integralPaintStroke = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE

        strokeWidth = 10F
        color = resources.getColor(R.color.integral_paint_stroke_stat_view)
    }

    val integralPaint = Paint().apply {
        strokeWidth = 10F
        color = resources.getColor(R.color.integral_paint_stat_view)
    }

    val sumPaint = Paint().apply {
        strokeWidth = 20F
        color = WHITE
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }

    val textPaint = Paint().apply {
        typeface = resources.getFont(R.font.quicksand_bold)
        strokeWidth = 20F
        color = WHITE
    }


    val pointPaint = Paint().apply { color = resources.getColor(R.color.point_stat_view) }

    val dataPoints = arrayOf(10, 20, 0, 30, 90, 20, 50, 20, 100)


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paddingRight = width * .1F

        val padding = height * .1F
        val pointHeight = height - padding * 2
        val delta = (width - paddingRight) / (dataPoints.size + 1)


        fun calcPointY(points: Int): Float = height - points / 100F * pointHeight - padding
        fun calcIndexX(index: Int): Float = (index + 1) * delta


        // Integral dingsi
        val p = Path()
        p.setLastPoint(delta, calcPointY(0))
        dataPoints.forEachIndexed { index, dataPoint ->
            p.lineTo(calcIndexX(index), calcPointY(dataPoint))

        }
        p.lineTo(calcIndexX(dataPoints.size - 1), calcPointY(0))
        p.lineTo(delta, calcPointY(0))
        canvas.drawPath(p, integralPaint)
        canvas.drawPath(p, integralPaintStroke)

        // MAXIMUM Anzeige

        canvas.drawLine(
            calcIndexX(0),
            calcPointY(100),
            calcIndexX(dataPoints.size - 1),
            calcPointY(100),
            strokePaintTop
        )

        val sumPath = Path()

        sumPath.setLastPoint(
            calcIndexX(dataPoints.size - 1) + paddingRight * .8F,
            padding + paddingRight * .05F
        )
        sumPath.lineTo(calcIndexX(dataPoints.size - 1) + paddingRight * .8F, padding)
        sumPath.lineTo(calcIndexX(dataPoints.size - 1) + paddingRight * .2F, padding)
        sumPath.lineTo(
            calcIndexX(dataPoints.size - 1) + paddingRight * .5F,
            padding + paddingRight * .4F
        )
        sumPath.lineTo(
            calcIndexX(dataPoints.size - 1) + paddingRight * .2F,
            padding + paddingRight * .8F
        )
        sumPath.lineTo(
            calcIndexX(dataPoints.size - 1) + paddingRight * .8F,
            padding + paddingRight * .8F
        )
        sumPath.lineTo(
            calcIndexX(dataPoints.size - 1) + paddingRight * .8F,
            padding + paddingRight * .8F - paddingRight * .05F
        )

        val sumXOffset = paddingRight * .2F
        val sumYOffset = (height - padding * 2 - paddingRight * .8F) / 2F

        sumPath.offset(sumXOffset, sumYOffset)

        canvas.drawPath(sumPath, sumPaint)
        textPaint.textSize = paddingRight * .4F

        val sum = dataPoints.sum().toString()

        val textBounds = Rect()

        textPaint.getTextBounds(sum, 0, sum.length, textBounds)

        canvas.drawText(
            sum,
            calcIndexX(dataPoints.size - 1) + paddingRight * .2F + (paddingRight * .6F - textBounds.width()) / 2F + sumXOffset,
            padding + paddingRight * .8F + textBounds.height() + 30 + sumYOffset,
            textPaint
        )


        dataPoints.forEachIndexed { index, y ->


            val canvasY = height - (y / 100F * pointHeight) - padding
            val canvasX = delta * (index + 1)
            if (index + 1 != dataPoints.size) {

                val nextCanvasY = height - dataPoints[index + 1] / 100F * pointHeight - padding

                canvas.drawLine(
                    canvasX,
                    canvasY,
                    canvasX + delta,
                    nextCanvasY,
                    if (nextCanvasY < canvasY) strokePaint else strokePaintBad
                )

            }

//            canvas.drawLine(canvasX, height - padding, canvasX, canvasY, strokePaint)

            canvas.drawCircle(
                canvasX,
                canvasY,
                15F,
                pointPaint
            )
            canvas.drawCircle(
                canvasX,
                canvasY,
                8F,
                strokePaint
            )
        }


    }

}