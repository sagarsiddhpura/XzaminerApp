package com.xzaminer.app.course

import android.support.v7.widget.GridLayoutManager
import android.util.TypedValue
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
import com.xzaminer.app.studymaterial.StudyMaterial
import kotlinx.android.synthetic.main.course_study_material_item_grid.view.*


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
            setupView(itemView, studyMaterial, position)
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

    private fun setupView(view: View, studyMaterial: StudyMaterial, position: Int) {
        view.apply {
            cat_name.text = studyMaterial.name
            cat_name.setTextColor(resources.getColor(R.color.white))
            cat_name.beVisible()
            if(studyMaterial.imageIcon != null) {
                activity.loadImageImageView(studyMaterial.imageIcon!!, cat_image, false, cat_name, true, R.drawable.im_placeholder_h)
            } else {
                val img : Int = R.drawable.im_placeholder_h
                activity.loadIconImageView(img, cat_image, false)
            }

            if(orientation == GridLayoutManager.HORIZONTAL) {
                val layoutParams = view.layoutParams as (GridLayoutManager.LayoutParams)
                if(position == 0) {
                    val marginInDp = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 0F, resources
                            .displayMetrics
                    ).toInt()
                    layoutParams.setMargins(marginInDp, 0, 0, 0)
                } else if(position == studyMaterials.size-1) {
                    val marginInDp = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 0F, resources
                            .displayMetrics
                    ).toInt()
                    layoutParams.setMargins(0, 0, marginInDp, 0)
                } else {
                    layoutParams.setMargins(0, 0, 0, 0)
                }
            }

        }
    }
}
