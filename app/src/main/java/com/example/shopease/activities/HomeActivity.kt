package com.example.shopease.activities

import android.os.Bundle
import com.example.shopease.R

class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        this.setUpUpperNavBar()
        this.setBottomNavBar()
    }
}
