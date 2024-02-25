package com.example.shopease

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class ShopEase : Application() {
    override fun onCreate() {
        super.onCreate()

        // Enable offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
