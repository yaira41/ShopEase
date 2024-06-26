package com.example.shopease.fragments


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.shopease.activities.BaseActivity
import com.example.shopease.R
import com.example.shopease.dbHelpers.ShopListsDatabaseHelper
import com.example.shopease.models.ShopListWithCoordinates
import com.example.shopease.wishLists.ShopListFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class SavedPlaceFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private val shopListWithCoordinates: MutableList<ShopListWithCoordinates> = mutableListOf()
    private lateinit var username: String
    private val PERMISSION_REQUEST_CODE = 123 // Choose any value you prefer
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnReturnToSavedLocation: Button
    private lateinit var dbHelper: ShopListsDatabaseHelper
    private var currentLocationIndex = 0 // Initialize the current location index

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_place, container, false)
        username = (activity as BaseActivity?)?.username!!

        dbHelper = ShopListsDatabaseHelper()


        mapView = view.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        btnReturnToSavedLocation = view.findViewById(R.id.btnReturnToSavedLocation)



        btnReturnToSavedLocation.setOnClickListener {
            // Call function to return to the first saved location from the DB
            returnToFirstSavedLocation()
        }

        // Fetch lists with coordinates from DB
        fetchListsFromDB()
//        fetchData()

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.setOnMarkerClickListener(this)

        // Add markers for shop lists
        for (location in shopListWithCoordinates) {
            val marker = googleMap?.addMarker(
                MarkerOptions().position(
                    LatLng(
                        location.latitude,
                        location.longitude
                    )
                ).title(location.name)
            )
            marker?.tag = location // Attach ShopListWithCoordinates object to the marker
            context?.let { context ->
                val originalBitmap =
                    BitmapFactory.decodeResource(context.resources, R.drawable.wishlist_icon)
                val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)
                marker?.setIcon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
            }
        }

        // Zoom to the first location if available
        if (shopListWithCoordinates.isNotEmpty()) {
            val firstLocation = LatLng(
                shopListWithCoordinates[0].latitude,
                shopListWithCoordinates[0].longitude
            )
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10f))
        }

        // Get current location and zoom to it
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Got the location
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val latLng = LatLng(latitude, longitude)
                    Toast.makeText(
                        context,
                        "Latitude: $latitude, Longitude: $longitude",
                        Toast.LENGTH_SHORT
                    ).show()

                    googleMap?.addMarker(MarkerOptions().position(latLng).title("My Location"))
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                } else {
                    Toast.makeText(context, "Couldn't get location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun returnToFirstSavedLocation() {
        // Check if shopListWithCoordinates is not empty
        if (shopListWithCoordinates.isNotEmpty()) {
            // Increment the current location index
            currentLocationIndex = (currentLocationIndex + 1) % shopListWithCoordinates.size

            // Get the next location
            val nextLocation = shopListWithCoordinates[currentLocationIndex]

            // Move the map to the next location
            val nextLatLng = LatLng(nextLocation.latitude, nextLocation.longitude)
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(nextLatLng, 15f))
        }
    }

    private fun fetchListsFromDB() {
        dbHelper.getAllUserLists(username) { items ->
            if (items.isEmpty()) {
                Toast.makeText(context, "נראה שאין לך פריטים ברשימה", Toast.LENGTH_SHORT).show()
            } else {
                // Extract required data and populate shopListWithCoordinates
                for (shopList in items) {
                    // Extract id, name, latitude, and longitude from each shopList
                    val id = shopList.id
                    val name = shopList.name
                    val latitude = shopList.latitude
                    val longitude = shopList.longitude

                    // Check if latitude and longitude are not 0.0
                    if (latitude != 0.0 && longitude != 0.0) {
                        // Create ShopListWithCoordinates object and add it to the list
                        val shopListWithCoordinatesItem =
                            ShopListWithCoordinates(id, name, latitude, longitude)
                        shopListWithCoordinates.add(shopListWithCoordinatesItem)
                    }
                }

                // Update the map with markers
                googleMap?.let { map ->

                    // Update map with markers
                    onMapReady(map)
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val shopList = marker.tag as? ShopListWithCoordinates
        if (shopList != null) {
            val bundle = Bundle().apply {
                putString(
                    "SHOP_LIST_ID_KEY",
                    shopList.id
                )
                putString("SHOP_LIST_NAME_KEY", shopList.name)
                putString("USERNAME_KEY", username)
            }
            val shopListFragment = ShopListFragment()
            shopListFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, shopListFragment)
                .addToBackStack(null)
                .commit()
            return true
        }
        return false
    }

}
