package com.example.shopease

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity(), InterfaceFragmentTitle {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // Inflate the custom upper navigation bar layout
        val inflater = LayoutInflater.from(this)
        val customUpperNavBar = inflater.inflate(R.layout.upper_nav_bar, null)

        // Set custom upper navigation bar as the support action bar
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.customView = customUpperNavBar
        val btnBack: ImageButton = customUpperNavBar.findViewById(R.id.btnBack)
        val title: TextView = customUpperNavBar.findViewById(R.id.title)
        val btnProfile: ImageButton = customUpperNavBar.findViewById(R.id.btnProfile)

        // Set button click listeners or handle them as needed
        btnBack.setOnClickListener {
            finish()
        }

        btnProfile.setOnClickListener {
            loadFragment(ProfileFragment())
        }

        // Set the title
        title.text = "Your Fragment Title"
        bottomNavigation = findViewById(R.id.bottomNavigation)

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_wishlist -> {
                    loadFragment(WishlistsFragment())
                    true
                }

                R.id.action_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.action_friends -> {
                    loadFragment(FriendsFragment())
                    true
                }

                else -> false
            }
        }

        // Load the initial fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    override fun updateTitle(title: String) {
        // Update the title in your custom upper navigation bar
        val fragmentTitle: TextView = findViewById(R.id.title)
        fragmentTitle.text = title
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
