package com.xzaminer.app.billing

data class Purchase(
        val id: String = "",
        val name: String = "",
        val desc: String = "",
        val type: String = "",
        val actualPrice: String = "",
        val originalPrice: String = "",
        val showPurchase: Boolean = false,
        val expiry: String? = null,
        var details: String? = null,
        val extraPurchaseInfo: String? = null,
        var purchased: String? = null)
