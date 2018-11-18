package com.xzaminer.app.billing

data class Purchase(
        val id: String = "",
        val name: String = "",
        val type: String = "",
        val expiry: String? = null,
        val details: String? = null,
        val purchased: String? = null) {
}
