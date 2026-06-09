package com.cecar.smartbookmobile.features.common

data class FormStatus(
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)
