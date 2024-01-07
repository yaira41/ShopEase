package com.example.shopease.dbHelpers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseManager {
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
}

open class BaseDatabaseHelper {
    protected val databaseReference: DatabaseReference = FirebaseManager.databaseReference
    protected val auth: FirebaseAuth = FirebaseManager.auth
}