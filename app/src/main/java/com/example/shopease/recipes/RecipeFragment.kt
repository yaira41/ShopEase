package com.example.shopease.recipes

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import com.example.shopease.dataClasses.Recipe
import com.example.shopease.dataClasses.ShopListItem
import com.example.shopease.dbHelpers.RecipesDatabaseHelper
import com.example.shopease.dbHelpers.RequestsDatabaseHelper
import com.example.shopease.wishLists.ShopItemOptionsBottomSheetDialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton

class RecipeFragment : Fragment(), ShopItemOptionsBottomSheetDialogFragment.BottomSheetListener {
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var id: String
    private lateinit var name: String
    private lateinit var username: String
    private lateinit var recipeName: TextView
    private lateinit var dbHelper: RecipesDatabaseHelper
    private lateinit var friendDbHelper: RequestsDatabaseHelper
    private val PERMISSION_REQUEST_CODE = 101
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recipe, container, false)
        dbHelper = RecipesDatabaseHelper()
        friendDbHelper = RequestsDatabaseHelper()
        id = arguments?.getString("RECIPE_ID_KEY") ?: ""
        name = arguments?.getString("RECIPE_NAME_KEY") ?: "New Recipe"
        username = arguments?.getString("USERNAME_KEY") ?: ""
        // Ensure correct context for permission request
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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
        recipeAdapter = RecipeAdapter(mutableListOf(),
            itemLongClickListener = object : RecipeAdapter.OnItemLongClickListener {
                override fun onItemLongClick(position: Int, view: View) {
                    showOptionsBottomSheet(position)
                }
            }
        )
        fetchData()

        val rvRecipeItem = view.findViewById<RecyclerView>(R.id.rvRecipeItems)
        rvRecipeItem.adapter = recipeAdapter
        rvRecipeItem.layoutManager = LinearLayoutManager(requireContext())
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

        val procedureEditText = view.findViewById<EditText>(R.id.etProcedure)
        procedureEditText.setText(recipeAdapter.procedure)

        addButton.setOnClickListener {
            val titleItem = itemTitle.text.toString()
            val countItem = count.text.toString()
            val unit = unitSpinner.selectedItem.toString()
            if (titleItem.isNotEmpty() and countItem.isNotEmpty()) {
                val newItem = ShopListItem(titleItem, countItem.toInt(), unit)
                recipeAdapter.addRecipeItem(newItem)
                itemTitle.text.clear()
            } else {
                showToast("נראה שחסר לך שם מוצר ואו כמות.")
            }
        }

        recipeName = view.findViewById(R.id.tvRecipeName)

        // Initially, show the TextView and hide the EditText
        recipeName.text = name

        return view;
    }

    fun updateRecipe(items: List<ShopListItem>, procedure: String) {
        dbHelper.updateRecipe(id,
            recipeName.text.toString(),
            items,
            listOf(username),
            procedure,
            object : RecipesDatabaseHelper.InsertRecipeCallback {
                override fun onRecipeInserted(recipe: Recipe?) {}
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
                    // Got the location
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Do something with the latitude and longitude, e.g., display them
                    Toast.makeText(
                        context,
                        "Latitude: $latitude, Longitude: $longitude",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "Couldn't get location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onDeleteButtonClick(position: Int) {
        if (position >= 0 && position < recipeAdapter.items.size) {
            val selectedItem = recipeAdapter.items[position]
            Toast.makeText(requireContext(), "Delete ${selectedItem.title}", Toast.LENGTH_SHORT)
                .show()

            recipeAdapter.items.removeAt(position)
            recipeAdapter.notifyItemRemoved(position)
            // Update positions of remaining items
            recipeAdapter.notifyItemRangeChanged(position, recipeAdapter.items.size)
        }
    }

    private fun fetchData() {
        dbHelper.getRecipeById(id) { items, procedure ->
            if (items.isEmpty()) {
                showToast("נראה שאין לך פריטים למתכון")
            } else {
                recipeAdapter.initialList(items)
            }
            recipeAdapter.procedure = procedure ?: ""
            val procedure2 = view?.findViewById<EditText>(R.id.etProcedure)
            procedure2?.setText(procedure)
            recipeAdapter.notifyDataSetChanged()

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
            recipeAdapter.items[position] // Assuming `items` is the list of ShopListItem

        val bottomSheetFragment = ShopItemOptionsBottomSheetDialogFragment.newInstance(
            curItem.title,
            curItem.count,
            curItem.unit
        )

        bottomSheetFragment.listener = this
        bottomSheetFragment.position = position
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    override fun onDeleteClicked(position: Int) {
        showConfirmationDialog(position)
    }

    override fun onConfirmClicked(position: Int, title: String, count: Int, unit: String) {
        val recipeItem = ShopListItem(title, count, unit, false)
        dbHelper.updateRecipeItem(id, position, recipeItem)
        recipeAdapter.items[position] = recipeItem
        recipeAdapter.notifyItemChanged(position)
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

    override fun onDestroyView() {
        super.onDestroyView()

        // Save the current state and update the list in the database
        val procedure = view?.findViewById<EditText>(R.id.etProcedure)?.text.toString()
        updateRecipe(recipeAdapter.items, procedure)
    }

    override fun onPause() {
        super.onPause()

        // Save the current state and update the list in the database
        val procedure = view?.findViewById<EditText>(R.id.etProcedure)?.text.toString()
        updateRecipe(recipeAdapter.items, procedure)
    }
}