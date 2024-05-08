package com.example.shopease.friends

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import com.example.shopease.activities.BaseActivity
import com.example.shopease.R
import com.example.shopease.dataClasses.FriendInfo
import com.example.shopease.dbHelpers.RequestsDatabaseHelper
import com.example.shopease.utils.Utils.byteArrayToBitmap
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

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
        gridLayout = view.findViewById(R.id.gridLayoutShowFriends)

        // Add a top margin of 50dp to the GridLayout
        val params = gridLayout.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = resources.getDimensionPixelSize(R.dimen.grid_top_margin)

        username = (activity as BaseActivity).user?.username!!
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

            // Set username and image profile to friendView
            binding.textViewUsername.text = friend.username
            friend.imageProfileByteArray?.let { byteArray ->
                val bitmap = byteArrayToBitmap(byteArray)
                binding.imageViewFriend.setImageBitmap(bitmap)
            }

            binding.imageViewFriend.setOnClickListener {
                showFriendDetailsDialog(friend)
            }

            // Add friendView to the GridLayout
            val params = GridLayout.LayoutParams()
            params.width = GridLayout.LayoutParams.WRAP_CONTENT
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL)
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL)
            friendView.layoutParams = params

            gridLayout.addView(friendView)
        }
    }

    private fun showFriendDetailsDialog(friend: FriendInfo) {
        requestsDatabaseHelper.getUserByUsername(friend.username) { user ->
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_show_user_detail)

            val imageViewFriendDetails =
                dialog.findViewById<ShapeableImageView>(R.id.imageShowDialogViewProfile)
            val textViewUsernameDetails =
                dialog.findViewById<TextView>(R.id.textShowDialogViewUsername)
            val textViewOtherDetails = dialog.findViewById<TextView>(R.id.textShowDialogViewEmail)

            friend.imageProfileByteArray?.let { byteArray ->
                val bitmap = byteArrayToBitmap(byteArray)
                imageViewFriendDetails.setImageBitmap(bitmap)
            }
            textViewUsernameDetails.text = friend.username
            textViewOtherDetails.text = user?.email

            val buttonClose = dialog.findViewById<MaterialButton>(R.id.btnShowDialogClose)
            buttonClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

}
