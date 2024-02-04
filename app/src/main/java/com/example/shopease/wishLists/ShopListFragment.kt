package com.example.shopease.wishLists

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.ShopListItem
import com.example.shopease.dbHelpers.ShopList
import com.example.shopease.dbHelpers.ShopListsDatabaseHelper

class ShopListFragment : Fragment() {
    private lateinit var shopListAdapter: ShopListAdapter
    private val shopListsDatabaseHelper = ShopListsDatabaseHelper()
    private lateinit var id: String
    private lateinit var name: String
    private lateinit var username: String
    private lateinit var dbHelper: ShopListsDatabaseHelper

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_shop_list, container, false)
        val parentLayout = view.findViewById<View>(R.id.fShopListFragment)

        dbHelper = ShopListsDatabaseHelper()
        id = arguments?.getString("SHOP_LIST_ID_KEY") ?: ""
        name = arguments?.getString("SHOP_LIST_NAME_KEY") ?: "New List"
        username = arguments?.getString("USERNAME_KEY") ?: ""
        shopListAdapter = ShopListAdapter(mutableListOf(),
            itemLongClickListener = object : ShopListAdapter.OnItemLongClickListener {
                override fun onItemLongClick(position: Int, view: View) {
                    showDeleteButton(view, position)
                }
            }, parentLayout
        )
        fetchData()

        val rvShopListItem = view.findViewById<RecyclerView>(R.id.rvShopListItems)
        rvShopListItem.adapter = shopListAdapter
        rvShopListItem.layoutManager = LinearLayoutManager(requireContext())

        val addButton = view.findViewById<Button>(R.id.bAddButton)
        val editText = view.findViewById<EditText>(R.id.etShopListTitle)
        addButton.setOnClickListener {
            val itemTitle = editText.text.toString()
            if (itemTitle.isNotEmpty()) {
                val newItem = ShopListItem(itemTitle)
                shopListAdapter.addShopListItem(newItem)
                editText.text.clear()
            }
        }

        val shopListName = view.findViewById<TextView>(R.id.tvListName)
        val editListName = view.findViewById<EditText>(R.id.etListName)

        // Initially, show the TextView and hide the EditText
        shopListName.text = name
        shopListName.visibility = View.VISIBLE
        editListName.visibility = View.GONE

        // Set an OnClickListener to switch to EditText when clicked
        shopListName.setOnClickListener {
            shopListName.visibility = View.GONE
            editListName.visibility = View.VISIBLE
            editListName.setText(shopListName.text)
            editListName.requestFocus()

            // Show the keyboard when switching to EditText
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editListName, InputMethodManager.SHOW_IMPLICIT)
        }

        editListName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // If EditText loses focus, switch back to TextView
                shopListName.text = editListName.text.toString()

                // Make TextView visible and EditText invisible
                shopListName.visibility = View.VISIBLE
                editListName.visibility = View.GONE
            }
        }

        parentLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                shopListName.text = editListName.text.toString()

                shopListName.visibility = View.VISIBLE
                editListName.visibility = View.GONE

                // Hide keyboard
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editListName.windowToken, 0)

                true
            } else {
                false
            }
        }

        val saveListButton = view.findViewById<Button>(R.id.bCreateListButton)
        saveListButton.setOnClickListener {
            if (shopListName.text.isNullOrEmpty()) {
                showToast("הכנס שם לרשימה")
            } else {
                shopListsDatabaseHelper.updateShopList(id,
                    shopListName.text.toString(),
                    shopListAdapter.items,
                    listOf(username),
                    object : ShopListsDatabaseHelper.InsertShopListCallback {
                        override fun onShopListInserted(shopList: ShopList?) {
                            if (shopList != null) {
                                showToast("Shop list updated successfully.")
                            } else {
                                showToast("Failed to update shop list")
                            }
                        }
                    }
                )

                parentFragmentManager.popBackStack();
            }
        }

        return view;
    }

    private fun showDeleteButton(view: View, position: Int) {
        val deleteButton = view.findViewById<Button>(R.id.btnDeleteItem)
        deleteButton.visibility = View.VISIBLE

        view.setOnClickListener {
            hideDeleteButton(view)
        }

        deleteButton.setOnClickListener {
            onDeleteButtonClick(position)
        }
    }

    private fun hideDeleteButton(view: View) {
        val deleteButton = view.findViewById<Button>(R.id.btnDeleteItem)
        deleteButton.visibility = View.GONE
    }

    private fun onDeleteButtonClick(position: Int) {
        val selectedItem = shopListAdapter.items[position]
        Toast.makeText(requireContext(), "Delete ${selectedItem.title}", Toast.LENGTH_SHORT).show()

        shopListAdapter.items.removeAt(position)
        shopListAdapter.notifyItemRemoved(position)
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}