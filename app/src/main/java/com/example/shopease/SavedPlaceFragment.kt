package com.example.shopease


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.shopease.models.ShopListWithCoordinates
import com.example.shopease.wishLists.WishlistsFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

// Import your DB helper classes here

class SavedPlaceFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private val shopListWithCoordinates: MutableList<ShopListWithCoordinates> = mutableListOf()
    private lateinit var username: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_place, container, false)
        username = arguments?.getString("USERNAME_KEY") ?: ""

        mapView = view.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        // Fetch lists with coordinates from DB
        fetchListsFromDB()

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.setOnMarkerClickListener(this)
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
        }
        if (shopListWithCoordinates.isNotEmpty()) {
            val firstLocation = LatLng(
                shopListWithCoordinates[0].latitude,
                shopListWithCoordinates[0].longitude
            ) // Zoom to the first location
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10f))
        }
    }

    private fun fetchListsFromDB() {
        // Replace with your actual code to fetch lists with coordinates from DB
        // Example code to demonstrate fetching from DB
        val shopList1 = ShopListWithCoordinates("1","אכ", 32.0853, 34.7818)
        val shopList2 = ShopListWithCoordinates("2","Shop 2", 32.0767, 34.7723)
        val shopList3 = ShopListWithCoordinates("3","Shop 3", 32.0649, 34.7764)
        shopListWithCoordinates.addAll(listOf(shopList1, shopList2, shopList3))
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
                putString("SHOP_LIST_ID_KEY", shopList.id) // Assuming id is a string in ShopListWithCoordinates
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
