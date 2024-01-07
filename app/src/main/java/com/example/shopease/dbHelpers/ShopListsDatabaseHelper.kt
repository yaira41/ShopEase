package com.example.shopease.dbHelpers
import com.google.firebase.database.*

class ShopListsDatabaseHelper : BaseDatabaseHelper() {

    fun addShopList(name: String, items: List<String>, callback: (Boolean) -> Unit) {
        val shopListId = databaseReference.child("shopLists").push().key
        val shopListRef = databaseReference.child("shopLists").child(shopListId ?: "")
        val shopList = ShopList(shopListId, name, items)

        shopListRef.setValue(shopList)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
//                    Log.d("FirebaseHelper", "Shop list added successfully. ShopListId: $shopListId")
                    callback(true)
                } else {
//                    Log.e("FirebaseHelper", "Error adding shop list", task.exception)
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
//                Log.e("FirebaseHelper", "Failure adding shop list", exception)
                callback(false)
            }
    }

    // Add other methods for shop list operations as needed

    // Example data model for a shop list
    data class ShopList(
        val shopListId: String? = null,
        val name: String,
        val items: List<String>,
        // Add other properties as needed
    )
}