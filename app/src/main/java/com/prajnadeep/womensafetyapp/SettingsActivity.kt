package com.prajnadeep.womensafetyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class SettingsActivity : AppCompatActivity() {
    private lateinit var deviceName: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        (this as AppCompatActivity).supportActionBar?.title = "Settings"

        deviceName = findViewById(R.id.deviceNameTextview)
        val settingsButton: Button = findViewById(R.id.renameButton)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        if (auth.currentUser != null) {
            database.child("users").child(auth.currentUser!!.uid).get().addOnSuccessListener {
                Log.i("firebase", "Got value ${it.child("deviceName").value}")

                val dName:String = it.child("deviceName").value.toString()
                deviceName.setText(dName)

            }.addOnFailureListener {
                Log.e("firebase", "Error getting data", it)
            }
        }

        settingsButton.setOnClickListener {
            if (auth.currentUser!=null){
                val newName = deviceName.text
                database.child("users").child(auth.currentUser!!.uid).child("deviceName").setValue(newName.toString()).addOnCompleteListener {
                    Toast.makeText(this, "Device renamed successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}