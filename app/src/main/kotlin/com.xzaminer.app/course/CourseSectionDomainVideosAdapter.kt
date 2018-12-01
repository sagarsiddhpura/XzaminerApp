package com.xzaminer.app.course

import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.loadIconImageView
import com.xzaminer.app.extensions.loadImageImageView
import com.xzaminer.app.studymaterial.Video
import kotlinx.android.synthetic.main.course_study_material_item_grid.view.*


class CourseSectionDomainVideosAdapter(
    activity: SimpleActivity, var videos: ArrayList<Video>, recyclerView: MyRecyclerView, val orientation: Int,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    override fun getActionMenuId() = R.menu.cab_empty

    override fun prepareItemSelection(viewHolder: ViewHolder) { }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) { }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if(orientation == GridLayoutManager.VERTICAL) {
            return createViewHolder(R.layout.course_study_material_item_grid_vertical, parent)
        } else {
            return createViewHolder(R.layout.course_study_material_item_grid, parent)
        }
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val video = videos.getOrNull(position) ?: return
        val view = holder.bindView(video, true, false) { itemView, adapterPosition ->
            setupView(itemView, video)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = videos.size

    override fun prepareActionMode(menu: Menu) {
        return
    }

    override fun actionItemPressed(id: Int) {
        return
    }

    override fun getSelectableItemCount() = videos.size

    override fun getIsItemSelectable(position: Int) = true


    private fun setupView(view: View, video: Video) {
        view.apply {
            cat_name.text = video.name
            cat_name.beVisible()
            if(video.thumbnail != null && video.thumbnail != "") {
                activity.loadImageImageView(video.thumbnail!!, cat_image, false, cat_name, R.drawable.im_placeholder_video)
            } else {
                val img : Int = R.drawable.im_placeholder_video
                activity.loadIconImageView(img, cat_image, false)
            }
        }
    }
}
