package com.example.shopease.dbHelpers

import android.util.Log
import com.example.shopease.dataClasses.User
import com.example.shopease.utils.Utils.byteArrayToBase64
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UsersDatabaseHelper : BaseDatabaseHelper() {

    interface RegistrationCallback {
        fun onRegistrationResult(success: Boolean, user: User?)
    }

    interface LoginCallback {
        fun onLoginResult(user: User?)
    }

    fun registerUser(
        username: String,
        email: String,
        password: String,
        imageProfile: ByteArray?,
        callback: RegistrationCallback
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Convert the ByteArray to a base64-encoded string before storing
                        val base64ImageProfile = byteArrayToBase64(imageProfile)
                        // Create a User object with the provided data
                        val newUser = User(user.uid, username, email, base64ImageProfile)

                        // Store user data in the Realtime Database
                        val userId = user.uid
                        val userRef = databaseReference.child("users").child(userId)
                        userRef.setValue(newUser)
                            .addOnCompleteListener { innerTask ->
                                if (innerTask.isSuccessful) {
                                    callback.onRegistrationResult(true, newUser)
                                } else {
                                    Log.e(
                                        "FirebaseHelper",
                                        "Error adding user to Realtime Database",
                                        innerTask.exception
                                    )
                                    callback.onRegistrationResult(false, null)
                                }
                            }
                    } else {
                        Log.e("FirebaseHelper", "Error getting current user after registration")
                        callback.onRegistrationResult(false, null)
                    }
                } else {
                    Log.e(
                        "FirebaseHelper",
                        "Error registering user with Firebase Authentication",
                        task.exception
                    )
                    callback.onRegistrationResult(false, null)
                }
            }
    }

    // Modified login method to use Firebase Authentication
    fun login(username: String, password: String, callback: LoginCallback) {
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Fetch user data from the Realtime Database
                        val userId = user.uid
                        val userRef = databaseReference.child("users").child(userId)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userData = snapshot.getValue(User::class.java)
                                callback.onLoginResult(userData)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(
                                    "FirebaseHelper",
                                    "Error fetching user data from Realtime Database",
                                    error.toException()
                                )
                                callback.onLoginResult(null)
                            }
                        })
                    } else {
                        Log.e("FirebaseHelper", "Error getting current user after login")
                        callback.onLoginResult(null)
                    }
                } else {
                    Log.e(
                        "FirebaseHelper",
                        "Error logging in with Firebase Authentication",
                        task.exception
                    )
                    callback.onLoginResult(null)
                }
            }
    }

    fun isEmailExists(email: String, callback: (Boolean) -> Unit) {
        databaseReference.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "FirebaseHelper",
                        "Error checking email existence",
                        error.toException()
                    )
                    callback(false)
                }
            })
    }

    fun updateImage(username: String, newImageUrl: ByteArray?, callback: (Boolean) -> Unit) {
        val base64ImageProfile = byteArrayToBase64(newImageUrl)
        val usersRef = databaseReference.child("users")
        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userId = snapshot.children.first().key
                        val userRef = usersRef.child(userId ?: "")
                        userRef.child("profileImage").setValue(base64ImageProfile)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    callback(true)
                                } else {
                                    Log.e(
                                        "FirebaseHelper",
                                        "Error updating profile image URL",
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
                    Log.e(
                        "FirebaseHelper",
                        "Error updating profile image URL",
                        error.toException()
                    )
                    callback(false)
                }
            })
    }

//    fun addUser(
//        username: String,
//        email: String,
//        imageProfile: ByteArray?,
//        password: String,
//        callback: (Boolean) -> Unit
//    ) {
//        val userId = auth.currentUser?.uid ?: databaseReference.push().key
//        val userRef = databaseReference.child("users").child(userId ?: "")
//        // Convert the ByteArray to a base64-encoded string before storing
//        val base64ImageProfile = byteArrayToBase64(imageProfile)
//
//        // Create a User object with the provided data
//        val user = User(userId ?: "", username, email, base64ImageProfile)
//
//        userRef.setValue(user)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Log.d("FirebaseHelper", "User added successfully. UserId: $userId")
//                    callback(true)
//                } else {
//                    Log.e("FirebaseHelper", "Error adding user", task.exception)
//                    callback(false)
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("FirebaseHelper", "Failure adding user", exception)
//                callback(false)
//            }
//    }

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
