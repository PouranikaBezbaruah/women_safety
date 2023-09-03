package com.prajnadeep.womensafetyapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        (this as AppCompatActivity).supportActionBar?.title = "Important Links"
    }

    fun locatePoliceStation(view: android.view.View) {
        when (view.id) {
            R.id.locateButtonDispur -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/dU9yaAypoR2V91jt7")))
            }
            R.id.locateButtonBasistha -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/cxGekMAebDvp1WkR6")))
            }
            R.id.locateButtonHatigaon -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/5r7paxm14F2xfPMD9")))
            }
            R.id.locateButtonJalukbari -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/uDFZ5J9XaTzwnBmN6")))
            }
            R.id.locateButtonNoonmati -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/QdKPgjynqAsCAQbu5")))
            }
        }
    }

    fun callPoliceStation(view: android.view.View) {

        /*
        // use when real phone nos are added with each ID

        when (view.id) {
            R.id.callDispurButton -> {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:7399383903")
                startActivity(callIntent)
            }
        }

         */

        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:7399383903")
        startActivity(callIntent)
    }
}