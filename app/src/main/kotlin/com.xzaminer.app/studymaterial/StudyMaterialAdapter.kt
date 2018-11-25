package com.xzaminer.app.studymaterial

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.highlightTextPart
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.commons.views.MyTextView
import com.xzaminer.app.R
import kotlinx.android.synthetic.main.question_item_grid.view.*
import java.util.*



class StudyMaterialAdapter(
    activity: StudyMaterialActivity, var questions: ArrayList<Question>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private var quizActivity: QuizActivity? = null
    var adjustedPrimaryColor = activity.getAdjustedPrimaryColor()

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_empty

    override fun prepareItemSelection(viewHolder: ViewHolder) {
    }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(R.layout.study_material_item_grid, parent)
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val cat = questions.getOrNull(position) ?: return
        val view = holder.bindView(cat, true, false) { itemView, adapterPosition ->
            setupView(itemView, cat, adapterPosition)
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

    fun updateQuestions(newQuestions: ArrayList<Question>) {
        questions = newQuestions.clone() as ArrayList<Question>
        notifyDataSetChanged()
    }

    private fun setupView(view: View, question: Question, adapterPosition: Int) {
        view.apply {
            question_text.text = "${adapterPosition + 1}. ${question.text}"
            val rootLayout = (view as CardView).getChildAt(0) as ConstraintLayout
            for(i in 2 until rootLayout.childCount) {
                if(i < question.options.size + 2) {
                    val option = question.options[i - 2]
                    val text = rootLayout.getChildAt(i) as MyTextView
                    if(question.selectedAnswer == option.id) {
                        text.text = option.text!!.highlightTextPart(option.text!!, adjustedPrimaryColor)
                    } else {
                        text.text = option.text!!
                    }
                } else {
                    rootLayout.getChildAt(i).beGone()
                }
            }
        }
    }

    private fun getLetter(index: Int): String {
        if(index == 0) { return "A. " }
        if(index == 1) { return "B. " }
        if(index == 2) { return "C. " }
        if(index == 3) { return "D. " }
        if(index == 4) { return "E. " }
        if(index == 5) { return "F. " }
        if(index == 6) { return "G. " }
        if(index == 7) { return "H. " }
        return "A. "
    }
}
