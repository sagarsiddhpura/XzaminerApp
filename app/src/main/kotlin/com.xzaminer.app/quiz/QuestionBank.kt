package com.xzaminer.app.quiz

import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.utils.PURCHASE_TYPE_IAP

data class QuestionBank (
    var id: Long = 0,
    var name: String? = null,
    var description: String? = null,
    var length: String? = null,
    var imageIcon: String? = null,
    var openCount: Int = 0,
    var properties : HashMap<String, ArrayList<String>> = hashMapOf(),
    var questions: ArrayList<Question>? = null,
    val purchaseInfo: ArrayList<Purchase> = arrayListOf()
    ) {
    fun getIapPurchase(): Purchase? {
        purchaseInfo.forEach {
            if(it.type == PURCHASE_TYPE_IAP) {
                return it
            }
        }
        return null
    }

    // genre
}
