package com.example.shopease.dataClasses

data class Recipe(val id: String? = null,
                  var name: String,
                  var items: List<ShopListItem>?,
                  var members: List<String>,
                  var procedure: String,
    )
