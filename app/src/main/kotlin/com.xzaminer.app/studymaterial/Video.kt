package com.xzaminer.app.studymaterial

data class Video (
    var id: Long = 0,
    var name: String = "",
    var desc: String? = null,
    var image: String? = null,
    var fileName: String? = null,
    var url: String = "",
    var localFile: String? = null,
    var order: Int = 0,
    var duration: String = "",
    var details: HashMap<String, ArrayList<String>> = hashMapOf()
    )
