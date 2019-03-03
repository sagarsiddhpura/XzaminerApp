package com.xzaminer.app.course

import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.STUDY_MATERIAL_TYPE_STUDY_MATERIAL

data class CourseSection (
    var id: Long = 0,
    var name: String = "",
    var desc: String? = null,
    var image: String? = null,
    var studyMaterials: HashMap<String, StudyMaterial> = hashMapOf(),
    var type: String = STUDY_MATERIAL_TYPE_STUDY_MATERIAL,
    val purchaseInfo: ArrayList<Purchase> = arrayListOf(),
    var order: Int = 0,
    var isVisible: Boolean = true
    ) {
        override fun toString(): String {
            return "$name:::$desc"
        }

    fun fetchStudyMaterialById(studyMaterialId: Long): @ParameterName(name = "course") StudyMaterial? {
        if(!studyMaterials.isEmpty()) {
            return studyMaterials.values.find { it -> (it != null && it.id == studyMaterialId) }
        }
        return null
    }

    fun fetchVisiblePurchases(): List<Purchase> {
        if(!purchaseInfo.isEmpty()) {
            return purchaseInfo.filter { it.showPurchase }
        }
        return arrayListOf()
    }

    fun fetchVisibleStudyMaterials(): ArrayList<StudyMaterial> {
        val arrayList =
            ArrayList(studyMaterials.values.filter { it != null && it.isVisible })
        if(!arrayList.isEmpty()) {arrayList.sortWith (compareBy { it.order })}
        return arrayList
    }
}
