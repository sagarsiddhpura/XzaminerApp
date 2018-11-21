package com.xzaminer.app.course

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.extensions.loadImage
import com.xzaminer.app.quiz.QuestionBank
import com.xzaminer.app.utils.TYPE_IMAGES
import kotlinx.android.synthetic.main.category_item_grid.view.*


class CourseQuestionBanksAdapter(
    activity: CourseActivity, var questionBanks: ArrayList<QuestionBank>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    override fun getActionMenuId() = R.menu.cab_empty

    override fun prepareItemSelection(viewHolder: ViewHolder) { }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) { }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(R.layout.course_study_material_item_grid, parent)
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

    private fun setupView(view: View, questionBank: QuestionBank) {
        view.apply {
            cat_name.text = questionBank.name
            activity.loadImage(TYPE_IMAGES, questionBank.imageIcon!!, cat_thumbnail, false, false)
        }
    }
}
