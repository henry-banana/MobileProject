package com.example.foodapp.data.model.client

import com.google.gson.annotations.SerializedName

data class DeliveryAddress(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("label")
    val label: String = "",

    @SerializedName("fullAddress")
    val fullAddress: String = "",

    @SerializedName("building")
    val building: String? = null,      // 👈 Thêm trường này

    @SerializedName("room")
    val room: String? = null,          // 👈 Thêm trường này

    @SerializedName("note")
    val note: String? = null,          // 👈 Chuyển thành val từ API

    @SerializedName("isDefault")
    val isDefault: Boolean = false,

    @SerializedName("userId")
    val clientId: String = "",         // 👈 Map từ API

    // Các trường optional (không có trong API address)
    var receiverName: String = "",     // 👈 Đổi tên cho rõ nghĩa
    var receiverPhone: String = "",    // 👈 Đổi tên cho rõ nghĩa

    var latitude: Double? = null,
    var longitude: Double? = null
) {
    // Computed property để hiển thị thông tin tòa nhà/phòng
    val buildingAndRoom: String
        get() = when {
            !building.isNullOrBlank() && !room.isNullOrBlank() -> "$building - Phòng $room"
            !building.isNullOrBlank() -> building
            !room.isNullOrBlank() -> "Phòng $room"
            else -> ""
        }

    // Helper để lấy note an toàn (không null)
    fun getSafeNote(): String = note ?: ""

    // Helper để kiểm tra có thông tin tòa nhà/phòng không
    fun hasBuildingInfo(): Boolean = !building.isNullOrBlank() || !room.isNullOrBlank()
}