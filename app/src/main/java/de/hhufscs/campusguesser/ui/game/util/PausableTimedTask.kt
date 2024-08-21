package de.hhufscs.campusguesser.ui.game.util

import android.os.CountDownTimer

abstract class PausableTimedTask(val intervalMillis: Long, val durationMillis: Long) {

    private lateinit var countDownTimer: CountDownTimer
    private var timeLeft = durationMillis
    private var stopped = true

    @Synchronized
    fun start() {

        if (!stopped) return
        countDownTimer = object : CountDownTimer(timeLeft, intervalMillis) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                onTick(durationMillis - timeLeft)
            }

            override fun onFinish() {
                onFinish()
            }
        }

        stopped = false
    }

    fun pause() {
        countDownTimer.cancel()
        stopped = true
    }

    fun restart() {
        pause()
        timeLeft = durationMillis
        start()
    }

    abstract fun onFinish()
    abstract fun onTick(timePassedInMs: Int)

}