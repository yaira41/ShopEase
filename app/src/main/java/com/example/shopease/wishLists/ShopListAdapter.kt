package com.example.shopease.wishLists

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.ShopListItem
import com.example.shopease.utils.StrikeThroughTextView

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

    fun initialList(itemsToAdd: List<ShopListItem>) {
        items.addAll(itemsToAdd)
    }

    fun addShopListItem(item: ShopListItem) {
        items.add(item)
        notifyItemInserted(items.size + 1)
    }

    private fun toggleStrikeThrough(
        tvShopListItem: StrikeThroughTextView,
        checked: Boolean,
        pencilImageView: ImageView
    ) {
        if (checked) {
            animateCrayonMark(pencilImageView, tvShopListItem)
            tvShopListItem.setStrikeThrough(true)
        } else {
            tvShopListItem.setStrikeThrough(false)
            tvShopListItem.setStrikeThroughTextFlag(false)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ShopListHolder, position: Int) {
        val curItem = items[position]
        holder.itemView.apply {
            val tvShopListItem: StrikeThroughTextView = findViewById(R.id.tvShopItemTitle)
            val cbCheckBox: CheckBox = findViewById(R.id.cbBought)
            val countItem: TextView = findViewById(R.id.tvItemCount)
            val pencilImageView: ImageView = findViewById(R.id.pencilImageView)

            tvShopListItem.text = curItem.title
            cbCheckBox.isChecked = curItem.checked
            countItem.text = "${curItem.count} ${curItem.unit}"
            tvShopListItem.setStrikeThroughTextFlag(cbCheckBox.isChecked)
            cbCheckBox.setOnCheckedChangeListener { _, checked ->
                toggleStrikeThrough(tvShopListItem, checked, pencilImageView)
                curItem.checked = !curItem.checked
            }
        }

        holder.itemView.setOnLongClickListener {
            itemLongClickListener?.onItemLongClick(position, holder.itemView)
            true // Consume the long click event
        }
    }

    private fun animateCrayonMark(pencilImageView: ImageView, textView: StrikeThroughTextView) {
        pencilImageView.visibility = View.VISIBLE

        // Get the starting position of tvShopItemTitle relative to its parent
        val tvStartX = textView.x

        // Get the ending position of tvShopItemTitle relative to its parent
        val tvEndX = tvStartX + textView.width

        // Reset the translationX to the starting position before starting a new animation
        pencilImageView.translationX = tvStartX

        // Set the pencil image dynamically
        pencilImageView.setImageResource(R.drawable.pencil)

        val movePencil = ObjectAnimator.ofFloat(pencilImageView, "translationX", tvStartX, tvEndX)
        movePencil.duration = 500 // Adjust the duration as needed

        movePencil.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            textView.setStrikeThrough(true, value - tvStartX)
        }

        movePencil.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                pencilImageView.visibility = View.GONE
            }
        })
        movePencil.start()
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
