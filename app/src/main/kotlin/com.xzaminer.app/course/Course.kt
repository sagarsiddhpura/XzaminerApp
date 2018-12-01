package com.xzaminer.app.course

import com.xzaminer.app.billing.Purchase

data class Course (
    var id: Long = 0,
    var name: String? = null,
    var description: String? = null,
    var imageIcon: String? = null,
    var sections: HashMap<String, CourseSection> = hashMapOf(),
    val purchaseInfo: ArrayList<Purchase> = arrayListOf(),
    val descImages: ArrayList<String> = arrayListOf(),
    val shortName: String? = null
    ) {
        override fun toString(): String {
            return "$name:::$description"
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
}
