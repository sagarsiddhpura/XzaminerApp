package com.xzaminer.app.billing

import com.anjlab.android.iab.v3.TransactionDetails
import com.xzaminer.app.data.User

data class PurchaseLog(
        val productId: String = "",
        val details: TransactionDetails? = null,
        val user: User? = null) {
}
