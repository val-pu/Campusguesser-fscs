package de.hhufscs.campusguesser.services.async

import android.os.Handler
import android.os.Looper
import android.util.Log

class NetworkFileThread<T>(private var runnable: () -> T, private var resultConsumer: (T) -> Unit) :
    Thread() {
    private var result: T? = null
    private var handler: Handler = Handler(Looper.getMainLooper())

    override fun run(){
        this.result = runnable()
        Log.d("PERSONALDEBUG", "run: result set to $result")
        handler.post{resultConsumer(result!!)}
    }
}