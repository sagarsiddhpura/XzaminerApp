package com.xzaminer.app.course

import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.loadIconImageView
import com.xzaminer.app.extensions.loadImageImageView
import com.xzaminer.app.studymaterial.StudyMaterial
import kotlinx.android.synthetic.main.course_study_material_item_grid.view.*


class CourseQuestionBanksAdapter(
    activity: SimpleActivity, var questionBanks: ArrayList<StudyMaterial>, recyclerView: MyRecyclerView, val orientation: Int,
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
        val questionBank = questionBanks.getOrNull(position) ?: return
        val view = holder.bindView(questionBank, true, false) { itemView, adapterPosition ->
            setupView(itemView, questionBank)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = questionBanks.size

    override fun prepareActionMode(menu: Menu) {
        return
    }

    override fun actionItemPressed(id: Int) {
        return
    }

    override fun getSelectableItemCount() = questionBanks.size

    override fun getIsItemSelectable(position: Int) = true

    private fun setupView(view: View, questionBank: StudyMaterial) {
        view.apply {
            cat_name.text = questionBank.name
            if(questionBank.imageIcon != null) {
                activity.loadImageImageView(questionBank.imageIcon!!, cat_image, false, cat_name)
            } else {
                val img : Int = R.drawable.im_placeholder
                activity.loadIconImageView(img, cat_image, false)
            }
        }
    }
}
