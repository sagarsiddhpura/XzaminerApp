package com.xzaminer.app.quiz

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
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
import kotlinx.android.synthetic.main.category_item_grid.view.*
import kotlinx.android.synthetic.main.question_item_grid.view.*
import java.util.*



class QuestionsAdapter(activity: BaseSimpleActivity, var questions: ArrayList<Question>, recyclerView: MyRecyclerView,
                       itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private var currentQuestionsHash = questions.hashCode()
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
        return createViewHolder(R.layout.question_item_grid, parent)
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val cat = questions.getOrNull(position) ?: return
        val view = holder.bindView(cat, true, false) { itemView, adapterPosition ->
            setupView(itemView, cat)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = questions.size

    override fun prepareActionMode(menu: Menu) {
        return
    }

    override fun actionItemPressed(id: Int) {
        return
    }

    override fun getSelectableItemCount() = questions.size

    override fun getIsItemSelectable(position: Int) = true

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isActivityDestroyed()) {
            Glide.with(activity).clear(holder.itemView?.cat_thumbnail!!)
        }
    }

    fun updateQuestions(newQuestions: ArrayList<Question>) {
        val newquestionsClone = newQuestions.clone() as ArrayList<Question>
        if (newquestionsClone.hashCode() != currentQuestionsHash) {
            currentQuestionsHash = newquestionsClone.hashCode()
            questions = newquestionsClone
            notifyDataSetChanged()
            finishActMode()
        }
    }

    private fun setupView(view: View, question: Question) {
        view.apply {
            question_text.text = question.text
            option_1.text = question.options[0].text
            option_2.text = question.options[1].text
            option_3.text = question.options[2].text
            option_4.text = question.options[3].text
        }
    }
}
