package de.hhufscs.campusguesser.ui.util

import android.view.LayoutInflater
import android.view.ViewGroup
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.databinding.ActivityGuessBinding


class AnimatedLoadingPopUp(val rootView: ViewGroup) {

    val binding: ActivityGuessBinding
    val rootLayout: ViewGroup
    init {
        val layoutInflater = LayoutInflater.from(rootView.context)
         rootLayout= layoutInflater.inflate(R.layout.activity_guess, rootView)
            .findViewById(R.id.root_thingy)!!
        binding = ActivityGuessBinding.bind(rootLayout)

        // binding.root.animate().setDuration(1000L).alpha(1F).start()

    }

    fun hideAndRemove(onFinish: (() -> Unit)? = null) {
        //binding.root.animate().setDuration(1000L).alpha(0F).start()
        //}
        rootView.post {
            Thread.sleep(100)
            rootView.removeView(rootView.findViewById(R.id.root_thingy))

        }


    }
}



