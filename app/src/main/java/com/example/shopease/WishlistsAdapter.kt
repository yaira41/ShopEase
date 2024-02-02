package com.example.shopease

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.dataClasses.ShopListItem
import com.example.shopease.dbHelpers.ShopList

class WishlistsAdapter(
    val items: MutableList<ShopList>,
    var itemClickListener: OnItemClickListener? = null,
    var itemLongClickListener: OnItemLongClickListener? = null,
    private var parentView: View
) : RecyclerView.Adapter<WishlistsAdapter.WishListHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int, view: View)
    }

    class WishListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shopListName: TextView = itemView.findViewById(R.id.tvShopListTitle)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WishlistsAdapter.WishListHolder {
        return WishlistsAdapter.WishListHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_picker,
                parent,
                false
            )
        )
    }

    fun initialList(itemsToAdd: List<ShopList>){
        items.addAll(itemsToAdd)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear();
    }

    fun addShopList(item: ShopList) {
        items.add(item)
        notifyItemInserted(items.size + 1)
    }

    override fun onBindViewHolder(holder: WishListHolder, position: Int) {
        val shopList = items[position]

        // Bind data to views
        holder.shopListName.text = shopList.name

        // Set click listener for item
        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }

        // Set long click listener for item
        holder.itemView.setOnLongClickListener {
            itemLongClickListener?.onItemLongClick(position, parentView)
            true // Consume the long click event
        }

        // Set click listener for delete button
        holder.deleteButton.setOnClickListener {
            // Handle delete button click
            onItemDeleteButtonClick(position)
        }
    }

    private fun onItemDeleteButtonClick(position: Int) {
        // Handle delete button click
//        val selectedList = items[position]
////        Toast.makeText(
////            holder.itemView.context,
////            "Delete ${selectedList.name}",
////            Toast.LENGTH_SHORT
////        ).show()
//
//        // Remove the shop list from the list and update the adapter
//        items.removeAt(position)
//        notifyItemRemoved(position)

        // Perform deletion in the database (you need to implement this)
        // dbHelper.deleteShopList(selectedList.id)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}