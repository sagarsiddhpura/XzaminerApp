package com.xzaminer.app.course

import android.content.Context
import android.widget.ImageView
import com.xzaminer.app.R
import com.xzaminer.app.extensions.loadImageImageView
import ss.com.bannerslider.ImageLoadingService


class PicassoImageLoadingService(var context: Context) : ImageLoadingService {

    override fun loadImage(url: String, imageView: ImageView) {
        context.loadImageImageView(url, imageView, false, null, R.drawable.im_desc_placeholder)
    }

    override fun loadImage(resource: Int, imageView: ImageView) {

    }

    override fun loadImage(url: String, placeHolder: Int, errorDrawable: Int, imageView: ImageView) {
        context.loadImageImageView(url, imageView, false, null)
    }
}