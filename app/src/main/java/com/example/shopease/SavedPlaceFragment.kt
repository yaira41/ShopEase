package com.example.shopease


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
import com.example.shopease.dbHelpers.ShopListsDatabaseHelper
import com.example.shopease.models.ShopListWithCoordinates
import com.example.shopease.wishLists.WishlistsFragment
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

// Import your DB helper classes here

class SavedPlaceFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private val shopListWithCoordinates: MutableList<ShopListWithCoordinates> = mutableListOf()
    private lateinit var username: String
    private val PERMISSION_REQUEST_CODE = 123 // Choose any value you prefer
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnZoomIn: Button
    private lateinit var btnZoomOut: Button
    private lateinit var btnReturnToSavedLocation: Button
    private lateinit var dbHelper: ShopListsDatabaseHelper
    private lateinit var id: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_place, container, false)
        username = arguments?.getString("USERNAME_KEY") ?: ""

        dbHelper = ShopListsDatabaseHelper()


        mapView = view.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//        btnZoomIn = view.findViewById(R.id.btnZoomIn)
//        btnZoomOut = view.findViewById(R.id.btnZoomOut)
        btnReturnToSavedLocation = view.findViewById(R.id.btnReturnToSavedLocation)

//        btnZoomIn.setOnClickListener {
//            // Call function to zoom in
//            zoomIn()
//        }
//
//        btnZoomOut.setOnClickListener {
//            // Call function to zoom out
//            zoomOut()
//        }

        btnReturnToSavedLocation.setOnClickListener {
            // Call function to return to the first saved location from the DB
            returnToFirstSavedLocation()
        }

        // Fetch lists with coordinates from DB
        fetchListsFromDB()
//        fetchData()

        return view
    }

    private fun zoomIn() {
        googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
    }

    private fun zoomOut() {
        googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
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
                // Load the original bitmap
                val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.icon_app)
                // Resize the bitmap to 50x50 pixels
                val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 150, 150, false)
                // Set the resized bitmap as the marker icon
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
            val firstLocation = LatLng(
                shopListWithCoordinates[0].latitude,
                shopListWithCoordinates[0].longitude
            )
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 15f))
        }
    }


    private fun fetchData() {
        // Replace with your actual code to fetch lists with coordinates from DB
        // Example code to demonstrate fetching from DB
        val shopList1 = ShopListWithCoordinates("1", "אכ", 32.0853, 34.7818)
        val shopList2 = ShopListWithCoordinates("2", "Shop 2", 32.0767, 34.7723)
        val shopList3 = ShopListWithCoordinates("3", "Shop 3", 32.0649, 34.7764)
        shopListWithCoordinates.addAll(listOf(shopList1, shopList2, shopList3))

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
                    val latitude = String.format("%.4f", shopList.latitude)
                    val longitude = String.format("%.4f", shopList.longitude)
                    // If latitude or longitude has fewer than four digits after the decimal point, pad with zeros
                    val formattedLatitude = "%.${4 - latitude.substringAfter(".").length}f".format(latitude.toDouble())
                    val formattedLongitude = "%.${4 - longitude.substringAfter(".").length}f".format(longitude.toDouble())
                    Toast.makeText(
                        context,
                        "Id: $id,name: $name, Latitude: $latitude, Longitude: $longitude",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Create ShopListWithCoordinates object and add it to the list
                    val shopListWithCoordinatesItem =
                        ShopListWithCoordinates(id, name, formattedLatitude.toDouble(), formattedLongitude.toDouble())
                    shopListWithCoordinates.add(shopListWithCoordinatesItem)

                }
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
            // Create a bundle to pass data to the WishlistsFragment
            val bundle = Bundle().apply {
                putString(
                    "SHOP_LIST_ID_KEY",
                    shopList.id
                ) // Assuming id is a string in ShopListWithCoordinates
                putString("SHOP_LIST_NAME_KEY", shopList.name)
                putString("USERNAME_KEY", username)
                // Add more data if needed
            }
            // Create an instance of WishlistsFragment and set the arguments bundle
            val wishlistsFragment = WishlistsFragment()
            wishlistsFragment.arguments = bundle
            // Navigate to WishlistsFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, wishlistsFragment)
                .addToBackStack(null)
                .commit()
            return true // Mark event as handled
        }
        return false // Let the default behavior occur
    }

}
