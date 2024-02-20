package com.example.shopease.dbHelpers

import android.util.Log
import com.example.shopease.dataClasses.ShopListItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

interface CheckProductExistenceCallback {
    fun onProductExistenceChecked(exists: Boolean)
}

class ShopListsDatabaseHelper : BaseDatabaseHelper() {
//    init {
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
//    }

    interface InsertShopListCallback {
        fun onShopListInserted(shopList: ShopList?)
    }

    fun getAllUserLists(userName: String, listener: (List<ShopList>) -> Unit) {
        val query =
            databaseReference.child("shopLists")

        // Enable offline persistence for the query
        query.keepSynced(true)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lists: MutableList<ShopList> = mutableListOf()

                for (listSnapshot in dataSnapshot.children) {
                    // Extracting members
                    val membersList: MutableList<String> = mutableListOf()
                    val membersSnapshot = listSnapshot.child("members")
                    for (memberSnapshot in membersSnapshot.children) {
                        val member = memberSnapshot.getValue(String::class.java)
                        member?.let { membersList.add(it) }
                    }

                    if (userName in membersList) {
                        // Convert each list snapshot to ListObject
                        val id = listSnapshot.child("id").getValue(String::class.java)
                        val name = listSnapshot.child("name").getValue(String::class.java) ?: "list"

                        // Extracting items
                        val itemsList: MutableList<ShopListItem> = mutableListOf()
                        val itemsSnapshot = listSnapshot.child("items")
                        for (itemSnapshot in itemsSnapshot.children) {
                            val itemTitle =
                                itemSnapshot.child("title").getValue(String::class.java) ?: "asd"
                            val itemState =
                                itemSnapshot.child("isChecked").getValue(Boolean::class.java) ?: false
                            val itemCount =
                                itemSnapshot.child("count").getValue(Int::class.java) ?: 1
                            val itemUnit =
                                itemSnapshot.child("unit").getValue(String::class.java) ?: "יחידות"
                            itemsList.add(ShopListItem(itemTitle, itemCount, itemUnit, itemState))
                        }

                        // Create ShopList object
                        val shopList = ShopList(id, name, itemsList, membersList)
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

        // Create a unique key for the new product entry in the shop list
        val newItemRef = shopListRef.push()

        // Construct the product entry
        val productEntry = mapOf(
            "title" to product,
            "count" to countItem,
            "unit" to unit,
            "isChecked" to false
        )

        // Set the product entry in the shop list
        newItemRef.setValue(productEntry)
    }

    fun isExistProductInList(
        listId: String,
        productName: String,
        callback: CheckProductExistenceCallback
    ) {
        val shopListRef = databaseReference.child("shopLists").child(listId).child("items")

        shopListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val exists = dataSnapshot.children.any {
                    it.child("title").getValue(String::class.java) == productName
                }
                callback.onProductExistenceChecked(exists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "ShopListsDatabaseHelper",
                    "Error checking product existence in the list",
                    error.toException()
                )
                callback.onProductExistenceChecked(false)
            }
        })
    }

    fun getListById(id: String, listener: (List<ShopListItem>) -> Unit) {
        // Replace "users" with the collection name where user lists are stored in your Firestore
        databaseReference.child("shopLists").child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val itemsList: MutableList<ShopListItem> = mutableListOf()
                    val itemsSnapshot = dataSnapshot.child("items")
                    for (itemSnapshot in itemsSnapshot.children) {
                        val itemTitle =
                            itemSnapshot.child("title").getValue(String::class.java)
                                ?: "asd"
                        val itemState =
                            itemSnapshot.child("isChecked").getValue(Boolean::class.java)
                                ?: false
                        val itemCount =
                            itemSnapshot.child("count").getValue(Int::class.java)
                                ?: 1
                        val itemUnit =
                            itemSnapshot.child("unit").getValue(String::class.java)
                                ?: "יחידות"
                        itemsList.add(ShopListItem(itemTitle, itemCount, itemUnit, itemState))
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
        listener: InsertShopListCallback
    ) {
        val shopListRef = databaseReference.child("shopLists").child(listId)

        val updatedList = ShopList(listId, listName, items, members)

        shopListRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                currentData.value = updatedList.toMap()
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                dataSnapshot: DataSnapshot?
            ) {
                if (error == null && committed) {
                    listener.onShopListInserted(updatedList)
                } else {
                    Log.e("ShopListsDatabaseHelper", "Transaction failed", error?.toException())
                }
            }
        })
    }

    fun updateWishlistName(shopListId: String, newName: String) {
        databaseReference.child("shopLists").child(shopListId).child("name").setValue(newName)
    }

    fun deleteShopListForSpecificUser(shopListId: String, member: String) {
        val shopListRef = databaseReference.child("shopLists").child(shopListId)

        shopListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val membersList: MutableList<String> = mutableListOf()

                // Retrieve the current members list
                val membersSnapshot = dataSnapshot.child("members")
                for (memberSnapshot in membersSnapshot.children) {
                    val existingMember = memberSnapshot.getValue(String::class.java)
                    existingMember?.let { membersList.add(it) }
                }

                // Remove the specific member from the list
                membersList.remove(member)

                if (membersList.isEmpty()) {
                    // If the updated members list is empty, delete the shop list
                    shopListRef.removeValue()
                } else {
                    // Update the shop list with the modified members list
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
        val shopListRef = databaseReference.child("shopLists").child(listId)

        shopListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val itemsList: MutableList<ShopListItem> = mutableListOf()

                // Retrieve the current items list
                val itemsSnapshot = dataSnapshot.child("items")
                for (itemSnapshot in itemsSnapshot.children) {
                    val currentItem = itemSnapshot.getValue(ShopListItem::class.java)
                    currentItem?.let { itemsList.add(it) }
                }

                // Update the specific item in the list
                if (position >= 0 && position < itemsList.size) {
                    itemsList[position] = updatedItem
                }

                // Update the shop list with the modified items list
                shopListRef.child("items").setValue(itemsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "ShopListFirebaseHelper",
                    "Error updating shop list item",
                    error.toException()
                )
            }
        })
    }
}

// Example data model for a shop list
data class ShopList(
    val id: String? = null,
    var name: String,
    val items: List<ShopListItem>?,
    val members: List<String>,
) {
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["id"] = id
        map["name"] = name
        map["items"] = items?.map { it.toMap() }
        map["members"] = members
        return map
    }

    override fun toString(): String {
        return name
    }
}