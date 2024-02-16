//package com.example.shopease.mapview
//
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//import com.example.shopease.databinding.ActivityMapsBinding
//import com.example.shopease.models.UserMap
//import com.google.android.gms.maps.model.LatLngBounds
//
//class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    val TAG = "MapsActivity"
//
//    private lateinit var mMap: GoogleMap
//    private lateinit var binding: ActivityMapsBinding
//    private lateinit var userMap: UserMap
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMapsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        userMap = intent.getSerializableExtra(EXTRA_USER_MAP) as UserMap
//        supportActionBar?.title = userMap.title
//
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        Log.i(TAG, "${userMap.title} and ${userMap.places.toString()}")
//        mMap.uiSettings.isZoomControlsEnabled = true
//        mMap.uiSettings.isZoomGesturesEnabled = true
//
//        //To Move the camera to center of all the points
//        val boundsBuilder = LatLngBounds.builder()
//
//        //To draw markers on all the points
//        for (place in userMap.places) {
//            val latLng = LatLng(place.latitude, place.longitude)
//            boundsBuilder.include(latLng)
//            mMap.addMarker(MarkerOptions()
//                .position(latLng)
//                .title("Marker in ${place.title}")
//                .snippet(place.description)
//            )
//        }
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 1000, 1000, 0))
//
//    }
//}