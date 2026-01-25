package com.example.foodapp.data.remote.client.response.order

import com.google.gson.annotations.SerializedName


sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}



// ============== CREATE ORDER REQUEST ==============

/**
 * Request tạo đơn hàng
 * Format:
 * {
 *   "shopId": "shop_ktx_001",
 *   "deliveryAddress": {
 *     "label": "KTX B5",
 *     "fullAddress": "KTX Khu B - Tòa B5",
 *     "building": "B5",
 *     "room": "101",
 *     "note": "Gọi trước 5 phút"
 *   },
 *   "paymentMethod": "COD"
 * }
 */
data class CreateOrderRequest @JvmOverloads constructor(
    @SerializedName("shopId")
    val shopId: String = "",

    @SerializedName("deliveryAddress")
    val deliveryAddress: DeliveryAddressRequest? = null,

    @SerializedName("paymentMethod")
    val paymentMethod: String = "COD"
)

/**
 * Địa chỉ giao hàng trong request
 */
data class DeliveryAddressRequest @JvmOverloads constructor(
    @SerializedName("label")
    val label: String = "",

    @SerializedName("fullAddress")
    val fullAddress: String = "",

    @SerializedName("building")
    val building: String? = null,

    @SerializedName("room")
    val room: String? = null,

    @SerializedName("note")
    val note: String? = null
)

// ============== CREATE ORDER RESPONSE ==============

/**
 * Response wrapper cho API tạo đơn hàng
 * Format:
 * {
 *   "success": true,
 *   "data": OrderApiModel,
 *   "timestamp": "2026-01-18T15:12:21.000Z"
 * }
 */
data class CreateOrderResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: OrderApiModel? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

// ============== ORDER API MODEL ==============

/**
 * Model đơn hàng từ API
 */
data class OrderApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("orderNumber")
    val orderNumber: String = "",

    @SerializedName("customerId")
    val customerId: String = "",

    @SerializedName("shopId")
    val shopId: String = "",

    @SerializedName("shopName")
    val shopName: String = "",

    @SerializedName("shipperId")
    val shipperId: String? = null,

    @SerializedName("items")
    val items: List<OrderItemApiModel> = emptyList(),

    @SerializedName("subtotal")
    val subtotal: Double = 0.0,

    @SerializedName("shipFee")
    val shipFee: Double = 0.0,

    @SerializedName("discount")
    val discount: Double = 0.0,

    @SerializedName("total")
    val total: Double = 0.0,

    @SerializedName("status")
    val status: String = "",

    @SerializedName("paymentStatus")
    val paymentStatus: String = "",

    @SerializedName("paymentMethod")
    val paymentMethod: String = "",

    @SerializedName("deliveryAddress")
    val deliveryAddress: DeliveryAddressResponse? = null,

    @SerializedName("deliveryNote")
    val deliveryNote: String? = null,

    @SerializedName("cancelReason")
    val cancelReason: String? = null,

    @SerializedName("cancelledBy")
    val cancelledBy: String? = null,

    @SerializedName("cancelledAt")
    val cancelledAt: String? = null,

    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = "",

    @SerializedName("confirmedAt")
    val confirmedAt: String? = null,

    @SerializedName("preparingAt")
    val preparingAt: String? = null,

    @SerializedName("readyAt")
    val readyAt: String? = null,

    @SerializedName("shippingAt")
    val shippingAt: String? = null,

    @SerializedName("deliveredAt")
    val deliveredAt: String? = null,

    @SerializedName("reviewId")
    val reviewId: String? = null,

    @SerializedName("reviewedAt")
    val reviewedAt: String? = null,

    @SerializedName("paidOut")
    val paidOut: Boolean = false,

    @SerializedName("paidOutAt")
    val paidOutAt: String? = null
)

/**
 * Item trong đơn hàng từ API
 */
data class OrderItemApiModel @JvmOverloads constructor(
    @SerializedName("productId")
    val productId: String = "",

    @SerializedName("productName")
    val productName: String = "",

    @SerializedName("quantity")
    val quantity: Int = 0,

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName("subtotal")
    val subtotal: Double = 0.0
)

/**
 * Địa chỉ giao hàng trong response
 */
data class DeliveryAddressResponse @JvmOverloads constructor(
    @SerializedName("label")
    val label: String = "",

    @SerializedName("fullAddress")
    val fullAddress: String = "",

    @SerializedName("building")
    val building: String? = null,

    @SerializedName("room")
    val room: String? = null,

    @SerializedName("note")
    val note: String? = null
)


// ============== GET ORDERS RESPONSE ==============

