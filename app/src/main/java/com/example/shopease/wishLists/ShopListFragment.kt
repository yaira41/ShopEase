package com.example.shopease.wishLists

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
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
import com.example.shopease.dbHelpers.ShopListsDatabaseHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import java.util.*

class ShopListFragment : Fragment(), ShopItemOptionsBottomSheetDialogFragment.BottomSheetListener {
    private lateinit var shopListAdapter: ShopListAdapter
    private val shopListsDatabaseHelper = ShopListsDatabaseHelper()
    private lateinit var id: String
    private lateinit var shopList: ShopList
    private lateinit var username: String
    private lateinit var shopListName: TextView
    private val PERMISSION_REQUEST_CODE = 101
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var geocoder: Geocoder

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop_list, container, false)
        id = arguments?.getString("SHOP_LIST_ID_KEY") ?: ""
        username = arguments?.getString("USERNAME_KEY") ?: ""

        shopListsDatabaseHelper.getShopListById(id) { sL ->
            if (sL != null) {
                shopList = sL
                initializeUI(view)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geocoder = Geocoder(requireActivity(), Locale.getDefault())
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun initializeUI(view: View) {

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
        val addButton = view.findViewById<ImageButton>(R.id.bAddButton)
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
            } else {
                showToast("נראה שחסר לך שם מוצר ואו כמות.")
            }
        }
        val chooseLocationButton = view.findViewById<MaterialButton>(R.id.button4)
        chooseLocationButton.setOnClickListener {
            openLocationPickerOrAddressInput()
        }

        val addRecipesButton = view.findViewById<MaterialButton>(R.id.bAddRecipes)
        addRecipesButton.setOnClickListener {
            showAddRecipesDialog()
        }

        shopListName = view.findViewById(R.id.tvListName)
        shopListName.text = shopList.name
    }

    private fun openLocationPickerOrAddressInput() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Location Method")
        builder.setItems(arrayOf("Use Current Location", "Enter Address")) { _, which ->
            when (which) {
                0 -> {
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
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
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
            .setPositiveButton("OK") { _, _ ->
                val addressString = editText.text.toString()
                if (addressString.isNotEmpty()) {
                    launchAddressInput(addressString)
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

    private fun launchAddressInput(addressString: String) {
        try {
            val addressList = geocoder.getFromLocationName(addressString, 1)
            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                shopList.latitude = address.latitude
                shopList.longitude = address.longitude
                Toast.makeText(
                    requireContext(),
                    "Latitude: ${shopList.latitude}, Longitude: ${shopList.longitude}",
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

    private fun updateList(items: List<ShopListItem>) {
        shopListsDatabaseHelper.updateShopList(id,
            shopListName.text.toString(),
            items,
            shopList.members,
            shopList.latitude,
            shopList.longitude,
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
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    shopList.latitude = location.latitude
                    shopList.longitude = location.longitude
                    Toast.makeText(
                        context,
                        "Latitude: ${shopList.latitude}, Longitude: ${shopList.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
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
        shopListsDatabaseHelper.getListItemsById(id) { items ->
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
            shopListAdapter.items[position]

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
        shopListsDatabaseHelper.updateShopListItem(id, position, shopListItem)
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
        updateList(shopListAdapter.items)
    }
}