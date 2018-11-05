package com.xzaminer.app.category

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.extensions.isActivityDestroyed
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.loadIcon
import com.xzaminer.app.extensions.loadImage
import com.xzaminer.app.utils.TYPE_IMAGES
import kotlinx.android.synthetic.main.directory_item_grid.view.*
import java.util.*



class CategoriesAdapter(activity: BaseSimpleActivity, var cats: ArrayList<Category>, recyclerView: MyRecyclerView,
                        itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private val config = activity.config
    private var currentCategoriesHash = cats.hashCode()
    private var colorDrawable : GradientDrawable

    init {
        setupDragListener(true)
        colorDrawable = GradientDrawable()
        colorDrawable.setColor(primaryColor.adjustAlpha(0.6f))
        colorDrawable.cornerRadius = Resources.getSystem().displayMetrics.density*4

    }

    override fun getActionMenuId() = R.menu.cab_empty

    override fun prepareItemSelection(viewHolder: ViewHolder) {
    }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(R.layout.directory_item_grid, parent)
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val cat = cats.getOrNull(position) ?: return
        val view = holder.bindView(cat, true, false) { itemView, adapterPosition ->
            setupView(itemView, cat)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = cats.size

    override fun prepareActionMode(menu: Menu) {
        return
    }

    override fun actionItemPressed(id: Int) {
        return
    }

    override fun getSelectableItemCount() = cats.size

    override fun getIsItemSelectable(position: Int) = true

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isActivityDestroyed()) {
            Glide.with(activity).clear(holder.itemView?.dir_thumbnail!!)
        }
    }

    fun updateDirs(newDirs: ArrayList<Category>) {
        val directories = newDirs.clone() as ArrayList<Category>
        if (directories.hashCode() != currentCategoriesHash) {
            currentCategoriesHash = directories.hashCode()
            cats = directories
            notifyDataSetChanged()
            finishActMode()
        }
    }

    private fun setupView(view: View, category: Category) {
        view.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dir_shadow_holder.clipToOutline = true
                dir_thumbnail.clipToOutline = true
            }

            dir_shadow_holder.setImageDrawable(colorDrawable)
            dir_name.text = category.name
            val thumbnailType = TYPE_IMAGES

            if(category.image == "" || category.image == null) {
                val img : Int = R.drawable.im_1
                activity.loadIcon(img, dir_thumbnail, true)
            } else {
                activity.loadImage(thumbnailType, category.image!!, dir_thumbnail, false, true)
            }
        }
    }
}
