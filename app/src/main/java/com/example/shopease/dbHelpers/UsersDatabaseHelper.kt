package com.example.shopease.dbHelpers

import android.util.Log
import com.example.shopease.utils.LoginCallback
import com.example.shopease.dataClasses.User
import com.example.shopease.utils.Utils.byteArrayToBase64
import com.google.firebase.database.*


class DatabaseHelper : BaseDatabaseHelper() {

    fun isUsernameExists(username: String, callback: (Boolean) -> Unit) {
        val usersRef = databaseReference.child("users")
        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "FirebaseHelper",
                        "Error checking username existence",
                        error.toException()
                    )
                    callback(false)
                }
            })
    }

    fun isValidLogin(username: String, password: String, callback: LoginCallback) {
        // Check if the username and password match a user in the database
        val usersRef = databaseReference.child("users")
        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // User with the provided username is found
                        val userSnapshot = snapshot.children.first()
                        val storedPassword =
                            userSnapshot.child("password").getValue(String::class.java)

                        if (storedPassword == password) {
                            // Passwords match, login successful
                            val user = userSnapshot.getValue(User::class.java)
                            callback.onLoginResult(user)
                        } else {
                            // Passwords do not match
                            callback.onLoginResult(null)
                        }
                    } else {
                        // User with the provided username is not found
                        callback.onLoginResult(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    println(error)
                    callback.onLoginResult(null)
                }
            })
    }

    fun isEmailExists(email: String, callback: (Boolean) -> Unit) {
        val usersRef = databaseReference.child("users")
        usersRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error checking email existence", error.toException())
                    callback(false)
                }
            })
    }

    fun addUser(
        username: String,
        email: String,
        imageProfile: ByteArray?,
        password: String,
        callback: (Boolean) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: databaseReference.push().key
        val userRef = databaseReference.child("users").child(userId ?: "")
        // Convert the ByteArray to a base64-encoded string before storing
        val base64ImageProfile = byteArrayToBase64(imageProfile)

        // Create a User object with the provided data
        val user = User(username, email, password, base64ImageProfile)

        userRef.setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseHelper", "User added successfully. UserId: $userId")
                    callback(true)
                } else {
                    Log.e("FirebaseHelper", "Error adding user", task.exception)
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseHelper", "Failure adding user", exception)
                callback(false)
            }
    }

    fun getUserByUsername(username: String, callback: (User?) -> Unit) {
        val usersRef = databaseReference.child("users")
        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.children.first().getValue(User::class.java)
                        callback(userData)
                    } else {
                        callback(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error getting user by username", error.toException())
                    callback(null)
                }
            })
    }

    fun updatePassword(username: String, newPassword: String, callback: (Boolean) -> Unit) {
        val usersRef = databaseReference.child("users")
        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userId = snapshot.children.first().key
                        val userRef = usersRef.child(userId ?: "")
                        userRef.child("password").setValue(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    callback(true)
                                } else {
                                    Log.e(
                                        "FirebaseHelper",
                                        "Error updating password",
                                        task.exception
                                    )
                                    callback(false)
                                }
                            }
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error updating password", error.toException())
                    callback(false)
                }
            })
    }
}
