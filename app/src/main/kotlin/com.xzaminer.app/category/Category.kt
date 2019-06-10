package com.xzaminer.app.category

import com.xzaminer.app.course.Course

data class Category (
    var id: Long = 0,
    var name: String? = null,
    var desc: String? = null,
    var image: String? = null,
    var subCategories: HashMap<String, Category>? = null,
    var courses: HashMap<String, Course>? = null,
    var isCourse: Boolean = false,
    var order: Int = 0,
    var isVisible: Boolean = true
    )
    {
        override fun toString(): String {
                return "$name:::$desc"
        }
    }
