package de.hhufscs.campusguesser.ui.util

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionLayout.TransitionListener
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.databinding.LayoutPopupBinding

class AnimatedPopup(val rootView: ViewGroup, val builder: Builder.() -> (Unit)) {

    val rootMotionLayout: MotionLayout
    val binding: LayoutPopupBinding

    init {
        val layoutInflater = LayoutInflater.from(rootView.context)
        rootMotionLayout = layoutInflater.inflate(R.layout.layout_popup, rootView)
            .findViewById(R.id.popup)!!
        binding = LayoutPopupBinding.bind(rootMotionLayout)

        val popupBuilder = Builder()
        builder(popupBuilder)

        binding.apply {
            btnDoSth.setOnClickListener {
                popupBuilder.onClickListener?.onClick(it)
                hideAndRemove {  }
            }
// TODO:            popupCard.background.setTint(rootMotionLayout.resources.getColor(popupBuilder.mainColor))
            btnDoSth.setPrimaryColor(popupBuilder.buttonColor)
            btnDoSth.text = popupBuilder.buttonText
            popupTitle.text = popupBuilder.title
            popupDescription.text = popupBuilder.description
            extraTextRight.text = popupBuilder.extraTextRight
        }

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
        @ColorRes
        var mainColor: Int = R.color.dark
        var buttonColor: Int = R.color.skyBlue
        var onClickListener: OnClickListener? = null
        var buttonText: CharSequence = "Okay"
        var extraTextRight: CharSequence = ""
        var description: CharSequence = "Eine Description woo"
        var title: CharSequence = "Ein Titel!"
    }

}


