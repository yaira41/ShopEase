package com.example.shopease.wishLists

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.ShopListItem
import com.example.shopease.dbHelpers.RequestsDatabaseHelper
import com.example.shopease.dbHelpers.ShopList
import com.example.shopease.dbHelpers.ShopListsDatabaseHelper

class ShopListFragment : Fragment(), ShopItemOptionsBottomSheetDialogFragment.BottomSheetListener {
    private lateinit var shopListAdapter: ShopListAdapter
    private val shopListsDatabaseHelper = ShopListsDatabaseHelper()
    private lateinit var id: String
    private lateinit var name: String
    private lateinit var username: String
    private lateinit var shopListName: TextView
    private lateinit var dbHelper: ShopListsDatabaseHelper
    private lateinit var friendDbHelper: RequestsDatabaseHelper

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
        username = arguments?.getString("USERNAME_KEY") ?: ""
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
        val addButton = view.findViewById<Button>(R.id.bAddButton)
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
            val countItem = count.text.toString().toInt()
            val unit = unitSpinner.selectedItem.toString()
            if (titleItem.isNotEmpty()) {
                val newItem = ShopListItem(titleItem, countItem, unit)
                shopListAdapter.addShopListItem(newItem)
                itemTitle.text.clear()
            }
        }

        val shareListButton = view.findViewById<Button>(R.id.sharedListButton)
        shopListName = view.findViewById(R.id.tvListName)

        shareListButton.setOnClickListener {
            showShareListDialog()
        }

        // Initially, show the TextView and hide the EditText
        shopListName.text = name

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

    private fun showShareListDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("בחר עם מי לשתף.")

        // Use the asynchronous getFriendsFromUsername function
        friendDbHelper.getFriendsFromUsername(username) { friendUsernames ->
            val checkedFriends = BooleanArray(friendUsernames.size) { false }

            builder.setMultiChoiceItems(
                friendUsernames.toTypedArray(),
                checkedFriends
            ) { _, which, isChecked ->
                checkedFriends[which] = isChecked
            }

            builder.setPositiveButton("שתף") { _, _ ->
                val selectedFriends = mutableListOf<String>()
                selectedFriends.add(username) // Add itself first
                for (i in checkedFriends.indices) {
                    if (checkedFriends[i]) {
                        selectedFriends.add(friendUsernames[i])
                    }
                }

                shareListWithFriends(selectedFriends)
            }

            builder.setNegativeButton("ביטול") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }
    }

    private fun shareListWithFriends(selectedFriends: List<String>) {
        shopListsDatabaseHelper.updateShopList(id,
            shopListName.text.toString(),
            shopListAdapter.items,
            selectedFriends,
            object : ShopListsDatabaseHelper.InsertShopListCallback {
                override fun onShopListInserted(shopList: ShopList?) {
                    if (shopList != null) {
                        showToast("הרשימה שותפה בהצלחה.")
                    } else {
                        showToast("משהו השתבש.")
                    }
                }
            })
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

    override fun onDeleteClicked(position: Int) {
        showConfirmationDialog(position)
    }

    override fun onConfirmClicked(position: Int, title: String, count: Int, unit: String) {
        val shopListItem = ShopListItem(title, count, unit, false)
        dbHelper.updateShopListItem(id, position, shopListItem)
        shopListAdapter.items[position] = shopListItem
        shopListAdapter.notifyItemChanged(position)
    }
}