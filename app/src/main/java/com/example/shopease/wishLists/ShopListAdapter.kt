package com.example.shopease.wishLists

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.ShopListItem

class ShopListAdapter(
    val items: MutableList<ShopListItem>,
    var itemLongClickListener: OnItemLongClickListener? = null
) : RecyclerView.Adapter<ShopListAdapter.ShopListHolder>() {

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int, view: View)
    }

    class ShopListHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopListHolder {
        return ShopListHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.shop_list_item,
                parent,
                false
            )
        )
    }

    fun initialList(itemsToAdd: List<ShopListItem>){
        items.addAll(itemsToAdd)
    }

    fun addShopListItem(item: ShopListItem) {
        items.add(item)
        notifyItemInserted(items.size + 1)
    }

    private fun toggleStrikeThrough(tvShopListItem: TextView, isChecked: Boolean) {
        if (isChecked){
            tvShopListItem.paintFlags = tvShopListItem.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tvShopListItem.paintFlags = tvShopListItem.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun onBindViewHolder(holder: ShopListHolder, position: Int) {
        val curItem = items[position]
        holder.itemView.apply {
            val tvShopListItem: TextView = findViewById(R.id.tvShopItemTitle)
            val cbCheckBox: CheckBox = findViewById(R.id.cbBought)

            tvShopListItem.text = curItem.title
            cbCheckBox.isChecked = curItem.isChecked
            toggleStrikeThrough(tvShopListItem, cbCheckBox.isChecked)
            cbCheckBox.setOnCheckedChangeListener{_, isChecked ->
                toggleStrikeThrough(tvShopListItem, isChecked)
                curItem.isChecked = !curItem.isChecked
            }
        }

        holder.itemView.setOnLongClickListener {
            itemLongClickListener?.onItemLongClick(position, holder.itemView)
            true // Consume the long click event
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
