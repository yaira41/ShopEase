package com.example.shopease.wishLists

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

        wishlistsAdapter = WishlistsAdapter(shopLists,
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
                    showDeleteButton(view, position)
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

    private fun fetchUserLists(userName : String) {
        dbHelper.getAllUserLists(userName) { allLists ->
            wishlistsAdapter.clear()
            if (allLists.isEmpty()) {
                Toast.makeText(requireContext(), "נראה שאין לך רשימות", Toast.LENGTH_SHORT).show()
            } else {
                wishlistsAdapter.initialList(allLists)
            }
        }
    }

    private fun showDeleteButton(view: View, position: Int) {
        val deleteButton = view.findViewById<Button>(R.id.btnDelete)
        deleteButton.visibility = View.VISIBLE

        view.setOnClickListener {
            hideDeleteButton(view)
        }

        deleteButton.setOnClickListener {
            onDeleteButtonClick(position)
        }
    }

    private fun hideDeleteButton(view: View) {
        val deleteButton = view.findViewById<Button>(R.id.btnDelete)
        deleteButton.visibility = View.GONE
    }

    private fun onDeleteButtonClick(position: Int) {
        val selectedList = shopLists[position]
        Toast.makeText(requireContext(), "Delete ${selectedList.name}", Toast.LENGTH_SHORT).show()
        if(!selectedList.id.isNullOrEmpty()){
            dbHelper.deleteShopListForSpecificUser(selectedList.id, username)
        }

        shopLists.removeAt(position)
        wishlistsAdapter.notifyItemRemoved(position)
    }

    private fun replaceWithNewFragment(newFragment : Fragment, args: Bundle? = null) {
        newFragment.arguments = args

        parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, newFragment)
            .addToBackStack(null).commit()
    }

    private fun showCreateListDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("שם רשימה")

        val input = EditText(requireContext())
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

        // Move builder.show() here
        builder.show()
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}
