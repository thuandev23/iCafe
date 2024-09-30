package com.pro.shopfee.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.pro.shopfee.BuildConfig.GOOGLE_MAPS_API_KEY
import com.pro.shopfee.R

class MapsActivity : BaseActivity(), OnMapReadyCallback {

    private companion object {
        private const val TAG = "LOG_TAG"
        private const val DEFAULT_ZOOM = 15
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

    }

    private var mMap: GoogleMap? = null
    private var mPlacesClient: PlacesClient? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLastKnownLocation: Location? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var selectedAddress: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_maps)

        initToolbar()
        initUI()

    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = getString(R.string.pick_your_location)
        /*tvToolbarRight.setOnClickListener {
            if (isGPSEnabled()) {
                checkLocationPermission()
                pickCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location is not on! Turn it on to show current location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }*/
    }

    private fun initUI() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        Places.initialize(applicationContext, GOOGLE_MAPS_API_KEY)
        mPlacesClient = Places.createClient(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val autoCompleteSupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        val placesList =
            arrayOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        autoCompleteSupportMapFragment.setPlaceFields(listOf(*placesList))
        autoCompleteSupportMapFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d(TAG, "Place: " + place.name + ", " + place.id)
                val id = place.id
                val name = place.name
                val latLng = place.latLng
                selectedLatitude = latLng?.latitude
                selectedLongitude = latLng?.longitude
                selectedAddress = place.address ?: ""
                Log.d(TAG, "Selected ID: $id")
                Log.d(TAG, "Selected Name: $name")
                Log.d(TAG, "Selected Latitude: $selectedLatitude")
                Log.d(TAG, "Selected Longitude: $selectedLongitude")
                Log.d(TAG, "Selected Address: $selectedAddress")
                addMarker(latLng, name, selectedAddress)
            }
            override fun onError(status: Status) {
                Log.d(TAG, "An error occurred: $status")
            }
        })
        val btnDone = findViewById<TextView>(R.id.btnDone)
        btnDone.setOnClickListener {
            val intent = Intent()
            intent.putExtra("latitude", selectedLatitude)
            intent.putExtra("longitude", selectedLongitude)
            intent.putExtra("address", selectedAddress)
            setResult(RESULT_OK, intent)
            finish()

        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d(TAG, "onMapReady: map is ready")
        mMap = googleMap
        /// permission check

        checkLocationPermission()
        mMap!!.setOnMapClickListener { latLng ->

            selectedLatitude = latLng.latitude
            selectedLongitude = latLng.longitude
            addressFromLatLng(latLng)

        }
    }

    private fun addressFromLatLng(latLng: LatLng) {
        Log.d(TAG, "addressFromLatLng: ")
        val geocode = Geocoder(this)
        try {
            val addressList = geocode.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = addressList!![0]
            val addressLine = address.getAddressLine(0)
            val subLocality = address.subLocality
            selectedAddress = addressLine
            addMarker(latLng, subLocality, addressLine)
        } catch (e: Exception) {
            Log.e(TAG, "addressFromLatLng: ${e.message}")
        }
    }

    private fun isGPSEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Log.e(TAG, "isGPSEnabled: ${e.message}")
        }
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            Log.e(TAG, "isGPSEnabled: ${e.message}")
        }
        return !(!gpsEnabled || !networkEnabled)
    }

    @SuppressLint("MissingPermission")
    private fun detectAndShowDeviceLocationMap() {
        try {
            val locationResult = mFusedLocationProviderClient!!.lastLocation
            locationResult.addOnSuccessListener { location ->
                mLastKnownLocation = location
                selectedLatitude = location.latitude
                selectedLongitude = location.longitude
                val latLng = LatLng(selectedLatitude!!, selectedLongitude!!)
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))
                addressFromLatLng(latLng)

            }
        } catch (e: Exception) {
            Log.e(TAG, "detectAndShowDeviceLocationMap: ${e.message}")
        }
    }


    private fun pickCurrentLocation() {
        Log.d(TAG, "pickCurrentLocation: ")
        if (mMap != null) {
            return
        }
        detectAndShowDeviceLocationMap()
    }

    private fun addMarker(latLng: LatLng?, title: String?, address: String?) {

        mMap?.let { map ->
            map.clear()
            latLng?.let {
                val markerOptions = MarkerOptions().position(it).title(title).snippet(address)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                map.addMarker(markerOptions)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, DEFAULT_ZOOM.toFloat()))
                val tvAddress = findViewById<TextView>(R.id.tvSelectedPlace)
                tvAddress.text = address
            }
        }
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap!!.isMyLocationEnabled = true
                // Permission was granted
                pickCurrentLocation() // Get current location and display it
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}