package com.example.shopease

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShopListFragment : Fragment() {
    private lateinit var shopListAdapter: ShopListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_shop_list, container, false)

        shopListAdapter = ShopListAdapter(mutableListOf())

        val rvShopListItem = view.findViewById<RecyclerView>(R.id.rvShopListItems)
        rvShopListItem.adapter = shopListAdapter
        rvShopListItem.layoutManager = LinearLayoutManager(requireContext())

        val addButton = view.findViewById<Button>(R.id.bAddButton)
        val editText = view.findViewById<EditText>(R.id.etShopListTitle)
        addButton.setOnClickListener {
            val itemTitle = editText.text.toString()
            if(itemTitle.isNotEmpty()){
                val newItem = ShopListItem(itemTitle)
                shopListAdapter.addShopListItem(newItem)
                editText.text.clear()
            }
        }

        return view;
    }
}