package com.example.shopease.dataClasses

import java.io.Serializable

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    var profileImage: String = "",
) : Serializable