/**
 * Response wrapper cho API lấy danh sách đơn hàng
 * Format:
 * {
 *   "success": true,
 *   "data": GetOrdersDataResponse,
 *   "timestamp": "2026-01-19T07:30:00.000Z"
 * }
 */
data class GetOrdersResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: GetOrdersDataResponse? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

/**
 * Data response cho danh sách đơn hàng
 * Format:
 * {
 *   "orders": [OrderApiModel],
 *   "page": 1,
 *   "limit": 20,
 *   "total": 50,
 *   "totalPages": 3
 * }
 */
data class GetOrdersDataResponse @JvmOverloads constructor(
    @SerializedName("orders")
    val orders: List<OrderPreviewApiModel> = emptyList(),

    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20,

    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("totalPages")
    val totalPages: Int = 0
)

// ============== ORDER PREVIEW API MODEL ==============

/**
 * Model xem trước đơn hàng (dùng cho danh sách)
 * Format:
 * {
 *   "id": "order_abc123def456",
 *   "orderNumber": "ORD-1705591320000-A2B3C4",
 *   "shopId": "shop_123",
 *   "shopName": "Cơm Tấm Sườn",
 *   "status": "PENDING",
 *   "paymentStatus": "UNPAID",
 *   "total": 85000,
 *   "itemCount": 2,
 *   "createdAt": "2026-01-19T10:00:00.000Z",
 *   "updatedAt": "2026-01-19T10:30:00.000Z",
 *   "itemsPreview": [OrderItemPreviewApiModel],
 *   "itemsPreviewCount": 1,
 *   "customer": CustomerApiModel,
 *   "paymentMethod": "COD",
 *   "deliveryAddress": DeliveryAddressResponse,
 *   "shipperId": null,
 *   "shipper": ShipperApiModel? // chỉ có khi có shipper
 * }
 */
data class OrderPreviewApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("orderNumber")
    val orderNumber: String = "",

    @SerializedName("shopId")
    val shopId: String = "",

    @SerializedName("shopName")
    val shopName: String = "",

    @SerializedName("status")
    val status: String = "",

    @SerializedName("paymentStatus")
    val paymentStatus: String = "",

    @SerializedName("total")
    val total: Double = 0.0,

    @SerializedName("itemCount")
    val itemCount: Int = 0,

    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = "",

    @SerializedName("itemsPreview")
    val itemsPreview: List<OrderItemPreviewApiModel> = emptyList(),

    @SerializedName("itemsPreviewCount")
    val itemsPreviewCount: Int = 0,

    @SerializedName("customer")
    val customer: CustomerApiModel? = null,

    @SerializedName("paymentMethod")
    val paymentMethod: String = "",

    @SerializedName("deliveryAddress")
    val deliveryAddress: DeliveryAddressResponse? = null,

    @SerializedName("shipperId")
    val shipperId: String? = null,

    @SerializedName("shipper")
    val shipper: ShipperApiModel? = null
)

/**
 * Model xem trước item trong đơn hàng (dùng cho danh sách)
 * Format:
 * {
 *   "productId": "prod_123",
 *   "productName": "Cơm sườn bì chả",
 *   "quantity": 2,
 *   "price": 35000,
 *   "subtotal": 70000
 * }
 */
data class OrderItemPreviewApiModel @JvmOverloads constructor(
    @SerializedName("productId")
    val productId: String = "",

    @SerializedName("productName")
    val productName: String = "",

    @SerializedName("quantity")
    val quantity: Int = 0,

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName("subtotal")
    val subtotal: Double = 0.0
)

/**
 * Model khách hàng
 * Format:
 * {
 *   "id": "user_cust_001",
 *   "displayName": "Nguyễn Văn A",
 *   "phone": "0901234567"
 * }
 */
data class CustomerApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("displayName")
    val displayName: String = "",

    @SerializedName("phone")
    val phone: String? = null
)

/**
 * Model shipper
 * Format:
 * {
 *   "id": "shipper_456",
 *   "displayName": "Nguyễn Văn Shipper",
 *   "phone": "0909999999"
 * }
 */
data class ShipperApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("displayName")
    val displayName: String = "",

    @SerializedName("phone")
    val phone: String? = null
)

data class DeleteOrdersResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,
)


// ============== GET ORDER BY ID RESPONSE ==============

/**
 * Response wrapper cho API lấy chi tiết đơn hàng theo ID
 * Format:
 * {
 *   "success": true,
 *   "data": OrderApiModel,
 *   "timestamp": "2026-01-18T15:12:21.000Z"
 * }
 */
data class GetOrderByIdResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: OrderApiModel? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)