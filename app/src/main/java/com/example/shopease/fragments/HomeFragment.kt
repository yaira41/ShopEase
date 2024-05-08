package com.example.shopease.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.shopease.activities.BaseActivity
import com.example.shopease.R
import com.example.shopease.recipes.RecipesFragment
import com.example.shopease.wishLists.WishlistsFragment
import com.google.android.material.imageview.ShapeableImageView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as BaseActivity?)?.updateTitle("בית")
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        var btnGoToScan = view.findViewById<ShapeableImageView>(R.id.btnGoToScan)
        btnGoToScan.setOnClickListener {
            navigateToFragment(BarcodeScannerFragment())
        }

        val bundle = Bundle()
        bundle.putString("USERNAME_KEY", (activity as BaseActivity?)?.username!!)

        var btnGoToMyRecipes = view.findViewById<ShapeableImageView>(R.id.btnGoToMyRecipes)
        btnGoToMyRecipes.setOnClickListener {
            navigateToFragment(RecipesFragment(), bundle)
        }

        var btnGoToMyWishlists = view.findViewById<ShapeableImageView>(R.id.btnGoToMyWishlists1)
        btnGoToMyWishlists.setOnClickListener {
            navigateToFragment(WishlistsFragment(), bundle)
            (activity as BaseActivity?)?.updateNavigationBarToWishlists()
        }

        return view
    }

    private fun navigateToFragment(fragment: Fragment, bundle: Bundle? = null) {
        (activity as BaseActivity?)?.loadFragment(fragment, bundle)
    }
}

