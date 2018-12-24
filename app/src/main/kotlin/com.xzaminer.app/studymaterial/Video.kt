package com.xzaminer.app.studymaterial

data class Video (
    var id: Long = 0,
    var name: String? = null,
    var desc: String? = null,
    var image: String? = null,
    var fileName: String? = null,
    var url: String? = null,
    var localFile: String? = null,
    var order: Int = 0,
    var duration: String = ""
)
