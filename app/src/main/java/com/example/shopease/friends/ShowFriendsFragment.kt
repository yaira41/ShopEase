package com.example.shopease.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.fragment.app.Fragment
import com.example.shopease.BaseActivity
import com.example.shopease.R
import com.example.shopease.dataClasses.FriendInfo
import com.example.shopease.dbHelpers.RequestsDatabaseHelper
import com.example.shopease.utils.Utils.byteArrayToBitmap

class ShowFriendsFragment : Fragment() {

    private lateinit var gridLayout: GridLayout
    private lateinit var username: String
    private val requestsDatabaseHelper = RequestsDatabaseHelper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_friends, container, false)
        gridLayout = view.findViewById(R.id.gridLayoutFriends)

        // Add a top margin of 50dp to the GridLayout
        val params = gridLayout.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = resources.getDimensionPixelSize(R.dimen.grid_top_margin)

        username = (activity as BaseActivity).username!!
        requestsDatabaseHelper.getFriendsWithImages(username) { friends ->
            updateGridLayout(friends)
        }
        return view
    }

    private fun calculateRowCount(friendCount: Int, itemsPerRow: Int): Int {
        return (friendCount + itemsPerRow - 1) / itemsPerRow
    }

    private fun updateGridLayout(friends: List<FriendInfo>) {
        // Calculate row count based on the number of friends
        val rowCount = calculateRowCount(friends.size, 3)

        // Update GridLayout
        gridLayout.rowCount = rowCount
        gridLayout.removeAllViews() // Clear existing views

        for (friend in friends) {
            val friendView = layoutInflater.inflate(R.layout.item_show_friend, null)
            val binding = ItemFriendBinding.bind(friendView)

            // Log the friend information
            Log.d("FriendInfo", "Username: ${friend.username}")
            Log.d("FriendInfo", "Image ByteArray: ${friend.imageProfileByteArray}")

            // Set username and image profile to friendView
            binding.textViewUsername.text = friend.username
            friend.imageProfileByteArray?.let { byteArray ->
                val bitmap = byteArrayToBitmap(byteArray)
                binding.imageViewFriend.setImageBitmap(bitmap)
            }

            // Add friendView to the GridLayout
            val params = GridLayout.LayoutParams()
            params.width = GridLayout.LayoutParams.WRAP_CONTENT
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL)
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL)
            friendView.layoutParams = params

            gridLayout.addView(friendView)
            Log.d("GridLayout", "Added friendView to GridLayout")
        }
    }
}
