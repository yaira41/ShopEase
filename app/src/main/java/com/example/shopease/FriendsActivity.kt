package com.example.shopease

import android.os.Bundle

class FriendsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)
        this.setNavBars()
    }
}