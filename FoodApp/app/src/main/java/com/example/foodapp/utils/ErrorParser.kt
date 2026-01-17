package com.example.foodapp.utils

import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Utility để parse error response từ API và trả về message thân thiện
 */
object ErrorParser {

    private val gson = Gson()

    /**
     * Parse error body string và trả về message thân thiện
     */
    fun parseError(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return "Đã xảy ra lỗi không xác định"
        }

        return try {
            // Try to parse as JSON
            val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)

            // Try to get message from common fields
            when {
                jsonObject.has("message") -> {
                    val message = jsonObject.get("message").asString
                    translateErrorMessage(message)
                }
                jsonObject.has("error") -> {
                    val error = jsonObject.get("error")
                    if (error.isJsonPrimitive) {
                        translateErrorMessage(error.asString)
                    } else if (error.isJsonObject && error.asJsonObject.has("message")) {
                        translateErrorMessage(error.asJsonObject.get("message").asString)
                    } else {
                        "Đã xảy ra lỗi"
                    }
                }
                jsonObject.has("errors") -> {
                    // Handle validation errors array
                    val errors = jsonObject.getAsJsonArray("errors")
                    if (errors.size() > 0) {
                        val firstError = errors[0].asJsonObject
                        if (firstError.has("message")) {
                            translateErrorMessage(firstError.get("message").asString)
                        } else {
                            "Dữ liệu không hợp lệ"
                        }
                    } else {
                        "Dữ liệu không hợp lệ"
                    }
                }
                else -> "Đã xảy ra lỗi"
            }
        } catch (e: Exception) {
            // Not JSON, return as is but cleaned up
            translateErrorMessage(errorBody.take(100))
        }
    }

    /**
     * Translate common error messages to Vietnamese
     */
    private fun translateErrorMessage(message: String): String {
        return when {
            // Network errors
            message.contains("timeout", ignoreCase = true) -> "Kết nối quá chậm. Vui lòng thử lại"
            message.contains("network", ignoreCase = true) -> "Lỗi kết nối mạng"
            message.contains("connection", ignoreCase = true) -> "Không thể kết nối đến máy chủ"

            // Auth errors
            message.contains("unauthorized", ignoreCase = true) -> "Phiên đăng nhập đã hết hạn"
            message.contains("forbidden", ignoreCase = true) -> "Bạn không có quyền thực hiện thao tác này"
            message.contains("not found", ignoreCase = true) -> "Không tìm thấy dữ liệu"

            // Validation errors
            message.contains("invalid", ignoreCase = true) && message.contains("phone", ignoreCase = true) ->
                "Số điện thoại không hợp lệ"
            message.contains("Vietnamese phone number", ignoreCase = true) ->
                "Số điện thoại phải là số điện thoại Việt Nam hợp lệ (VD: 0901234567)"
            message.contains("invalid", ignoreCase = true) && message.contains("email", ignoreCase = true) ->
                "Email không hợp lệ"
            message.contains("required", ignoreCase = true) -> "Vui lòng điền đầy đủ thông tin"
            message.contains("already exists", ignoreCase = true) -> "Dữ liệu đã tồn tại"

            // File upload errors
            message.contains("file", ignoreCase = true) && message.contains("type", ignoreCase = true) ->
                "Định dạng file không được hỗ trợ. Chỉ chấp nhận JPG, PNG"
            message.contains("file", ignoreCase = true) && message.contains("size", ignoreCase = true) ->
                "File quá lớn. Kích thước tối đa là 5MB"
            message.contains("Only JPEG and PNG", ignoreCase = true) ->
                "Chỉ hỗ trợ ảnh định dạng JPEG và PNG"
            message.contains("5MB", ignoreCase = true) ->
                "Kích thước ảnh không được vượt quá 5MB"

            // Server errors
            message.contains("internal server error", ignoreCase = true) ->
                "Lỗi hệ thống. Vui lòng thử lại sau"
            message.contains("500", ignoreCase = true) ->
                "Lỗi hệ thống. Vui lòng thử lại sau"

            // Default: return message but clean up
            else -> message
                .replace(Regex("\""), "")
                .replace(Regex("\\{|\\}"), "")
                .replace("success:false,", "")
                .replace("success: false,", "")
                .replace("message:", "")
                .trim()
                .ifBlank { "Đã xảy ra lỗi" }
        }
    }

    /**
     * Parse exception to user-friendly message
     */
    fun parseException(exception: Throwable): String {
        return when {
            exception.message?.contains("Unable to resolve host") == true ->
                "Không có kết nối internet"
            exception.message?.contains("timeout") == true ->
                "Kết nối quá chậm. Vui lòng thử lại"
            exception.message?.contains("SSL") == true ->
                "Lỗi bảo mật kết nối"
            else -> parseError(exception.message)
        }
    }
}
