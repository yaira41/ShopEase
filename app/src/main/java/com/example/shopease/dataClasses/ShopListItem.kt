package com.example.shopease.dataClasses

data class ShopListItem(
    val title: String,
    val count: Int = 1,
    val unit: String = "יחידות",
    var isChecked: Boolean = false
) {
    fun toMap(): HashMap<String, Any?> {
        val map = HashMap<String, Any?>()
        map["title"] = title
        map["count"] = count
        map["unit"] = unit
        map["isChecked"] = isChecked
        return map
    }
}
