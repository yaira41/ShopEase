package com.example.shopease.utils

import com.example.shopease.dataClasses.User

interface LoginCallback {
    fun onLoginResult(user: User?)
}