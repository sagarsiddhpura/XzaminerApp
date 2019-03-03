package com.xzaminer.app.course

import com.xzaminer.app.billing.Purchase

data class Course (
    var id: Long = 0,
    var name: String? = null,
    var desc: String? = null,
    var image: String? = null,
    var sections: HashMap<String, CourseSection> = hashMapOf(),
    val purchaseInfo: ArrayList<Purchase> = arrayListOf(),
    val descImages: ArrayList<String> = arrayListOf(),
    var shortName: String? = null,
    var order: Int = 0,
    var isVisible: Boolean = true
    ) {
        override fun toString(): String {
            return "$name:::$desc"
        }

    fun fetchVisibleSections(): ArrayList<CourseSection> {
        val arrayList =
            ArrayList(sections.values.filter { it -> it != null && it.isVisible && !it.studyMaterials.isEmpty() })
        if(!arrayList.isEmpty()) {arrayList.sortWith (compareBy { it.order })}
        return arrayList
    }

    fun fetchSection(sectionId: Long): CourseSection? {
        return sections.values.find { it -> it.id == sectionId }
    }

    fun fetchVisiblePurchases(): List<Purchase> {
        if(!purchaseInfo.isEmpty()) {
            return purchaseInfo.filter { it.showPurchase }
        }
        return arrayListOf()
    }

    fun fetchPurchaseById(productId: String): Purchase? {
        fetchVisiblePurchases().forEach {
            if(it.id == productId) {
                return it
            }
        }

        sections.values.forEach {
            it.fetchVisiblePurchases().forEach { purchaseSection ->
                if(purchaseSection.id == productId) {
                    return purchaseSection
                }
            }
        }

        return null
    }

    fun fetchAllSections(): ArrayList<CourseSection> {
        return ArrayList(sections.values)
    }
}
