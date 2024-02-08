package com.example.shopease.dataClasses

data class ShopListItem(
    val title: String = "",
    val count: Int = 1,
    val unit: String = "יחידות",
    var isChecked: Boolean = false
)