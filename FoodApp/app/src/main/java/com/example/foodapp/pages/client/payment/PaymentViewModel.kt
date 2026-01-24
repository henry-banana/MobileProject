package com.example.foodapp.pages.client.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.client.DeliveryAddress
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.client.response.order.*
import com.example.foodapp.data.repository.client.order.OrderRepository
import com.example.foodapp.data.repository.client.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data class để đại diện cho một mặt hàng trong giỏ hàng
data class CartItem(val product: Product, val quantity: Int)

class PaymentViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(0) // 0: COD, 1: MoMo
    val selectedPaymentMethod: StateFlow<Int> = _selectedPaymentMethod.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    private val _addresses = MutableStateFlow<List<DeliveryAddress>>(emptyList())
    val addresses: StateFlow<List<DeliveryAddress>> = _addresses.asStateFlow()

    private val _selectedAddress = MutableStateFlow<DeliveryAddress?>(null)
    val selectedAddress: StateFlow<DeliveryAddress?> = _selectedAddress.asStateFlow()

    private val _addressError = MutableStateFlow<String?>(null)
    val addressError: StateFlow<String?> = _addressError.asStateFlow()

    private val _orderResult = MutableStateFlow<ApiResult<OrderApiModel>?>(null)
    val orderResult: StateFlow<ApiResult<OrderApiModel>?> = _orderResult.asStateFlow()

    private val _orderError = MutableStateFlow<String?>(null)
    val orderError: StateFlow<String?> = _orderError.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    // Hàm fetchAddress để lấy danh sách địa chỉ
    fun fetchAddress() {
        if (_isLoading.value) return

        _isLoading.value = true
        _addressError.value = null

        viewModelScope.launch {
            try {
                val result = profileRepository.getAddresses()

                if (result is com.example.foodapp.data.remote.client.response.profile.ApiResult.Success) {
                    val deliveryAddresses = result.data.map { addressResponse ->
                        DeliveryAddress(
                            id = addressResponse.id ?: "",
                            label = addressResponse.label ?: "Địa chỉ",
                            fullAddress = addressResponse.fullAddress ?: "",
                            building = addressResponse.building,
                            room = addressResponse.room,
                            note = addressResponse.note,
                            isDefault = addressResponse.isDefault ?: false,
                            clientId = addressResponse.userId ?: "",
                            receiverName = "",
                            receiverPhone = ""
                        )
                    }

                    _addresses.value = deliveryAddresses

                    // Tự động chọn địa chỉ mặc định nếu có
                    val defaultAddress = deliveryAddresses.firstOrNull { it.isDefault }
                    if (defaultAddress != null) {
                        _selectedAddress.value = defaultAddress
                    } else if (deliveryAddresses.isNotEmpty()) {
                        _selectedAddress.value = deliveryAddresses.first()
                    }

                    println("DEBUG: Fetched ${deliveryAddresses.size} addresses")
                } else if (result is com.example.foodapp.data.remote.client.response.profile.ApiResult.Failure) {
                    _addressError.value = "Không thể tải địa chỉ: ${result.exception.message}"
                }
            } catch (e: Exception) {
                _addressError.value = "Lỗi khi tải địa chỉ: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Hàm chọn địa chỉ
    fun selectAddress(address: DeliveryAddress) {
        _selectedAddress.value = address
    }

    // Hàm kiểm tra có địa chỉ nào không
    fun hasAddresses(): Boolean {
        return _addresses.value.isNotEmpty()
    }

    // Hàm khởi tạo với danh sách sản phẩm VÀ số lượng
    fun initializeWithProductsAndQuantities(products: List<Product>, quantities: List<Int>) {
        val items = List(minOf(products.size, quantities.size)) { index ->
            CartItem(
                product = products[index],
                quantity = quantities[index]
            )
        }
        _cartItems.value = items
        calculateTotalPrice()
    }

    // Hàm khởi tạo chỉ với sản phẩm
    fun initializeWithProducts(products: List<Product>) {
        initializeWithProductsAndQuantities(products, List(products.size) { 1 })
    }

    // Tăng số lượng
    fun increaseQuantity(productId: String) {
        _cartItems.value = _cartItems.value.map { cartItem ->
            if (cartItem.product.id == productId) {
                cartItem.copy(quantity = cartItem.quantity + 1)
            } else {
                cartItem
            }
        }
        calculateTotalPrice()
    }

    // Giảm số lượng
    fun decreaseQuantity(productId: String) {
        _cartItems.value = _cartItems.value.map { cartItem ->
            if (cartItem.product.id == productId && cartItem.quantity > 1) {
                cartItem.copy(quantity = cartItem.quantity - 1)
            } else {
                cartItem
            }
        }
        calculateTotalPrice()
    }

    fun selectPaymentMethod(method: Int) {
        _selectedPaymentMethod.value = method
    }

    // Hàm tạo đơn hàng mới - TRỰC TIẾP GỌI API
    suspend fun placeOrder(): ApiResult<OrderApiModel> {
        // Kiểm tra đã chọn địa chỉ giao hàng chưa
        if (_selectedAddress.value == null) {
            return ApiResult.Failure(Exception("Vui lòng chọn địa chỉ giao hàng"))
        }

        // Kiểm tra có sản phẩm trong giỏ hàng không
        if (_cartItems.value.isEmpty()) {
            return ApiResult.Failure(Exception("Giỏ hàng trống"))
        }

        // Lấy shopId từ sản phẩm đầu tiên
        val shopId = _cartItems.value.firstOrNull()?.product?.shopId ?: ""
        if (shopId.isBlank()) {
            return ApiResult.Failure(Exception("Không tìm thấy thông tin cửa hàng"))
        }

        _isPlacingOrder.value = true
        _orderError.value = null

        return try {
            // ============== THỬ CẢ 2 CÁCH ==============

            // CÁCH 1: Không có items trong request (theo bạn nói)
            val requestWithoutItems = CreateOrderRequest(
                shopId = shopId,
                deliveryAddress = DeliveryAddressRequest(
                    label = _selectedAddress.value?.label ?: "",
                    fullAddress = _selectedAddress.value?.fullAddress ?: "",
                    building = _selectedAddress.value?.building,
                    room = _selectedAddress.value?.room,
                    note = _selectedAddress.value?.note
                ),
                paymentMethod = if (_selectedPaymentMethod.value == 0) "COD" else "MOMO"
            )

            println("DEBUG: Creating order WITHOUT items: $requestWithoutItems")

            // Gọi API tạo đơn hàng
            val result = orderRepository.createOrder(requestWithoutItems)


            if (result is ApiResult.Failure &&
                result.exception.message?.contains("Cart is empty", ignoreCase = true) == true) {

                println("DEBUG: Server says cart is empty. Trying WITH items...")

            }

            _orderResult.value = result

            when (result) {
                is ApiResult.Success -> {
                    println("DEBUG: Order created successfully: ${result.data.id}")
                    result
                }
                is ApiResult.Failure -> {
                    _orderError.value = result.exception.message
                    println("DEBUG: Order creation failed: ${result.exception.message}")
                    result
                }
            }
        } catch (e: Exception) {
            _orderError.value = e.message
            println("DEBUG: Exception in placeOrder: ${e.message}")
            e.printStackTrace()
            ApiResult.Failure(e)
        } finally {
            _isPlacingOrder.value = false
        }
    }
    // Tính toán lại tổng giá trị
    private fun calculateTotalPrice() {
        _totalPrice.value = _cartItems.value.sumOf { cartItem ->
            val price = parsePrice(cartItem.product.price)
            price * cartItem.quantity
        }
    }

    private fun parsePrice(priceString: String): Double {
        return try {
            val sanitizedString = priceString
                .replace("đ", "")
                .replace(".", "")
                .replace(",", "")
                .trim()
            sanitizedString.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    fun getShopName(): String {
        return _cartItems.value.firstOrNull()?.product?.shopName ?: "Không có thông tin shop"
    }

    fun getTotalItems(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    companion object {
        fun getFactory(
            profileRepository: ProfileRepository? = null,
            orderRepository: OrderRepository? = null
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
                        return PaymentViewModel(
                            profileRepository = profileRepository ?: ProfileRepository(),
                            orderRepository = orderRepository ?: OrderRepository()
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}