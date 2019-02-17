package com.xzaminer.app.admin

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.extensions.getXzaminerDataDir
import com.xzaminer.app.studymaterial.Question
import kotlinx.android.synthetic.main.study_material_item_grid.view.*
import java.io.File
import java.util.*


class EditStudyMaterialsQuestionsAdapter(
    activity: EditStudyMaterialQuestionsActivity, var questions: ArrayList<Question>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private var quizActivity: EditStudyMaterialQuestionsActivity? = null
    private var xzaminerDataDir: File

    init {
        setupDragListener(true)
        this.quizActivity = activity
        xzaminerDataDir = activity.getXzaminerDataDir()
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
            setupView(itemView, cat, position)
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
            divider_edit.beVisible()
            manage_root.beVisible()
            question_text.text = "${adapterPosition + 1}. ${question.text}"
            option_text.text = question.options.joinToString (separator = "\n\n")  { it -> "\u25CF  ${it.text}" }

            manage_edit.setOnClickListener {
                quizActivity?.editQuestion(question)
            }

            manage_delete.setOnClickListener {
                quizActivity?.deleteQuestion(question)
            }
        }
    }

    private fun getLetter(index: Int): String {
        if(index == 0) { return "A. " }
        if(index == 1) { return "B. " }
        if(index == 2) { return "C. " }
        if(index == 3) { return "D. " }
        return "A. "
    }
}
