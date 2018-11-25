package com.xzaminer.app.course

import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.loadImage
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.TYPE_IMAGES
import kotlinx.android.synthetic.main.category_item_grid.view.*


class CourseStudyMaterialsAdapter(
    activity: SimpleActivity, var studyMaterials: ArrayList<StudyMaterial>, recyclerView: MyRecyclerView, val orientation: Int,
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
        val studyMaterial = studyMaterials.getOrNull(position) ?: return
        val view = holder.bindView(studyMaterial, true, false) { itemView, adapterPosition ->
            setupView(itemView, studyMaterial)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = studyMaterials.size

    override fun prepareActionMode(menu: Menu) {
        return
    }

    override fun actionItemPressed(id: Int) {
        return
    }

    override fun getSelectableItemCount() = studyMaterials.size

    override fun getIsItemSelectable(position: Int) = true

    private fun setupView(view: View, studyMaterial: StudyMaterial) {
        view.apply {
            cat_name.text = studyMaterial.name
            activity.loadImage(TYPE_IMAGES, studyMaterial.imageIcon!!, cat_thumbnail, false, false)
        }
    }
}
