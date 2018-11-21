package com.xzaminer.app.result

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.commons.views.MyTextView
import com.xzaminer.app.R
import com.xzaminer.app.quiz.Question
import kotlinx.android.synthetic.main.answers_item_grid.view.*
import java.util.*



class QuestionsAnswerAdapter(
    activity: QuizAnswersActivity, var questions: ArrayList<Question>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private var currentQuestionsHash = questions.hashCode()
    private var colorDrawable : GradientDrawable
    var adjustedPrimaryColor = activity.getAdjustedPrimaryColor()

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
        return createViewHolder(R.layout.answers_item_grid, parent)
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

    fun updateQuestions(newQuestions: ArrayList<Question>) {
        questions = newQuestions.clone() as ArrayList<Question>
        notifyDataSetChanged()
    }

    private fun setupView(view: View, question: Question) {
        view.apply {
            question_text.text = "${question.id.toString()}. ${question.text}"
            if(question.isCorrect()) {
                question_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_answer_correct))
            } else if(question.isIncorrect()) {
                question_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_answer_incorrect))
            } else if(question.isNotAttempeted()) {
                question_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_answer_not_attempted))
            }

            val rootLayout = (view as CardView).getChildAt(0) as ConstraintLayout

            question.options.forEachIndexed { index, option ->
                val root = rootLayout.getChildAt(index + 2) as LinearLayout
                val text = root.getChildAt(1) as MyTextView
                val image = root.getChildAt(0) as ImageView
                val optionText = getLetter(index) + option.text

                if(question.selectedAnswer == option.id) {
                    text.text = optionText.highlightTextPart(optionText, adjustedPrimaryColor)
                    image.setColorFilter(adjustedPrimaryColor)
                    image.beVisible()
                } else {
                    text.text = optionText
                    image.beInvisible()
                }

                if(question.correctAnswer == option.id) {
                    answer.text = "Correct Answer: " + getLetter(index) + option.text + if(option.explanation == null || option.explanation == "") "" else "\n\nExplanation: " + option.explanation
                }
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
