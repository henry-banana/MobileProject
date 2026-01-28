package com.example.foodapp.pages.client.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.model.shared.category.Category
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.product.ProductFilterDto
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.client.response.product.SearchProductsApiResponse
import com.example.foodapp.data.remote.shared.response.ApiResult as SharedApiResult
import com.example.foodapp.data.remote.client.response.product.ApiResult as ProductApiResult
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.data.repository.client.products.ProductRepository
import com.example.foodapp.data.repository.shared.CategoryRepository
import com.example.foodapp.data.remote.client.response.product.SearchProductsRequestDto
import com.example.foodapp.data.remote.client.response.product.toProductList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============== PRODUCT STATES ==============

sealed class ProductState {
    object Idle : ProductState()
    object Loading : ProductState()
    data class Success(val products: List<Product>) : ProductState()
    data class Error(val message: String) : ProductState()
    object Empty : ProductState()
}

sealed class UserNameState {
    object Idle : UserNameState()
    object Loading : UserNameState()
    data class Success(val userName: String) : UserNameState()
    data class Error(val message: String) : UserNameState()
    object Empty : UserNameState()
}

sealed class CategoryState {
    object Idle : CategoryState()
    object Loading : CategoryState()
    data class Success(val categories: List<Category>) : CategoryState()
    data class Error(val message: String) : CategoryState()
}

// ============== HOME VIEW MODEL ==============

