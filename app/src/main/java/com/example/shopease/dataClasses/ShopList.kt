package com.example.shopease.dataClasses

data class ShopList(
    val id: String? = null,
    var name: String,
    var items: List<ShopListItem>?,
    var members: List<String>,
) {
    override fun toString(): String {
        return name
    }
}