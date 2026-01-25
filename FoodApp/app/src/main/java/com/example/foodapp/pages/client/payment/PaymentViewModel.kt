package com.example.foodapp.pages.client.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.client.DeliveryAddress
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.client.response.order.*
import com.example.foodapp.data.remote.client.response.voucher.VoucherApiModel
import com.example.foodapp.data.remote.client.response.voucher.ApiResult as VoucherApiResult
import com.example.foodapp.data.remote.client.response.voucher.ValidateVoucherResponse
import com.example.foodapp.data.repository.client.order.OrderRepository
import com.example.foodapp.data.repository.client.profile.ProfileRepository
import com.example.foodapp.data.repository.client.voucher.VoucherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data class để đại diện cho một mặt hàng trong giỏ hàng
data class CartItem(val product: Product, val quantity: Int)

class PaymentViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val voucherRepository: VoucherRepository = VoucherRepository()
) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(0) // 0: COD, 1: MoMo
    val selectedPaymentMethod: StateFlow<Int> = _selectedPaymentMethod.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    private val _shippingFee = MutableStateFlow(15000.0) // Thêm shipping fee
    val shippingFee: StateFlow<Double> = _shippingFee.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _finalPrice = MutableStateFlow(0.0)
    val finalPrice: StateFlow<Double> = _finalPrice.asStateFlow()

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

    private val _vouchers = MutableStateFlow<List<VoucherApiModel>>(emptyList())
    val vouchers: StateFlow<List<VoucherApiModel>> = _vouchers.asStateFlow()

    private val _selectedVoucher = MutableStateFlow<VoucherApiModel?>(null)
    val selectedVoucher: StateFlow<VoucherApiModel?> = _selectedVoucher.asStateFlow()

    private val _isLoadingVouchers = MutableStateFlow(false)
    val isLoadingVouchers: StateFlow<Boolean> = _isLoadingVouchers.asStateFlow()

    private val _voucherError = MutableStateFlow<String?>(null)
    val voucherError: StateFlow<String?> = _voucherError.asStateFlow()

    private val _voucherValidationResult = MutableStateFlow<ValidateVoucherResponse?>(null)
    val voucherValidationResult: StateFlow<ValidateVoucherResponse?> = _voucherValidationResult.asStateFlow()

    private val _isValidatingVoucher = MutableStateFlow(false)
    val isValidatingVoucher: StateFlow<Boolean> = _isValidatingVoucher.asStateFlow()

    private val _shopId = MutableStateFlow<String?>(null)

    // Hàm validate voucher từ API server
    suspend fun validateSelectedVoucher(): Boolean {
        val voucher = _selectedVoucher.value ?: run {
            // Không có voucher được chọn là hợp lệ
            _voucherValidationResult.value = null
            calculateFinalPrice()
            return true
        }

        val shopId = _shopId.value ?: run {
            _voucherError.value = "Không tìm thấy thông tin cửa hàng"
            return false
        }

        _isValidatingVoucher.value = true
        _voucherError.value = null

        return try {
            println("DEBUG: Validating voucher: ${voucher.code} for shop: $shopId")
            println("DEBUG: Order subtotal: ${_totalPrice.value}, shipping: ${_shippingFee.value}")

            val result = voucherRepository.validateVoucher(
                voucherCode = voucher.code,
                shopId = shopId,
                subtotal = _totalPrice.value,
                shipFee = _shippingFee.value
            )

            when (result) {
                is VoucherApiResult.Success -> {
                    val validationData = result.data
                    _voucherValidationResult.value = validationData

                    if (validationData.isValid) {
                        // Cập nhật discount amount từ server response
                        _discountAmount.value = validationData.discountAmount
                        calculateFinalPrice()
                        println("DEBUG: Voucher validated successfully: ${voucher.code}, discount: ${validationData.discountAmount}")
                        true
                    } else {
                        _voucherError.value = "Voucher không hợp lệ hoặc đã hết hiệu lực"
                        // Clear voucher selection nếu không hợp lệ
                        _selectedVoucher.value = null
                        _discountAmount.value = 0.0
                        calculateFinalPrice()
                        println("DEBUG: Voucher is invalid: ${voucher.code}")
                        false
                    }
                }
                is VoucherApiResult.Failure -> {
                    _voucherError.value = "Lỗi kiểm tra voucher: ${result.exception.message}"
                    println("DEBUG: Voucher validation failed: ${result.exception.message}")
                    false
                }
            }
        } catch (e: Exception) {
            _voucherError.value = "Lỗi khi kiểm tra voucher: ${e.message}"
            println("DEBUG: Exception in validateSelectedVoucher: ${e.message}")
            e.printStackTrace()
            false
        } finally {
            _isValidatingVoucher.value = false
        }
    }

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

    // Hàm fetchVouchers để lấy danh sách voucher
    fun fetchVouchers() {
        val shopId = _shopId.value
        if (shopId.isNullOrEmpty() || _isLoadingVouchers.value) {
            println("DEBUG: Cannot fetch vouchers - shopId: $shopId, isLoading: ${_isLoadingVouchers.value}")
            return
        }

        _isLoadingVouchers.value = true
        _voucherError.value = null

        viewModelScope.launch {
            try {
                val result = voucherRepository.getVouchers(shopId)

                // Sử dụng VoucherApiResult
                when (result) {
                    is VoucherApiResult.Success -> {
                        // Lọc chỉ các voucher còn hiệu lực và có thể áp dụng
                        val applicableVouchers = result.data.filter { voucher ->
                            // Kiểm tra voucher còn hiệu lực
                            val isValid = voucher.isActive && !voucher.isDeleted

                            // Kiểm tra đơn hàng đủ điều kiện tối thiểu
                            val meetsMinOrderAmount = _totalPrice.value >= voucher.minOrderAmount

                            // Kiểm tra voucher còn lượt sử dụng (nếu có thông tin)
                            val hasUsageLeft = voucher.usageLimit == 0 ||
                                    (voucher.usageLimit > 0 && voucher.currentUsage < voucher.usageLimit)

                            isValid && meetsMinOrderAmount && hasUsageLeft
                        }

                        _vouchers.value = applicableVouchers
                        println("DEBUG: Loaded ${applicableVouchers.size} applicable vouchers for shop $shopId")

                        // Auto-select voucher nếu có và chưa chọn voucher nào
                        if (_selectedVoucher.value == null && applicableVouchers.isNotEmpty()) {
                            // Ưu tiên chọn voucher có giá trị cao nhất
                            val bestVoucher = applicableVouchers.maxByOrNull { it.value } ?: applicableVouchers.first()
                            _selectedVoucher.value = bestVoucher

                            // Validate voucher đã chọn từ API server
                            validateSelectedVoucher()
                            println("DEBUG: Auto-selected voucher: ${bestVoucher.code}")
                        }
                    }
                    is VoucherApiResult.Failure -> {
                        _voucherError.value = "Không thể tải voucher: ${result.exception.message}"
                        println("DEBUG: Failed to load vouchers: ${result.exception.message}")
                    }
                }
            } catch (e: Exception) {
                _voucherError.value = "Lỗi khi tải voucher: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoadingVouchers.value = false
            }
        }
    }

    // Hàm chọn voucher
    fun selectVoucher(voucher: VoucherApiModel?) {
        _selectedVoucher.value = voucher
        _voucherValidationResult.value = null

        // Validate voucher từ server khi chọn
        if (voucher != null) {
            viewModelScope.launch {
                validateSelectedVoucher()
            }
        } else {
            // Nếu bỏ chọn voucher
            _discountAmount.value = 0.0
            calculateFinalPrice()
        }
        println("DEBUG: Selected voucher: ${voucher?.code ?: "None"}")
    }

    // Hàm bỏ chọn voucher
    fun clearSelectedVoucher() {
        _selectedVoucher.value = null
        _voucherValidationResult.value = null
        _discountAmount.value = 0.0
        calculateFinalPrice()
        println("DEBUG: Cleared voucher selection")
    }

    // Tính toán discount và giá cuối cùng (client-side fallback nếu chưa validate)
    private fun calculateDiscountAndFinalPrice() {
        val voucher = _selectedVoucher.value
        val total = _totalPrice.value

        var discount = 0.0

        // Nếu đã có validation result từ server, sử dụng giá trị từ server
        val validationResult = _voucherValidationResult.value
        if (validationResult != null && validationResult.isValid) {
            discount = validationResult.discountAmount
            println("DEBUG: Using server discount amount: $discount")
        } else if (voucher != null && voucher.isActive && !voucher.isDeleted) {
            // Fallback: tính toán client-side nếu chưa validate từ server
            when (voucher.type.uppercase()) {
                "PERCENTAGE" -> {
                    // Giảm theo phần trăm
                    val discountPercent = voucher.value / 100.0
                    discount = total * discountPercent

                    // Áp dụng maxDiscount nếu có
                    voucher.maxDiscount?.let { maxDiscount ->
                        if (discount > maxDiscount) {
                            discount = maxDiscount
                        }
                    }
                }
                "FIXED_AMOUNT" -> { // Đã sửa từ "FIXED" thành "FIXED_AMOUNT"
                    // Giảm cố định
                    discount = voucher.value
                }
                else -> {
                    // Kiểu voucher không hỗ trợ
                    discount = 0.0
                    println("DEBUG: Unknown voucher type: ${voucher.type}")
                }
            }

            // Đảm bảo discount không vượt quá tổng giá trị đơn hàng
            if (discount > total) {
                discount = total
            }

            println("DEBUG: Calculated client-side discount: $discount")
        }

        _discountAmount.value = discount
        calculateFinalPrice()
    }

    // Tính toán giá cuối cùng (subtotal + shipping - discount)
    private fun calculateFinalPrice() {
        _finalPrice.value = _totalPrice.value + _shippingFee.value - _discountAmount.value

        // Đảm bảo giá cuối cùng không âm
        if (_finalPrice.value < 0) {
            _finalPrice.value = 0.0
        }

        println("DEBUG: Final price calculation: subtotal=${_totalPrice.value}, shipping=${_shippingFee.value}, discount=${_discountAmount.value}, final=${_finalPrice.value}")
    }

    // Hàm chọn địa chỉ
    fun selectAddress(address: DeliveryAddress) {
        _selectedAddress.value = address
        println("DEBUG: Selected address: ${address.label}")
    }

    // Hàm kiểm tra có địa chỉ nào không
    fun hasAddresses(): Boolean {
        return _addresses.value.isNotEmpty()
    }

    // Hàm khởi tạo với danh sách sản phẩm VÀ số lượng
    fun initializeWithProductsAndQuantities(products: List<Product>, quantities: List<Int>) {
        if (products.isEmpty()) {
            println("DEBUG: No products to initialize")
            return
        }

        val items = List(minOf(products.size, quantities.size)) { index ->
            CartItem(
                product = products[index],
                quantity = quantities[index]
            )
        }
        _cartItems.value = items
        calculateTotalPrice()

        // Lấy shopId từ sản phẩm đầu tiên
        val shopId = items.firstOrNull()?.product?.shopId
        if (shopId != null) {
            _shopId.value = shopId
            println("DEBUG: Initialized with shopId: $shopId")
            // Load vouchers sau khi có shopId
            fetchVouchers()
        } else {
            println("DEBUG: No shopId found in products")
        }

        // Cũng nên fetch address
        fetchAddress()
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
        // Re-validate voucher nếu có (vì tổng tiền thay đổi)
        if (_selectedVoucher.value != null) {
            viewModelScope.launch {
                validateSelectedVoucher()
            }
        }
        println("DEBUG: Increased quantity for product: $productId")
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
        // Re-validate voucher nếu có (vì tổng tiền thay đổi)
        if (_selectedVoucher.value != null) {
            viewModelScope.launch {
                validateSelectedVoucher()
            }
        }
        println("DEBUG: Decreased quantity for product: $productId")
    }

    fun selectPaymentMethod(method: Int) {
        _selectedPaymentMethod.value = method
        println("DEBUG: Selected payment method: $method")
    }

    // Cập nhật shipping fee
    fun updateShippingFee(fee: Double) {
        _shippingFee.value = fee
        calculateFinalPrice()
        println("DEBUG: Updated shipping fee: $fee")

        // Re-validate voucher nếu có (vì shipping fee thay đổi có thể ảnh hưởng voucher)
        if (_selectedVoucher.value != null) {
            viewModelScope.launch {
                validateSelectedVoucher()
            }
        }
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

        // Validate voucher trước khi đặt hàng (bắt buộc)
        if (_selectedVoucher.value != null) {
            val isValid = validateSelectedVoucher()
            if (!isValid) {
                return ApiResult.Failure(Exception("Voucher không hợp lệ, vui lòng chọn voucher khác"))
            }
        }

        _isPlacingOrder.value = true
        _orderError.value = null

        return try {
            // Tạo request tạo đơn hàng
            val request = CreateOrderRequest(
                shopId = shopId,
                deliveryAddress = DeliveryAddressRequest(
                    label = _selectedAddress.value?.label ?: "",
                    fullAddress = _selectedAddress.value?.fullAddress ?: "",
                    building = _selectedAddress.value?.building,
                    room = _selectedAddress.value?.room,
                    note = _selectedAddress.value?.note
                ),
                paymentMethod = if (_selectedPaymentMethod.value == 0) "COD" else "MOMO",
            )

            println("DEBUG: Creating order with voucher: ${_selectedVoucher.value?.code ?: "No voucher"}")
            println("DEBUG: Order details - subtotal: ${_totalPrice.value}, shipping: ${_shippingFee.value}, discount: ${_discountAmount.value}, final: ${_finalPrice.value}")

            // Gọi API tạo đơn hàng
            val result = orderRepository.createOrder(request)

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
        calculateDiscountAndFinalPrice()
        println("DEBUG: Calculated total price: ${_totalPrice.value}")
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

    fun getVoucherDiscountText(): String {
        val voucher = _selectedVoucher.value
        return if (voucher != null && _discountAmount.value > 0) {
            "${voucher.code}: -${formatCurrency(_discountAmount.value)}"
        } else {
            ""
        }
    }

    fun getVoucherDescription(): String {
        val voucher = _selectedVoucher.value
        return voucher?.description ?: ""
    }

    private fun formatCurrency(amount: Double): String {
        return String.format("%,.0f", amount) + "đ"
    }

    fun hasVouchers(): Boolean {
        return _vouchers.value.isNotEmpty()
    }

    fun isLoadingVouchers(): Boolean {
        return _isLoadingVouchers.value
    }

    fun isValidatingVoucher(): Boolean {
        return _isValidatingVoucher.value
    }

    // Hàm clear error state
    fun clearOrderError() {
        _orderError.value = null
        _orderResult.value = null
    }

    fun clearVoucherError() {
        _voucherError.value = null
    }

    fun getShippingFee(): Double {
        return _shippingFee.value
    }

    companion object {
        fun getFactory(
            profileRepository: ProfileRepository? = null,
            orderRepository: OrderRepository? = null,
            voucherRepository: VoucherRepository? = null
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
                        return PaymentViewModel(
                            profileRepository = profileRepository ?: ProfileRepository(),
                            orderRepository = orderRepository ?: OrderRepository(),
                            voucherRepository = voucherRepository ?: VoucherRepository()
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}