package com.xzaminer.app.extensions

import android.content.Context
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.xzaminer.app.R
import com.xzaminer.app.category.Category
import com.xzaminer.app.data.DataSource
import com.xzaminer.app.data.DebugDataSource
import com.xzaminer.app.data.MySquareImageView
import com.xzaminer.app.utils.Config
import com.xzaminer.app.utils.GlideApp



val Context.windowManager: WindowManager get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager
val Context.dataSource: DataSource get() = DataSource()
val Context.debugDataSource: DebugDataSource get() = DebugDataSource()
val Context.config: Config get() = Config.newInstance(applicationContext)

fun Context.launchSettings() {
//    startActivity(Intent(applicationContext, SettingsActivity::class.java))
}

fun Context.loadImage(type: Int, path: String, target: MySquareImageView, horizontalScroll: Boolean, cropThumbnails: Boolean) {
    target.isHorizontalScrolling = horizontalScroll
    val options = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .format(DecodeFormat.PREFER_ARGB_8888)

    if (cropThumbnails) options.centerCrop() else options.fitCenter()

    GlideApp.with(applicationContext)
            .load(dataSource.getStorage().getReference(path))
            .apply(options)
            .placeholder(R.drawable.im_placeholder)
            .into(target)

}

fun Context.loadIcon(path: Int, target: MySquareImageView, cropThumbnails: Boolean) {
    val options = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .format(DecodeFormat.PREFER_ARGB_8888)

    val builder = Glide.with(applicationContext)
            .asBitmap()
            .load(path)

    if (cropThumbnails) options.centerCrop() else options.fitCenter()
    builder.apply(options).into(target)
}

fun Context.loadPng(path: String, target: MySquareImageView, cropThumbnails: Boolean) {
    val options = RequestOptions()
            .signature(path.getFileSignature())
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .format(DecodeFormat.PREFER_ARGB_8888)

    val builder = Glide.with(applicationContext)
            .asBitmap()
            .load(path)

    if (cropThumbnails) options.centerCrop() else options.fitCenter()
    builder.apply(options).into(target)
}

fun Context.loadJpg(path: String, target: MySquareImageView, cropThumbnails: Boolean) {
    val options = RequestOptions()
            .signature(path.getFileSignature())
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    val builder = Glide.with(applicationContext)
            .load(path)

    if (cropThumbnails) options.centerCrop() else options.fitCenter()
    builder.apply(options)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(target)
}

fun Context.getCategoriesFromDb(catId: Long?, callback: (ArrayList<Category>, name: String) -> Unit) {
    dataSource.getChildCategories(catId) { cats: ArrayList<Category>, name: String ->
        callback(cats, name)
    }
}
