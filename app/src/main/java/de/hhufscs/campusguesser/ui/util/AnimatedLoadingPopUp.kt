package de.hhufscs.campusguesser.ui.util

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.databinding.ActivityGuessBinding
import de.hhufscs.campusguesser.databinding.LayoutLoadingBinding


class AnimatedLoadingPopUp(val rootView: ViewGroup) {

    val binding: LayoutLoadingBinding
    val rootLayout: View

    init {
        val layoutInflater = LayoutInflater.from(rootView.context)
        rootLayout = layoutInflater.inflate(R.layout.layout_loading, rootView, false)
            .findViewById(R.id.popup_loading)
        binding = LayoutLoadingBinding.bind(rootLayout)
        rootView.addView(rootLayout)
    }

    fun show() {
        rootLayout.scaleX = 1F
        rootLayout.scaleY = 1F
        rootLayout.animate().setDuration(400).alpha(1F).start()
    }

    fun hideAndRemove(onFinish: (() -> Unit)? = null) {
        binding.popupLoading.visibility = VISIBLE
        rootLayout.animate().setDuration(400).scaleXBy(1F).scaleYBy(1F).alpha(0F).withEndAction {
            onFinish?.invoke()
        }.start()


    }
}



