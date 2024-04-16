package com.example.shopease.dbHelpers

import android.util.Log
import com.example.shopease.dataClasses.ShopList
import com.example.shopease.dataClasses.ShopListItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

interface CheckProductExistenceCallback {
    fun onProductExistenceChecked(exists: Boolean)
}

class ShopListsDatabaseHelper : BaseDatabaseHelper() {

    interface InsertShopListCallback {
        fun onShopListInserted(shopList: ShopList?)
    }

    fun getAllUserLists(userName: String, listener: (List<ShopList>) -> Unit) {
        val query =
            databaseReference.child("shopLists")

        query.keepSynced(true)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lists: MutableList<ShopList> = mutableListOf()

                for (listSnapshot in dataSnapshot.children) {
                    val membersList: MutableList<String> = mutableListOf()
                    val membersSnapshot = listSnapshot.child("members")
                    for (memberSnapshot in membersSnapshot.children) {
                        val member = memberSnapshot.getValue(String::class.java)
                        member?.let { membersList.add(it) }
                    }

                    if (userName in membersList) {
                        val id = listSnapshot.child("id").getValue(String::class.java)
                        val name = listSnapshot.child("name").getValue(String::class.java) ?: "list"
                        val longitude = listSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                        val latitude = listSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0

                        val itemsList: MutableList<ShopListItem> = mutableListOf()
                        val itemsSnapshot = listSnapshot.child("items")
                        for (itemSnapshot in itemsSnapshot.children) {
                            itemsList.add(itemSnapshot.getValue(ShopListItem::class.java)!!)
                        }

                        val shopList = ShopList(id, name, itemsList, membersList, latitude, longitude)
                        lists.add(shopList)
                    }
                }

                if (lists.isEmpty()) {
                    listener(emptyList())
                } else {
                    listener(lists)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ShopListsDatabaseHelper", "Error getting user lists", error.toException())
                listener(emptyList())
            }
        })
    }

    fun addProductToList(listId: String, product: String, countItem: Int, unit: String) {
        val shopListRef = databaseReference.child("shopLists").child(listId).child("items")
        val newItemRef = shopListRef.push()
        val productEntry = mapOf(
            "title" to product,
            "count" to countItem,
            "unit" to unit,
            "checked" to false
        )
        newItemRef.setValue(productEntry)
    }

    fun getListById(id: String, listener: (List<ShopListItem>) -> Unit) {
        databaseReference.child("shopLists").child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val itemsList: MutableList<ShopListItem> = mutableListOf()
                    val itemsSnapshot = dataSnapshot.child("items")
                    for (itemSnapshot in itemsSnapshot.children) {
                        itemsList.add(itemSnapshot.getValue(ShopListItem::class.java)!!)
                    }

                    if (itemsList.isEmpty()) {
                        listener(emptyList())
                    } else {
                        listener(itemsList)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "ShopListFirebaseHelper",
                        "Error getting all lists by username",
                        error.toException()
                    )
                    listener(emptyList())
                }
            })
    }

    fun updateShopList(
        listId: String,
        listName: String,
        items: List<ShopListItem>,
        members: List<String>,
        latitude: Double,
        longitude: Double,
        listener: InsertShopListCallback
    ) {
        val shopListRef = databaseReference.child("shopLists").child(listId)
        shopListRef.keepSynced(true)
        val updatedList = ShopList(listId, listName, items, members, latitude, longitude)

        shopListRef.setValue(updatedList)
            .addOnSuccessListener {
                listener.onShopListInserted(updatedList)
            }
            .addOnFailureListener { exception ->
                Log.e("ShopListFirebaseHelper", "Error updating shop list", exception.cause)
            }
    }

    fun updateWishlistName(shopListId: String, newName: String) {
        databaseReference.child("shopLists").child(shopListId).child("name").setValue(newName)
    }

    fun deleteShopListForSpecificUser(shopListId: String, member: String) {
        val shopListRef = databaseReference.child("shopLists").child(shopListId)

        shopListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val membersList: MutableList<String> = mutableListOf()
                val membersSnapshot = dataSnapshot.child("members")
                for (memberSnapshot in membersSnapshot.children) {
                    val existingMember = memberSnapshot.getValue(String::class.java)
                    existingMember?.let { membersList.add(it) }
                }

                membersList.remove(member)

                if (membersList.isEmpty()) {
                    shopListRef.removeValue()
                } else {
                    shopListRef.child("members").setValue(membersList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "ShopListFirebaseHelper",
                    "Error deleting shop list for specific user",
                    error.toException()
                )
            }
        })
    }

    fun insertNewList(
        listName: String,
        items: List<ShopListItem>?,
        members: List<String>,
        listener: InsertShopListCallback
    ) {
        val shopListsRef = databaseReference.child("shopLists")
        val newShopListRef = shopListsRef.push()

        val newList = ShopList(newShopListRef.key, listName, items, members)

        newShopListRef.setValue(newList)
            .addOnSuccessListener {
                listener.onShopListInserted(newList)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "ShopListFirebaseHelper",
                    "Error getting all lists by username",
                    exception.cause
                )
            }
    }

    fun updateShopListItem(listId: String, position: Int, updatedItem: ShopListItem) {
        val shopListRef = databaseReference.child("shopLists").child(listId).child("items")
        shopListRef.keepSynced(true)

        shopListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentItems: MutableList<ShopListItem>? =
                    dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<ShopListItem>>() {})

                if (currentItems != null) {
                    if (position >= 0 && position < currentItems.size) {
                        currentItems[position] = updatedItem
                    }
                    shopListRef.setValue(currentItems)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ShopListsDatabaseHelper", "Error updating shop list item", error.toException())
            }
        })
    }
}