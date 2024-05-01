package com.example.shopease

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class ShopEase : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
