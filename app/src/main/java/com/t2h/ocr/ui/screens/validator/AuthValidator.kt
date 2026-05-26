package com.t2h.ocr.ui.screens.validator

object AuthValidator{
    fun validateEmail(email: String): String {
        return when {
            email.isBlank() -> "Bạn chưa điền email"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                ->"Email không hợp lệ"
            else -> ""

        }
    }

    fun validatePassword(password: String): String {
        return when{
            password.isBlank() -> "Bạn chưa điền mật khẩu"
            password.length < 6 -> "Mật khẩu phải có ít nhất 6 ký tự"
            else -> ""
        }
    }

    fun validateConfirmPassword(confirmPassword: String,
                                password: String): String{
        return when{
            confirmPassword.isBlank() -> "Bạn chưa xác nhận mật khẩu"
            confirmPassword != password -> "Xác nhận mật khẩu phải giống với mật khẩu"
            else -> ""
        }
    }
}
