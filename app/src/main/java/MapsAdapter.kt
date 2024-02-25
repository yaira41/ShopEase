//package com.example.shopease.adapter
//
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.AsyncListDiffer
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.RecyclerView
//import com.example.shopease.adapter.MapsAdapter.MapsViewHolder
//import com.example.shopease.databinding.ItemViewBinding
//import com.example.shopease.models.UserMap
//
//class MapsAdapter() : RecyclerView.Adapter<MapsViewHolder>() {
//
//    inner class MapsViewHolder(val binding: ItemViewBinding) : RecyclerView.ViewHolder(binding.root)
//
//    private val differCallback = object: DiffUtil.ItemCallback<UserMap>() {
//        override fun areItemsTheSame(oldItem: UserMap, newItem: UserMap): Boolean {
//            return oldItem.title == newItem.title
//        }
//
//        override fun areContentsTheSame(oldItem: UserMap, newItem: UserMap): Boolean {
//            return oldItem == newItem
//        }
//
//    }
//
//    private val differ = AsyncListDiffer(this, differCallback)
//
//    var map: List<UserMap>
//        get() = differ.currentList
//        set(value) {differ.submitList(value)}
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapsViewHolder {
//        return MapsViewHolder(
//            ItemViewBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun onBindViewHolder(holderMaps: MapsViewHolder, position: Int) {
//        holderMaps.binding.tvUserMap.apply {
//            val userMap = map[position]
//            text = userMap.title
//
//            setOnClickListener {
//                val intent = Intent(context, MapsActivity::class.java)
//                intent.put(_USER_MAP, userMap)
//                context.startActivity(intent)
//            }
//        }
//
//    }
//
//    override fun getItemCount() = map.size
//}