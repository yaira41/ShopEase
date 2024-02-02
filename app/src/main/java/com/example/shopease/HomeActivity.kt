package com.example.shopease

import android.os.Bundle

class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        this.setUpUpperNavBar()
        this.setBottomNavBar()
    }
}
