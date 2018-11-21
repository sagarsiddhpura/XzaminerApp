package com.xzaminer.app.category

import com.xzaminer.app.course.Course

data class Category (
    var id: Long = 0,
    var name: String? = null,
    var description: String? = null,
    var image: String? = null,
    var subCategories: HashMap<String, Category>? = null,
    var courses: HashMap<String, Course>? = null,
    var isCourse: Boolean = false)
    {
        override fun toString(): String {
            return "$name:::$description"
        }
}
