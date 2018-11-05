package com.xzaminer.app.extensions

import com.bumptech.glide.signature.ObjectKey
import com.simplemobiletools.commons.helpers.OTG_PATH
import java.io.File
import java.io.IOException

fun String.getFileSignature(): ObjectKey {
    val file = File(this)
    return ObjectKey("${file.absolutePath}${file.lastModified()}")
}

fun String.isThisOrParentIncluded(includedPaths: MutableSet<String>) = includedPaths.any { startsWith(it, true) }

fun String.isThisOrParentExcluded(excludedPaths: MutableSet<String>) = excludedPaths.any { startsWith(it, true) }

// recognize /sdcard/DCIM as the same folder as /storage/emulated/0/DCIM
fun String.getDistinctPath(): String {
    return try {
        if (startsWith(OTG_PATH)) toLowerCase() else File(this).canonicalPath.toLowerCase()
    } catch (e: IOException) {
        toLowerCase()
    }
}
