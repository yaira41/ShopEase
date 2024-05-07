package com.example.shopease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.shopease.dataClasses.User
import com.example.shopease.friends.FriendsFragment
import com.example.shopease.viewModels.UserViewModel
import com.example.shopease.wishLists.WishlistsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity(), InterfaceFragmentTitle {
    private lateinit var bottomNavigation: BottomNavigationView
    private val userViewModel: UserViewModel by viewModels()

    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras()
    }

    internal fun setUpUpperNavBar() {
        val inflater = LayoutInflater.from(this)
        val customUpperNavBar = inflater.inflate(R.layout.upper_nav_bar, null)

        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.customView = customUpperNavBar
    }

    internal fun handleIntentExtras() {
        userViewModel.user?.let { userObj ->
            user = userObj
        }

    }

    internal fun setBottomNavBar() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.action_wishlist -> {
                    val bundle = Bundle()
                    bundle.putString("USERNAME_KEY", user?.username)
                    loadFragment(WishlistsFragment(), bundle)
                    true
                }

                R.id.action_home -> {
                    val bundle = Bundle()
                    bundle.putString("USERNAME_KEY", user?.username)
                    loadFragment(HomeFragment(), bundle)
                    true
                }

                R.id.action_friends -> {
                    loadFragment(FriendsFragment())
                    true
                }

                R.id.action_saved_place -> {
                    val bundle = Bundle()
                    bundle.putString("USERNAME_KEY", user?.username)

                    loadFragment(SavedPlaceFragment(), bundle)
                    true
                }

                else -> false
            }
        }

        // Set the "Home" item as the default selected item
        bottomNavigation.selectedItemId = R.id.action_home
    }

    fun updateNavigationBarToWishlists() {
        bottomNavigation.selectedItemId = R.id.action_wishlist
    }

    override fun updateTitle(title: String) {
        // Update the title in your custom upper navigation bar
        val fragmentTitle: TextView = findViewById(R.id.title)
        fragmentTitle.text = title
    }

    fun loadFragment(fragment: Fragment, args: Bundle? = null, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()

        // Set arguments if provided
        fragment.arguments = args

        // Replace the existing fragment with the new one
        transaction.replace(R.id.fragmentContainer, fragment)

        // Add to back stack if needed
        if (addToBackStack) {
            transaction.addToBackStack(null)
                .setReorderingAllowed(true)
        }

        // Commit the transaction
        transaction.commit()
    }

    fun onProfileButtonClick(view: View) {
        val profileFragment = ProfileFragment()
        // Create a Bundle and add data to it
        val bundle = Bundle()
        bundle.putString("USERNAME_KEY", user?.username)
        bundle.putString("EMAIL_KEY", user?.email)
        bundle.putString("PROFILE_IMAGE_KEY", user?.profileImage)
        loadFragment(profileFragment, bundle)
    }

    fun onBackButtonClick(view: View) {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}