class HomeViewModel(
    private val userRepository: UserFirebaseRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _userNameState = MutableLiveData<UserNameState>(UserNameState.Idle)
    val userNameState: LiveData<UserNameState> = _userNameState

    private val _productState = MutableLiveData<ProductState>(ProductState.Idle)
    val productState: LiveData<ProductState> = _productState

    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData(1)
    val totalPages: LiveData<Int> = _totalPages

    private val _totalItems = MutableLiveData(0)
    val totalItems: LiveData<Int> = _totalItems

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> = _hasMore

    private val _currentFilters = MutableLiveData<ProductFilterDto>(ProductFilterDto())
    val currentFilters: LiveData<ProductFilterDto> = _currentFilters

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _isSearching = MutableLiveData<Boolean>(false) // THÊM: Trạng thái đang search fuzzy
    val isSearching: LiveData<Boolean> = _isSearching

    private val _searchResults = MutableLiveData<List<Product>>(emptyList()) // THÊM: Kết quả search
    val searchResults: LiveData<List<Product>> = _searchResults

    private val _categoryState = MutableLiveData<CategoryState>(CategoryState.Idle)
    val categoryState: LiveData<CategoryState> = _categoryState

    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> = _categories

    private var searchJob: Job? = null

    // ============== USER NAME FUNCTIONS ==============

    fun fetchUserName() {
        _userNameState.value = UserNameState.Loading
        userRepository.getCurrentUserName { name ->
            if (!name.isNullOrBlank()) {
                _userNameState.postValue(UserNameState.Success(name))
            } else {
                _userNameState.postValue(UserNameState.Error("Không tìm thấy người dùng"))
            }
        }
    }

    fun clearUserName() {
        _userNameState.value = UserNameState.Empty
    }

    // ============== PRODUCT FUNCTIONS ==============

    fun getProducts(
        filters: ProductFilterDto = ProductFilterDto(),
        forceRefresh: Boolean = false
    ) {
        // Nếu đang search fuzzy, không gọi getProducts thông thường
        if (_isSearching.value == true && filters.searchQuery.isNullOrBlank()) {
            return
        }

        if (forceRefresh) {
            _currentPage.value = filters.page
            _hasMore.value = true
        }

        _productState.value = ProductState.Loading
        _currentFilters.value = filters

        viewModelScope.launch {
            try {
                val result = productRepository.getProducts(filters)
                println("DEBUG: [ViewModel] Result type: ${result::class.simpleName}")

                when (result) {
                    is ProductApiResult.Success<*> -> {
                        println("DEBUG: [ViewModel] Entered Success branch")

                        val apiResponse = result.data

                        if (apiResponse is com.example.foodapp.data.remote.client.response.product.ProductApiResponse) {
                            val products = apiResponse.toProductList()
                            val productListData = apiResponse.data

                            println("DEBUG: [ViewModel] Products count: ${products.size}")

                            if (productListData != null) {
                                val total = productListData.total
                                val page = productListData.page
                                val limit = productListData.limit

                                _totalItems.value = total
                                _currentPage.value = page
                                _totalPages.value = if (limit > 0) {
                                    kotlin.math.ceil(total.toDouble() / limit.toDouble()).toInt()
                                } else {
                                    1
                                }
                                _hasMore.value = page < _totalPages.value!!

                                println("DEBUG: [ViewModel] Pagination: Page $page/${_totalPages.value}, Total: $total, Limit: $limit")
                            }

                            if (filters.page == 1 || forceRefresh) {
                                _products.value = products
                            } else {
                                val currentList = _products.value ?: emptyList()
                                _products.value = currentList + products
                            }

                            if (products.isNotEmpty()) {
                                _productState.value = ProductState.Success(_products.value ?: emptyList())
                                println("DEBUG: [ViewModel] State set to Success")
                            } else {
                                if (_products.value?.isEmpty() == true) {
                                    _productState.value = ProductState.Empty
                                }
                                println("DEBUG: [ViewModel] State set to Empty or Success")
                            }
                        } else {
                            val products = (result.data as? List<Product>) ?: emptyList()
                            _products.value = products

                            if (products.isNotEmpty()) {
                                _productState.value = ProductState.Success(products)
                                _hasMore.value = products.size >= filters.limit
                            } else {
                                _productState.value = ProductState.Empty
                                _hasMore.value = false
                            }
                            _currentPage.value = 1
                        }
                    }
                    is ProductApiResult.Failure -> {
                        println("DEBUG: [ViewModel] Entered Failure branch: ${result.exception.message}")
                        _productState.value = ProductState.Error(
                            result.exception.message ?: "Lỗi không xác định"
                        )
                        _hasMore.value = false
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [ViewModel] Exception in getProducts: ${e.message}")
                e.printStackTrace()
                _productState.value = ProductState.Error(
                    e.message ?: "Lỗi kết nối"
                )
                _hasMore.value = false
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun goToPage(pageNumber: Int) {
        val totalPages = _totalPages.value ?: 1
        if (pageNumber < 1 || pageNumber > totalPages) return

        val currentFilters = _currentFilters.value ?: ProductFilterDto()
        val newFilters = currentFilters.copy(page = pageNumber)

        getProducts(newFilters)
    }

    fun loadMoreProducts() {
        if (_isSearching.value == true) {
            return // Không load more khi đang search fuzzy
        }

        val currentPage = _currentPage.value ?: 1
        val totalPages = _totalPages.value ?: 1

        if (currentPage >= totalPages || _isLoadingMore.value == true) {
            _hasMore.value = false
            return
        }

        goToPage(currentPage + 1)
    }

    fun filterByCategory(categoryId: String?) {
        println("DEBUG: [ViewModel] Filter by category: $categoryId")

        // Nếu đang search fuzzy, hủy search
        if (_isSearching.value == true) {
            clearSearch()
        }

        val currentFilters = _currentFilters.value ?: ProductFilterDto()
        val newFilters = currentFilters.copy(
            categoryId = categoryId,
            page = 1
        )
        _currentPage.value = 1
        getProducts(newFilters)
    }


    fun searchProducts(query: String) {
        println("DEBUG: [ViewModel] Fuzzy search products: '$query'")

        _searchQuery.value = query

        val trimmedQuery = query.trim()

        if (trimmedQuery.isEmpty()) {
            // Query rỗng => load TẤT CẢ sản phẩm
            loadAllProducts()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce 300ms

            try {
                _isSearching.value = true
                _productState.value = ProductState.Loading

                val searchRequest = SearchProductsRequestDto(
                    q = trimmedQuery,  // Dùng query đã trim
                    limit = 20
                )

                val result = productRepository.searchProductsList(searchRequest)

                when (result) {
                    is ProductApiResult.Success<*> -> {
                        val apiResponse = result.data as? SearchProductsApiResponse
                        if (apiResponse != null && apiResponse.isValid) {
                            // Lấy products từ response
                            val products = apiResponse.data?.products?.toProductList() ?: emptyList()
                            _searchResults.value = products

                            if (products.isNotEmpty()) {
                                _productState.value = ProductState.Success(products)
                                println("DEBUG: [ViewModel] Fuzzy search found ${products.size} products")
                            } else {
                                _productState.value = ProductState.Empty
                                println("DEBUG: [ViewModel] Fuzzy search returned empty results")
                            }
                        } else {
                            _productState.value = ProductState.Empty
                            println("DEBUG: [ViewModel] Search API response invalid")
                        }
                    }
                    is ProductApiResult.Failure -> {
                        _productState.value = ProductState.Error(
                            result.exception.message ?: "Lỗi tìm kiếm"
                        )
                        println("DEBUG: [ViewModel] Fuzzy search failed: ${result.exception.message}")
                    }
                }
            } catch (e: Exception) {
                _productState.value = ProductState.Error(
                    e.message ?: "Lỗi tìm kiếm"
                )
                println("DEBUG: [ViewModel] Exception in fuzzy search: ${e.message}")
            }
        }
    }

    // THÊM HÀM MỚI: Load tất cả sản phẩm (reset filters)
    fun loadAllProducts() {
        println("DEBUG: [ViewModel] Loading all products")

        _searchQuery.value = ""
        _isSearching.value = false
        _searchResults.value = emptyList()

        // Tạo filters mới để load TẤT CẢ sản phẩm (không có filter nào)
        val allProductsFilter = ProductFilterDto(
            page = 1,
            limit = 20
            // Các filter khác để null để load tất cả
        )

        _currentFilters.value = allProductsFilter
        _currentPage.value = 1
        getProducts(allProductsFilter, forceRefresh = true)
    }

    // Sửa lại clearSearch để dùng loadAllProducts
    fun clearSearch() {
        println("DEBUG: [ViewModel] Clearing search")

        searchJob?.cancel()
        searchJob = null

        loadAllProducts()
    }
    // ============== CATEGORY FUNCTIONS ==============

    fun fetchCategories() {
        _categoryState.value = CategoryState.Loading
        viewModelScope.launch {
            val result = categoryRepository.getCategories()
            when (result) {
                is SharedApiResult.Success<*> -> {
                    val categories = (result.data as? List<Category>) ?: emptyList()
                    _categories.value = categories
                    _categoryState.value = CategoryState.Success(categories)
                    println("DEBUG: [ViewModel] Fetched ${categories.size} categories")
                }
                is SharedApiResult.Failure -> {
                    _categoryState.value = CategoryState.Error(
                        result.exception.message ?: "Lỗi tải danh mục"
                    )
                    println("DEBUG: [ViewModel] Category fetch failed: ${result.exception.message}")
                }
            }
        }
    }

    fun refresh() {
        println("DEBUG: [ViewModel] Refresh called")
        clearSearch() // Clear search khi refresh
        val currentFilters = _currentFilters.value ?: ProductFilterDto()
        getProducts(currentFilters.copy(page = 1), forceRefresh = true)
        fetchUserName()
        fetchCategories()
    }

    fun reset() {
        _products.value = emptyList()
        _searchResults.value = emptyList()
        _productState.value = ProductState.Idle
        _currentPage.value = 1
        _totalPages.value = 1
        _totalItems.value = 0
        _hasMore.value = true
        _currentFilters.value = ProductFilterDto()
        _isLoadingMore.value = false
        _searchQuery.value = ""
        _isSearching.value = false
        _categories.value = emptyList()
        _categoryState.value = CategoryState.Idle
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val userRepository = UserFirebaseRepository(context)
                val apiService = ApiClient.productApiService
                val productRepository = ProductRepository()
                val categoryRepository = CategoryRepository()
                HomeViewModel(userRepository, productRepository, categoryRepository)
            }
        }
    }
}