package com.example.shopease.models

data class ShopListWithCoordinates(
    val id: String? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double
)