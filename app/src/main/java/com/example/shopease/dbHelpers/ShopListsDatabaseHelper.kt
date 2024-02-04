package com.example.shopease.dbHelpers
import android.util.Log
import com.example.shopease.dataClasses.ShopListItem
import com.google.firebase.database.*

class ShopListsDatabaseHelper : BaseDatabaseHelper() {

    interface InsertShopListCallback {
        fun onShopListInserted(shopList: ShopList?)
    }

    fun getAllUserLists(userName: String, listener: (List<ShopList>) -> Unit) {
        // Replace "users" with the collection name where user lists are stored in your Firestore
        databaseReference.child("shopLists")
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
                            val name =
                                listSnapshot.child("name").getValue(String::class.java) ?: "list"

                            // Extracting items
                            val itemsList: MutableList<ShopListItem> = mutableListOf()
                            val itemsSnapshot = listSnapshot.child("items")
                            for (itemSnapshot in itemsSnapshot.children) {
                                val itemTitle =
                                    itemSnapshot.child("title").getValue(String::class.java)
                                        ?: "asd"
                                val itemState =
                                    itemSnapshot.child("checked").getValue(Boolean::class.java)
                                        ?: false
                                itemsList.add(ShopListItem(itemTitle, itemState))
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
                    Log.e(
                        "ShopListFirebaseHelper",
                        "Error getting all lists by username",
                        error.toException()
                    )
                    listener(emptyList())
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
                            itemSnapshot.child("checked").getValue(Boolean::class.java)
                                ?: false
                        itemsList.add(ShopListItem(itemTitle, itemState))
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

        shopListRef.setValue(updatedList)
            .addOnSuccessListener {
                listener.onShopListInserted(updatedList)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "ShopListFirebaseHelper",
                    "Error updating the shop list",
                    exception.cause
                )
            }
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
        items: List<ShopListItem>,
        members: List<String>,
        listener: InsertShopListCallback
    ) {
        val shopListsRef = databaseReference.child("shopLists")
        val newShopListRef = shopListsRef.push()
//        val newList23 = hashMapOf(
//            "id" to newShopListRef,
//            "name" to listName,
//            "items" to items,
//            "members" to members
//        )

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
}

    // Add other methods for shop list operations as needed

    // Example data model for a shop list
    data class ShopList(
        val id: String? = null,
        val name: String,
        val items: List<ShopListItem>,
        val members: List<String>,
    ) {
        override fun toString(): String {
            return name
        }
    }