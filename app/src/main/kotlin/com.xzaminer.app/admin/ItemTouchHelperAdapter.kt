package com.xzaminer.app.admin

import android.widget.ImageView
import android.widget.TextView

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
    fun onItemDismiss(position: Int)
    fun loadImageImageView(
        url: String,
        imageView: ImageView,
        cropThumbnails: Boolean,
        textView: TextView?,
        hasRoundEdges: Boolean,
        im_placeholder_video: Int
    )

    fun deleteEntity(entity: String)
}
