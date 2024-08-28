package de.hhufscs.campusguesser.ui.util

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionLayout.TransitionListener
import de.hhufscs.campusguesser.R

class AnimatedPopup(val rootView: ViewGroup, val builder: Builder.() -> (Unit)) {

    val rootMotionLayout: MotionLayout

    init {
        val layoutInflater = LayoutInflater.from(rootView.context)
        rootMotionLayout = layoutInflater.inflate(R.layout.layout_success, rootView)
            .findViewById(R.id.popup)!!
    }

    fun show() {
        rootMotionLayout.transitionToEnd()
    }

    fun hideAndRemove(onFinish: (() -> Unit)? = null) {
        rootMotionLayout.addTransitionListener(object : TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                onFinish?.invoke()
                rootView.removeView(rootMotionLayout)
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {
            }
        })
        rootMotionLayout.transitionToStart()
    }

    class Builder {
        lateinit var color: Color
        var onClickListener: OnClickListener? = null
        var buttonText: CharSequence = "Okay"
        var extraTextRight: CharSequence = ""
        var description: CharSequence = ""
    }

}


