package de.hhufscs.campusguesser.services.async

import android.os.Handler
import android.os.Looper
import android.util.Log

class NetworkFileThread<T>: Thread{
    private var runnable: () -> T
    private var result: T? = null
    private var handler: Handler
    private var resultConsumer: (T) -> Unit

    constructor(runnable: () -> T, resultConsumer: (T) -> Unit){
        this.runnable = runnable
        this.handler = Handler(Looper.getMainLooper())
        this.resultConsumer = resultConsumer
    }

    override fun run(){
        this.result = runnable()
        Log.d("PERSONALDEBUG", "run: result set to $result")
        handler.post{resultConsumer(result!!)}
    }
}