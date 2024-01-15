package com.example.shopease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class WishlistsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as BaseActivity?)?.updateTitle("Wishlists")
        val view = inflater.inflate(R.layout.fragment_wishlists, container, false)

        val button = view.findViewById<Button>(R.id.bMoveToMyShopList);
        button.setOnClickListener {
            replaceWithNewFragment()
        }

        return view;
    }

    private fun replaceWithNewFragment() {
        val newFragment = ShopListFragment()

        parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, newFragment)
                .addToBackStack(null).commit()
    }
}
