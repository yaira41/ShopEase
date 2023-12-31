package com.example.shopease

import ProfileFragment
import ShopListAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity(), InterfaceFragmentTitle {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var shopListAdapter: ShopListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // Inflate the custom upper navigation bar layout
        val inflater = LayoutInflater.from(this)
        val customUpperNavBar = inflater.inflate(R.layout.upper_nav_bar, null)

        // Set custom upper navigation bar as the support action bar
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.customView = customUpperNavBar

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

        // Set the "Home" item as the default selected item
        bottomNavigation.selectedItemId = R.id.action_home

        shopListAdapter = ShopListAdapter(mutableListOf())

        val rvShopListItem = findViewById<RecyclerView>(R.id.rvShopListItems)
        rvShopListItem.adapter = shopListAdapter
        rvShopListItem.layoutManager = LinearLayoutManager(this)



        val addButton = findViewById<Button>(R.id.bAddButton)
        val editText = findViewById<EditText>(R.id.etShopListTitle)
        addButton.setOnClickListener {
            val itemTitle = editText.text.toString()
            if(itemTitle.isNotEmpty()){
                val newItem = ShopListItem(itemTitle)
                shopListAdapter.addShopListItem(newItem)
                editText.text.clear()
            }
        }
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
        }

        // Commit the transaction
        transaction.commit()
    }

    fun onProfileButtonClick(view: View) {
        val profileFragment = ProfileFragment()
        val username = intent.getStringExtra("USERNAME_KEY")
        val email = intent.getStringExtra("EMAIL_KEY")
        // Create a Bundle and add data to it
        val bundle = Bundle()
        bundle.putString("USERNAME_KEY", username)
        bundle.putString("EMAIL_KEY", email)

        loadFragment(profileFragment, bundle)
    }

    fun onMyShopListButtonClick() {
        loadFragment(ShopListFragment())
    }

    fun onBackButtonClick() {
        finish()
    }
}
