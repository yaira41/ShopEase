package com.example.shopease.wishLists

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.ShopList
import com.example.shopease.dataClasses.ShopListItem
import com.example.shopease.dbHelpers.RecipesDatabaseHelper
import com.example.shopease.dbHelpers.RequestsDatabaseHelper
import com.example.shopease.dbHelpers.ShopListsDatabaseHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*
import android.location.Geocoder

class ShopListFragment : Fragment(), ShopItemOptionsBottomSheetDialogFragment.BottomSheetListener {
    private lateinit var shopListAdapter: ShopListAdapter
    private val shopListsDatabaseHelper = ShopListsDatabaseHelper()
    private lateinit var id: String
    private lateinit var name: String
    private lateinit var members: List<String>
    private var latitude: Double? = null
    private var longitude: Double? = null
    private lateinit var username: String
    private lateinit var shopListName: TextView
    private lateinit var dbHelper: ShopListsDatabaseHelper
    private lateinit var friendDbHelper: RequestsDatabaseHelper
    private val PERMISSION_REQUEST_CODE = 101
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private lateinit var geocoder:Geocoder

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_shop_list, container, false)
        dbHelper = ShopListsDatabaseHelper()
        friendDbHelper = RequestsDatabaseHelper()
        id = arguments?.getString("SHOP_LIST_ID_KEY") ?: ""
        name = arguments?.getString("SHOP_LIST_NAME_KEY") ?: "New List"
        members = arguments?.getStringArrayList("MEMBERS")?.toList() ?: listOf(username)
        username = arguments?.getString("USERNAME_KEY") ?: ""
        latitude = arguments?.getDouble("LATITUDE") ?: 0.0
        longitude = arguments?.getDouble("LONGITUDE") ?: 0.0
        // Ensure correct context for permission request
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        geocoder = Geocoder(requireActivity(),Locale.getDefault())

        // Create ActivityResultLauncher for permission request
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        shopListAdapter = ShopListAdapter(mutableListOf(),
            itemLongClickListener = object : ShopListAdapter.OnItemLongClickListener {
                override fun onItemLongClick(position: Int, view: View) {
                    showOptionsBottomSheet(position)
                }
            }
        )
        fetchData()

        val rvShopListItem = view.findViewById<RecyclerView>(R.id.rvShopListItems)
        rvShopListItem.adapter = shopListAdapter
        rvShopListItem.layoutManager = LinearLayoutManager(requireContext())
        val addButton = view.findViewById<ImageView>(R.id.bAddButton)
        val itemTitle = view.findViewById<EditText>(R.id.etItemTitle)
        val count = view.findViewById<TextView>(R.id.etQuantity)
        val unitSpinner = view.findViewById<Spinner>(R.id.unitSpinner)

        val unitList = listOf("יחידות", "קג", "ג", "מל", "ליטר") // Replace with your list of units
        val unitAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitList)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = unitAdapter
        unitSpinner.setSelection(0)

        addButton.setOnClickListener {
            val titleItem = itemTitle.text.toString()
            val countItem = count.text.toString()
            val unit = unitSpinner.selectedItem.toString()
            if (titleItem.isNotEmpty() and countItem.isNotEmpty()) {
                val newItem = ShopListItem(titleItem, countItem.toInt(), unit)
                shopListAdapter.addShopListItem(newItem)
                itemTitle.text.clear()
            }else {
                showToast("נראה שחסר לך שם מוצר ואו כמות.")
            }
        }
        val chooseLocationButton = view.findViewById<MaterialButton>(R.id.button4)
        chooseLocationButton.setOnClickListener {
            // Handle choosing a location or inserting an address
            openLocationPickerOrAddressInput()
        }

        val addRecipesButton = view.findViewById<MaterialButton>(R.id.bAddRecipes)
        addRecipesButton.setOnClickListener {
            showAddRecipesDialog()
        }

        shopListName = view.findViewById(R.id.tvListName)

        // Initially, show the TextView and hide the EditText
        shopListName.text = name

        return view;
    }

    private fun openLocationPickerOrAddressInput() {
        // Show a dialog to let the user choose between picking a location or entering an address
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Location Method")
        builder.setItems(arrayOf("Use Current Location", "Enter Address")) { dialog, which ->
            when (which) {
                0 -> {
                    // User wants to use current location
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        getCurrentLocation()

                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
                1 -> {
                    // User wants to enter address
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
//                        launchAddressInput2()
                        handleEnterAddress()

                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }
        }
        builder.show()
    }
    private fun handleEnterAddress() {
        val editText = EditText(requireContext())
        editText.hint = "Enter Address"

        AlertDialog.Builder(requireContext())
            .setTitle("Enter Address")
            .setView(editText)
            .setPositiveButton("OK") { dialog, _ ->
                val addressString = editText.text.toString()
                if (addressString.isNotEmpty()) {
                    launchAddressInput2(addressString)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please enter an address",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun launchAddressInput2(addressString: String) {
        try {
            val addressList = geocoder.getFromLocationName(addressString, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val latitude = address.latitude
                val longitude = address.longitude
                Toast.makeText(
                    requireContext(),
                    "Latitude: $latitude, Longitude: $longitude",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Address not found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    private fun launchAddressInput22() {
        val latitude = 31.884174634758583
        val longitude = 35.0341130828458
        val address = geocoder.getFromLocation(latitude,longitude,1)
        // Do something with the latitude and longitude, e.g., display them
        Toast.makeText(context, "Address: $address", Toast.LENGTH_SHORT).show()
    }

    private val addressResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val place = Autocomplete.getPlaceFromIntent(data)
            val address = place.address ?: ""

            // Check if address is empty before geocoding
            if (address.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a valid address.", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            // Geocode the address to get latitude and longitude
            val geocoder = Geocoder(requireContext(),Locale.getDefault())
            var latLng: LatLng? = null

            try {
                geocoder.getFromLocationName(address, 1)?.let { addresses ->
                    if (addresses.isNotEmpty()) {
                        latLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                    }
                }
            } catch (e: Exception) { // Handle various exceptions
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to retrieve location.", Toast.LENGTH_SHORT).show()
            }

            if (latLng != null) {
                // Use the latitude and longitude for your desired purposes
                Toast.makeText(requireContext(), "Address: $address\nLat/Lng: $latLng", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Could not retrieve latitude and longitude.", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun launchAddressInput() {
        // Create a new intent to start the Places Autocomplete Activity
        val fields = listOf(Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        ).build(requireActivity())

        addressResultLauncher.launch(intent)
    }



    fun updateList(items: List<ShopListItem>) {
            shopListsDatabaseHelper.updateShopList(id,
                shopListName.text.toString(),
                items,
                members,
                object : ShopListsDatabaseHelper.InsertShopListCallback {
                    override fun onShopListInserted(shopList: ShopList?) {}
                }
            )
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
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Got the location
                    latitude = location.latitude
                    longitude = location.longitude
                    // Do something with the latitude and longitude, e.g., display them
                    Toast.makeText(context, "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Couldn't get location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onDeleteButtonClick(position: Int) {
        if (position >= 0 && position < shopListAdapter.items.size) {
            val selectedItem = shopListAdapter.items[position]
            Toast.makeText(requireContext(), "Delete ${selectedItem.title}", Toast.LENGTH_SHORT)
                .show()

            shopListAdapter.items.removeAt(position)
            shopListAdapter.notifyItemRemoved(position)
            // Update positions of remaining items
            shopListAdapter.notifyItemRangeChanged(position, shopListAdapter.items.size)
        }
    }

    private fun fetchData() {
        dbHelper.getListById(id) { items ->
            if (items.isEmpty()) {
                showToast("נראה שאין לך פריטים ברשימה")
            } else {
                shopListAdapter.initialList(items)
                shopListAdapter.notifyDataSetChanged()

            }
        }
    }

    private fun showToast(message: String) {
        val context = context
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog(position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        val confirmButton: MaterialButton = dialogView.findViewById(R.id.btnConfirmDelete)
        val cancelButton: MaterialButton = dialogView.findViewById(R.id.btnCancelDelete)

        confirmButton.setOnClickListener {
            onDeleteButtonClick(position)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showOptionsBottomSheet(position: Int) {
        val curItem =
            shopListAdapter.items[position] // Assuming `items` is the list of ShopListItem

        val bottomSheetFragment = ShopItemOptionsBottomSheetDialogFragment.newInstance(
            curItem.title,
            curItem.count,
            curItem.unit
        )

        bottomSheetFragment.listener = this
        bottomSheetFragment.position = position
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    private fun showAddRecipesDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("בחר תבשילים להוספה")

        val recipesDbHelper = RecipesDatabaseHelper();
        // Use the asynchronous getFriendsFromUsername function
        recipesDbHelper.getAllUserRecipes(username) { recipes ->
            val recipeNames = recipes.map { it.name }.toTypedArray()
            val checkedRecipes = BooleanArray(recipeNames.size) { false }

            builder.setMultiChoiceItems(
                recipeNames,
                checkedRecipes
            ) { _, which, checked ->
                checkedRecipes[which] = checked
            }

            builder.setPositiveButton("הוסף") { _, _ ->
                for (i in checkedRecipes.indices) {
                    if (checkedRecipes[i]) {
                        val selectedRecipeItems = recipes[i].items ?: emptyList()
                        shopListAdapter.items.addAll(selectedRecipeItems)
                        shopListAdapter.notifyDataSetChanged()
                    }
                }
            }

            builder.setNegativeButton("ביטול") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }
    }


    override fun onDeleteClicked(position: Int) {
        showConfirmationDialog(position)
    }

    override fun onConfirmClicked(position: Int, title: String, count: Int, unit: String) {
        val shopListItem = ShopListItem(title, count, unit, false)
        dbHelper.updateShopListItem(id, position, shopListItem)
        shopListAdapter.items[position] = shopListItem
        shopListAdapter.notifyItemChanged(position)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()

        // Save the current state and update the list in the database
        updateList(shopListAdapter.items)
    }
}