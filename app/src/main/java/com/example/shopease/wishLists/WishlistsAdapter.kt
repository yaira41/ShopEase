package com.example.shopease.wishLists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.ShopList

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
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WishListHolder {
        return WishListHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_picker,
                parent,
                false
            )
        )
    }

    fun initialList(itemsToAdd: List<ShopList>) {
        items.addAll(itemsToAdd)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear();
    }

    fun addShopList(item: ShopList) {
        items.add(item)
        notifyItemInserted(items.size + 1)
        notifyItemChanged(items.size + 1)
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
    }

    override fun getItemCount(): Int {
        return items.size
    }
}