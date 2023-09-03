package com.prajnadeep.womensafetyapp

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler

import android.os.IBinder
import android.util.Log
import android.widget.Toast

class BackgroundService : Service() {
    var context: Context = this
    var handler: Handler? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show()
        handler = Handler()
        runnable = Runnable {
            Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show()
            runnable?.let { handler!!.postDelayed(it, 5000) }
        }
        handler!!.postDelayed(runnable!!, 5000)
    }

    override fun onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show()
    }

    override fun onStart(intent: Intent, startid: Int) {
        Log.d("backService","Started")
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show()
    }

    companion object {
        var runnable: Runnable? = null
    }
}