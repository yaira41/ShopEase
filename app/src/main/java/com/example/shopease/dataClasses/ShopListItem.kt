package com.example.shopease.dataClasses

data class ShopListItem (
    val title: String,
    var countByUnit: String,
    var isChecked: Boolean = false
)