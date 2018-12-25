package com.xzaminer.app.category

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.isActivityDestroyed
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.loadIconImageView
import com.xzaminer.app.extensions.loadImageImageView
import kotlinx.android.synthetic.main.category_item_grid.view.*
import kotlinx.android.synthetic.main.course_item.view.*
import kotlinx.android.synthetic.main.thumbnail_section.view.*
import java.util.*



class CategoriesAdapter(activity: BaseSimpleActivity, var cats: ArrayList<Category>, recyclerView: MyRecyclerView,
                        itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private val config = activity.config
    private var currentCategoriesHash = cats.hashCode()
    private var colorDrawable : GradientDrawable
    private val ITEM_SECTION = 0
    private val ITEM_MEDIUM = 1

    init {
        setupDragListener(true)
        colorDrawable = GradientDrawable()
        colorDrawable.setColor(primaryColor.adjustAlpha(0.6f))
        colorDrawable.cornerRadius = Resources.getSystem().displayMetrics.density*4
    }

    override fun getActionMenuId() = R.menu.cab_empty

    fun isASectionTitle(position: Int) = cats.getOrNull(position)!!.id == -1L

    override fun prepareItemSelection(viewHolder: ViewHolder) {
    }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutType = if (viewType == ITEM_SECTION) {
            R.layout.thumbnail_section
        } else {
            R.layout.course_item
        }
        return createViewHolder(layoutType, parent)
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val cat = cats.getOrNull(position) ?: return
        val view = holder.bindView(cat, true, false) { itemView, adapterPosition ->
            if (cat.id == -1L) {
                setupSection(itemView, cat)
            } else {
                setupView(itemView, cat)
            }
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = cats.size

    override fun getItemViewType(position: Int): Int {
        val tmbItem = cats[position]
        return if (tmbItem.id == -1L) {
            ITEM_SECTION
        } else {
            ITEM_MEDIUM
        }
    }

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
            if(holder.itemView != null && holder.itemView.cat_thumbnail != null) {
                Glide.with(activity).clear(holder.itemView.cat_thumbnail)
            }
        }
    }

    fun updateCategories(newDirs: ArrayList<Category>) {
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
            course_name.text = category.name
            course_desc.text = category.desc
            course_subtitle.beGone()
            if(category.image != null && category.image != "") {
                activity.loadImageImageView(category.image!!, course_image, false, null, false, R.drawable.im_placeholder_video)
            } else {
                val img : Int = R.drawable.im_placeholder
                activity.loadIconImageView(img, course_image, false)
            }
        }
    }

    private fun setupSection(view: View, section: Category) {
        view.apply {
            thumbnail_section.text = section.name
            thumbnail_section.setTextColor(activity.getAdjustedPrimaryColor())
        }
    }
}
