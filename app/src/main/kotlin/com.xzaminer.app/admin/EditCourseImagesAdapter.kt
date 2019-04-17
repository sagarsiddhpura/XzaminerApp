package com.xzaminer.app.admin

import android.support.v4.view.MotionEventCompat
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.extensions.getXzaminerDataDir
import com.xzaminer.app.extensions.loadImageImageView
import kotlinx.android.synthetic.main.course_image_grid.view.*
import java.io.File
import java.util.*


class EditCourseImagesAdapter (
    activity: EditCourseImagesActivity, var entities: ArrayList<String>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick), ItemTouchHelperAdapter {

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        parentActivity?.onItemMove(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
    }

    private var parentActivity: EditCourseImagesActivity? = null
    var adjustedPrimaryColor = activity.getAdjustedPrimaryColor()
    private var xzaminerDataDir: File

    init {
        setupDragListener(true)
        this.parentActivity = activity
        xzaminerDataDir = activity.getXzaminerDataDir()
    }

    override fun getActionMenuId() = com.xzaminer.app.R.menu.cab_empty

    override fun prepareItemSelection(viewHolder: ViewHolder) {
    }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(com.xzaminer.app.R.layout.course_image_grid, parent)
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val entity = entities.getOrNull(position) ?: return
        val view = holder.bindView(entity, true, false) { itemView, adapterPosition ->
            setupView(itemView, entity, position, holder)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = entities.size

    override fun prepareActionMode(menu: Menu) {
        return
    }

    override fun actionItemPressed(id: Int) {
        return
    }

    override fun getSelectableItemCount() = entities.size

    override fun getIsItemSelectable(position: Int) = true

    fun updateEntities(newEntities: ArrayList<String>) {
        entities = newEntities.clone() as ArrayList<String>
        notifyDataSetChanged()
    }

    private fun setupView(
        view: View,
        image: String,
        position: Int,
        holder: ViewHolder
    ) {
        view.apply {
            move_icon.setColorFilter(resources.getColor(com.xzaminer.app.R.color.md_blue_800_dark))
            manage_delete.setColorFilter(resources.getColor(com.xzaminer.app.R.color.md_blue_800_dark))
            activity.loadImageImageView(image, manage_image, false, null, false, com.xzaminer.app.R.drawable.im_placeholder_video)

            manage_delete.setOnClickListener {
                parentActivity?.deleteEntity(image)
            }

            manage_image.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        parentActivity?.onStartDrag(holder)
                    }
                    return false
                }
            })
        }
    }
}
