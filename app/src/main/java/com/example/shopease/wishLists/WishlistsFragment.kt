package com.example.shopease.wishLists

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.BaseActivity
import com.example.shopease.R
import com.example.shopease.dbHelpers.ShopList
import com.example.shopease.dbHelpers.ShopListsDatabaseHelper
import com.example.shopease.wishLists.ShopListFragment
import com.example.shopease.wishLists.WishlistsAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WishlistsFragment : Fragment() {

    private val shopLists: MutableList<ShopList> = mutableListOf()
    private lateinit var wishlistsAdapter: WishlistsAdapter
    private lateinit var username: String
    private lateinit var dbHelper: ShopListsDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as BaseActivity?)?.updateTitle("Wishlists")
        val view = inflater.inflate(R.layout.fragment_wishlists, container, false)
        username = arguments?.getString("USERNAME_KEY") ?: ""
        dbHelper = ShopListsDatabaseHelper()

// Add a long-press listener in your adapter
        wishlistsAdapter = WishlistsAdapter(
            shopLists,
            itemClickListener = object : WishlistsAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val selectedList = shopLists[position]
                    val bundle = Bundle()
                    bundle.putString("SHOP_LIST_ID_KEY", selectedList.id)
                    bundle.putString("SHOP_LIST_NAME_KEY", selectedList.name)
                    bundle.putString("USERNAME_KEY", username)
                    replaceWithNewFragment(ShopListFragment(), bundle)
                }
            },
            itemLongClickListener = object : WishlistsAdapter.OnItemLongClickListener {
                override fun onItemLongClick(position: Int, view: View) {
                    // Show the update dialog on long press
                    showUpdateItemDialog(shopLists[position], position)
                }
            },
            view
        )


        fetchUserLists(username)

        val fab = view.findViewById<FloatingActionButton>(R.id.bMoveToMyShopList)
        fab.setOnClickListener {
            showCreateListDialog()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvShopLists = view.findViewById<RecyclerView>(R.id.rvAllLists)
        rvShopLists.adapter = wishlistsAdapter
        rvShopLists.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showUpdateItemDialog(selectedList: ShopList, position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.update_item_dialog, null)
        val editText = dialogView.findViewById<EditText>(R.id.changeWishlistName)
        val confirmButton = dialogView.findViewById<Button>(R.id.updateWishlistName)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteWishlistButton)

        editText.setText(selectedList.name)

        builder.setView(dialogView)
        val alertDialog = builder.create()

        confirmButton.setOnClickListener {
            val updatedName = editText.text.toString()
            if (updatedName.isNotEmpty()) {
                shopLists[position].name = updatedName
                val id = shopLists[position].id
                dbHelper.updateWishlistName(id!!, updatedName)
                wishlistsAdapter.notifyItemChanged(position)
            }
            alertDialog.dismiss()
        }

        deleteButton.setOnClickListener {
            showConfirmationDialog(position)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun fetchUserLists(userName: String) {
        dbHelper.getAllUserLists(userName) { allLists ->
            wishlistsAdapter.clear()
            if (allLists.isEmpty()) {
                Toast.makeText(requireContext(), "נראה שאין לך רשימות", Toast.LENGTH_SHORT).show()
            } else {
                wishlistsAdapter.initialList(allLists)
            }
        }
    }

    private fun onDeleteButtonClick(position: Int) {
        val selectedList = shopLists[position]
        Toast.makeText(requireContext(), "Delete ${selectedList.name}", Toast.LENGTH_SHORT).show()
        if (!selectedList.id.isNullOrEmpty()) {
            dbHelper.deleteShopListForSpecificUser(selectedList.id, username)
        }

        // Remove the item from the data source
        shopLists.removeAt(position)
        // Notify the adapter about the item removal
        wishlistsAdapter.notifyItemRemoved(position)

        // Update the positions in the adapter for items after the deleted one
        for (i in position until shopLists.size) {
            wishlistsAdapter.notifyItemChanged(i)
        }
    }

    private fun replaceWithNewFragment(newFragment: Fragment, args: Bundle? = null) {
        newFragment.arguments = args

        parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, newFragment)
            .addToBackStack(null).commit()
    }

    private fun showCreateListDialog() {
        val context = context ?: return  // Check if the fragment is attached to a context
        val builder = AlertDialog.Builder(context)
        builder.setTitle("שם רשימה")

        val input = EditText(context)
        builder.setView(input)

        builder.setPositiveButton("צור") { _, _ ->
            val listName = input.text.toString()
            if (listName.isEmpty()) {
                showToast("הכנס שם לרשימה")
            } else {
                dbHelper.insertNewList(
                    listName,
                    null,
                    listOf(username),
                    object : ShopListsDatabaseHelper.InsertShopListCallback {
                        override fun onShopListInserted(shopList: ShopList?) {
                            if (shopList != null) {
                                showToast("הרשימה נוצרה בהצלחה.")
                                fetchUserLists(username)
                            } else {
                                showToast("משהו השתבש.")
                            }
                        }
                    }
                )
            }
        }

        builder.setNegativeButton("בטל") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showToast(message: String) {
        val context = context
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    private fun showConfirmationDialog(position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        val confirmButton: Button = dialogView.findViewById(R.id.btnConfirmDelete)
        val cancelButton: Button = dialogView.findViewById(R.id.btnCancelDelete)

        confirmButton.setOnClickListener {
            onDeleteButtonClick(position)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
