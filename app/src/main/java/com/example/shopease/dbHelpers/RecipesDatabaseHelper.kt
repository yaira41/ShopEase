package com.example.shopease.dbHelpers

import android.util.Log
import com.example.shopease.dataClasses.Recipe
import com.example.shopease.dataClasses.ShopListItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class RecipesDatabaseHelper : BaseDatabaseHelper() {

    interface InsertRecipeCallback {
        fun onRecipeInserted(recipe: Recipe?)
    }

    fun getAllUserRecipes(userName: String, listener: (List<Recipe>) -> Unit) {
        val query =
            databaseReference.child("recipes")

        query.keepSynced(true)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val recipes: MutableList<Recipe> = mutableListOf()

                for (recipeSnapshot in dataSnapshot.children) {
                    val membersList: MutableList<String> = mutableListOf()
                    val membersSnapshot = recipeSnapshot.child("members")
                    for (memberSnapshot in membersSnapshot.children) {
                        val member = memberSnapshot.getValue(String::class.java)
                        member?.let { membersList.add(it) }
                    }

                    if (userName in membersList) {
                        val id = recipeSnapshot.child("id").getValue(String::class.java)
                        val name =
                            recipeSnapshot.child("name").getValue(String::class.java) ?: "list"
                        val procedure =
                            recipeSnapshot.child("procedure").getValue(String::class.java)
                                ?: "procedure"

                        val itemsList: MutableList<ShopListItem> = mutableListOf()
                        val itemsSnapshot = recipeSnapshot.child("items")
                        for (itemSnapshot in itemsSnapshot.children) {
                            val itemTitle =
                                itemSnapshot.child("title").getValue(String::class.java) ?: "asd"
                            val itemState =
                                itemSnapshot.child("checked").getValue(Boolean::class.java)
                                    ?: false
                            val itemCount =
                                itemSnapshot.child("count").getValue(Int::class.java) ?: 1
                            val itemUnit =
                                itemSnapshot.child("unit").getValue(String::class.java) ?: "יחידות"
                            itemsList.add(ShopListItem(itemTitle, itemCount, itemUnit, itemState))
                        }

                        val recipe = Recipe(id, name, itemsList, membersList, procedure)
                        recipes.add(recipe)
                    }
                }

                if (recipes.isEmpty()) {
                    listener(emptyList())
                } else {
                    listener(recipes)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RecipesDatabaseHelper", "Error getting user recipes", error.toException())
                listener(emptyList())
            }
        })
    }

    fun addProductToRecipe(recipeId: String, product: String, countItem: Int, unit: String) {
        val recipeRef = databaseReference.child("recipes").child(recipeId).child("items")

        val newItemRef = recipeRef.push()

        val productEntry = mapOf(
            "title" to product,
            "count" to countItem,
            "unit" to unit,
            "checked" to false
        )

        // Set the product entry in the recipe
        newItemRef.setValue(productEntry)
    }

    fun isExistProductInRecipe(
        recipeId: String,
        productName: String,
        callback: CheckProductExistenceCallback
    ) {
        val recipeRef = databaseReference.child("recipes").child(recipeId).child("items")

        recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val exists = dataSnapshot.children.any {
                    it.child("title").getValue(String::class.java) == productName
                }
                callback.onProductExistenceChecked(exists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "recipesDatabaseHelper",
                    "Error checking product existence in the recipe",
                    error.toException()
                )
                callback.onProductExistenceChecked(false)
            }
        })
    }

    fun getRecipeById(id: String, listener: (List<ShopListItem>, String) -> Unit) {
        // Replace "users" with the collection name where user lists are stored in your Firestore
        databaseReference.child("recipes").child(id)
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
                        val itemCount =
                            itemSnapshot.child("count").getValue(Int::class.java)
                                ?: 1
                        val itemUnit =
                            itemSnapshot.child("unit").getValue(String::class.java)
                                ?: "יחידות"
                        itemsList.add(ShopListItem(itemTitle, itemCount, itemUnit, itemState))
                    }

                    val procedure =
                        dataSnapshot.child("procedure").getValue(String::class.java) ?: "procedure"

                    if (itemsList.isEmpty()) {
                        listener(emptyList(), procedure)
                    } else {
                        listener(itemsList, procedure)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "RecipesFirebaseHelper",
                        "Error getting recipe by id",
                        error.toException()
                    )
                    listener(emptyList(), "")
                }
            })
    }

    fun updateRecipe(
        recipeId: String,
        recipeName: String,
        items: List<ShopListItem>,
        members: List<String>,
        procedure: String,
        listener: InsertRecipeCallback
    ) {
        val recipeRef = databaseReference.child("recipes").child(recipeId)
        recipeRef.keepSynced(true)
        val updatedRecipe = Recipe(recipeId, recipeName, items, members, procedure)

        // Update the recipe without using a transaction
        recipeRef.setValue(updatedRecipe)
            .addOnSuccessListener {
                listener.onRecipeInserted(updatedRecipe)
            }
            .addOnFailureListener { exception ->
                Log.e("RecipesFirebaseHelper", "Error updating recipe", exception.cause)
            }
    }

    fun updateRecipeName(recipeId: String, newName: String) {
        databaseReference.child("recipes").child(recipeId).child("name").setValue(newName)
    }

    fun deleteRecipeForSpecificUser(recipeId: String, member: String) {
        val recipeRef = databaseReference.child("recipes").child(recipeId)

        recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
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
                    // If the updated members list is empty, delete the recipe
                    recipeRef.removeValue()
                } else {
                    // Update the recipe with the modified members list
                    recipeRef.child("members").setValue(membersList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "RecipesFirebaseHelper",
                    "Error deleting recipe for specific user",
                    error.toException()
                )
            }
        })
    }

    fun insertNewRecipe(
        recipeName: String,
        items: List<ShopListItem>?,
        members: List<String>,
        procedure: String,
        listener: InsertRecipeCallback
    ) {
        val recipesRef = databaseReference.child("recipes")
        val newRecipeRef = recipesRef.push()

        val newRecipe = Recipe(newRecipeRef.key, recipeName, items, members, procedure)

        newRecipeRef.setValue(newRecipe)
            .addOnSuccessListener {
                listener.onRecipeInserted(newRecipe)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "RecipeFirebaseHelper",
                    "Error while creating new recipe",
                    exception.cause
                )
            }
    }

    fun updateRecipeItem(recipeId: String, position: Int, updatedItem: ShopListItem) {
        val recipeRef = databaseReference.child("recipes").child(recipeId).child("items")
        recipeRef.keepSynced(true)

        // Update the specific item in the list without using a transaction
        recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentItems: MutableList<ShopListItem>? =
                    dataSnapshot.getValue(object :
                        GenericTypeIndicator<MutableList<ShopListItem>>() {})

                if (currentItems != null) {
                    if (position >= 0 && position < currentItems.size) {
                        currentItems[position] = updatedItem
                    }
                    recipeRef.setValue(currentItems)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RecipesDatabaseHelper", "Error updating recipe item", error.toException())
            }
        })
    }
}