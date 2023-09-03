package com.prajnadeep.womensafetyapp

import     android.Manifest
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.isapanah.awesomespinner.AwesomeSpinner
import java.util.*

import android.app.ActivityManager.RunningTaskInfo

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import hossamscott.com.github.backgroundservice.RunService
import kotlin.math.log
import android.content.Intent
import android.net.Uri
import android.content.DialogInterface





@IgnoreExtraProperties
data class User(val userID:String, val deviceName: String? = null, val address: String? = null,
                val latitude:String?=null, val longitude:String?=null) {  }

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager

    private lateinit var myAddress : TextView
    private lateinit var myLatitude :TextView
    private lateinit var myLongitude: TextView
    private lateinit var cAddress : TextView
    private lateinit var cLatitude: TextView
    private lateinit var cLongitude: TextView

    private val locationPermissionCode = 2
    private val phonePermissionCode = 4

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userID:String

    private lateinit var myDevicesArray:MutableList<String>
    private lateinit var mySpinner: AwesomeSpinner
    private lateinit var myDevicesSpinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (this as AppCompatActivity).supportActionBar?.title = "Women Safety App"

        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        //initialize spinner
        mySpinner = findViewById<View>(R.id.deviceListSpinner) as AwesomeSpinner
        myDevicesArray = ArrayList()

        myDevicesSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, myDevicesArray)
        mySpinner.setAdapter(myDevicesSpinnerAdapter)

        mySpinner.setOnSpinnerItemClickListener { position, itemAtPosition ->
            Toast.makeText(this, "position : ${position}, item: $itemAtPosition", Toast.LENGTH_SHORT).show()

            database.child("users").get().addOnSuccessListener {
                println(it.value)
                for (ds in it.children) {
                    val name = ds.child("deviceName").getValue(String::class.java)
                    if (name == itemAtPosition) {
                        val adr = ds.child("address").getValue(String::class.java)
                        val lat = ds.child("latitude").getValue(String::class.java)
                        val lng = ds.child("longitude").getValue(String::class.java)
                        runOnUiThread {
                            cAddress.text = adr
                            cLatitude.text = lat
                            cLongitude.text = lng
                        }
                    }
                }

                // update on data change
                val databaseReference = FirebaseDatabase.getInstance().getReference("users")
                databaseReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        for (ds in snapshot.children) {
                            val name = ds.child("deviceName").getValue(String::class.java)
                            if (name == itemAtPosition) {
                                val adr = ds.child("address").getValue(String::class.java)
                                val lat = ds.child("latitude").getValue(String::class.java)
                                val lng = ds.child("longitude").getValue(String::class.java)
                                runOnUiThread {
                                    cAddress.text = adr
                                    cLatitude.text = lat
                                    cLongitude.text = lng
                                }
                            }
                        }
                        Log.i("DDM", "Data Changed")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i("DDM", "Error While Messaging")
                    }
                })
            }
        }

        if (auth.currentUser == null){
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(baseContext, "Anonymous SignIn Success", Toast.LENGTH_SHORT).show()
                        val currentUser = auth.currentUser
                        userID = currentUser!!.uid

                        getDeviceDataFromDatabase()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Anonymous SignIn Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        else {
            userID = auth.currentUser!!.uid
            Toast.makeText(this, "SignIn Success", Toast.LENGTH_SHORT).show()
            getDeviceDataFromDatabase()
        }

        myAddress = findViewById(R.id.myAddressTextView)
        myLatitude = findViewById(R.id.latitudeTextView)
        myLongitude = findViewById(R.id.longitudeTextView)

        cAddress = findViewById(R.id.connectedDeviceAddressTextView)
        cLatitude = findViewById(R.id.connectedDeviceLatitudeTextView)
        cLongitude = findViewById(R.id.connectedDeviceLongitudeTextView)

        val shareButton: Button = findViewById(R.id.shareLocationButton)
        val sendButton: Button = findViewById(R.id.sendLocationButton)

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,locationPermissionCode)

        shareButton.setOnClickListener {
            Toast.makeText(this, "Fetching data, just a sec. .", Toast.LENGTH_LONG).show()
            getLocation()
        }

        sendButton.setOnClickListener{
            if (!myLatitude.text.isNullOrBlank()){
                val str = "Help! I'm in trouble, here's my location \n http://maps.google.com/maps?q=loc:"+myLatitude.text+","+myLongitude.text
                // share location
                sendMessageOnWhatsapp(str)
                println(str)
            }

            else {
                Toast.makeText(this, "Please locate first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDeviceDataFromDatabase() {

        database.child("users").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")

            myDevicesArray.clear()

            for (ds in it.children) {
                val name = ds.child("deviceName").getValue(String::class.java)
                if (name != null) {
                    myDevicesArray.add(name)
                    println(name)
                }
            }

            //update spinner
            myDevicesSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, myDevicesArray)
            mySpinner.setAdapter(myDevicesSpinnerAdapter)

        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE), locationPermissionCode)
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5f, myLocationListener)

        startService(Intent(this, BackgroundService::class.java))

    }

    private var myLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {

            val gcd = Geocoder(applicationContext, Locale.getDefault())
            val addresses: List<Address> = gcd.getFromLocation(location.latitude, location.longitude, 1)

            var address:String? = null

            // update TextViews
            if (addresses.isNotEmpty()){
                address = addresses[0].subLocality.toString()+", "+addresses[0].locality.toString()
                myAddress.text = address
            }
            myLatitude.text = location.latitude.toString()
            myLongitude.text = location.longitude.toString()

            Log.d("r0ck", "on location changed: " + location.latitude + " & " + location.longitude)


            //check device name before upload
            database.child("users").child(userID).get().addOnSuccessListener {
                val deviceName = it.child("deviceName").getValue<String>(String::class.java)

                for (dataSnapshot in it.children){
                    if (dataSnapshot.child("deviceName").getValue<String>(String::class.java).isNullOrEmpty()){
                        database.child("users").child(userID).setValue(User(auth.currentUser?.uid.toString(),deviceName, address,location.latitude.toString(),location.longitude.toString()))
                    }

                    else{
                        // Upload location to database
                        database.child("users").child(userID).setValue(User(auth.currentUser?.uid.toString(),"Demo", address,location.latitude.toString(),location.longitude.toString()))
                    }
                }
            }

                    // First time open update default name
            .addOnFailureListener {
                database.child("users").child(userID).setValue(User(auth.currentUser?.uid.toString(),"Demo", address,location.latitude.toString(),location.longitude.toString()))

            }

       }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }


    //check and request permission.
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Request permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == phonePermissionCode){
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:7399383903")
            startActivity(callIntent)
        }
    }

    private fun sendMessageOnWhatsapp(message: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.setPackage("com.whatsapp")
        intent.putExtra(Intent.EXTRA_TEXT, message)

        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, "Error ! $ex", Toast.LENGTH_SHORT).show()
        }
    }

    fun renameDevice(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun emergencyButtonCLickListener(view: android.view.View) {
        if (view.id == R.id.callPoliceButton){

//            val builder1: AlertDialog.Builder = AlertDialog.Builder(applicationContext)
//            builder1.setMessage("Do you really want to call the police ?")
//            builder1.setCancelable(true)
//
//            builder1.setPositiveButton("Yes",
//                DialogInterface.OnClickListener { dialog, id ->
//                    Toast.makeText(this, "Success CAlling", Toast.LENGTH_SHORT).show()
//                    dialog.cancel()
//                })
//
//            builder1.setNegativeButton(
//                "No",
//                DialogInterface.OnClickListener { dialog, id ->
//                    Toast.makeText(this, "NOT CALL", Toast.LENGTH_SHORT).show()
//                    dialog.cancel() })
//
//            val alert11: AlertDialog = builder1.create()
//            alert11.show()

            // check and request call permissions
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
                // Request permission
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CALL_PHONE), phonePermissionCode)
            } else {

                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:7399383903")
                startActivity(callIntent)
            }


            //startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")))
        }

        else if (view.id == R.id.helplineButton){
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

    }

}