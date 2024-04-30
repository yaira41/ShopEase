package com.example.shopease.recipes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.Recipe

class RecipesAdapter(
    val items: MutableList<Recipe>,
    var itemClickListener: OnItemClickListener? = null,
    var itemLongClickListener: OnItemLongClickListener? = null,
    private var parentView: View
) : RecyclerView.Adapter<RecipesAdapter.RecipesHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int, view: View)
    }

    class RecipesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeName: TextView = itemView.findViewById(R.id.tvShopListTitle)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecipesHolder {
        return RecipesHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_picker,
                parent,
                false
            )
        )
    }

    fun initialList(itemsToAdd: List<Recipe>) {
        items.addAll(itemsToAdd)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear();
    }

    fun addRecipe(item: Recipe) {
        items.add(item)
        notifyItemInserted(items.size + 1)
        notifyItemChanged(items.size + 1)
    }

    override fun onBindViewHolder(holder: RecipesHolder, position: Int) {
        val recipeItems = items[position]

        // Bind data to views
        holder.recipeName.text = recipeItems.name

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