package com.xzaminer.app.quiz

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.commons.views.MyTextView
import com.xzaminer.app.R
import kotlinx.android.synthetic.main.question_item_grid.view.*
import java.util.*



class QuestionsAdapter(activity: QuizActivity, var questions: ArrayList<Question>, recyclerView: MyRecyclerView,
                       itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private var currentQuestionsHash = questions.hashCode()
    private var colorDrawable : GradientDrawable
    private var quizActivity: QuizActivity? = null
    var adjustedPrimaryColor = activity.getAdjustedPrimaryColor()

    init {
        setupDragListener(true)
        colorDrawable = GradientDrawable()
        colorDrawable.setColor(primaryColor.adjustAlpha(0.6f))
        colorDrawable.cornerRadius = Resources.getSystem().displayMetrics.density*4
        this.quizActivity = activity

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

    fun updateQuestions(newQuestions: ArrayList<Question>) {
        questions = newQuestions.clone() as ArrayList<Question>
        notifyDataSetChanged()
    }

    private fun setupView(view: View, question: Question) {
        view.apply {
            question_text.text = question.text
            question_icon.setColorFilter(adjustedPrimaryColor)
            val rootLayout = (view as CardView).getChildAt(0) as ConstraintLayout

            rootLayout.getChildAt(7).beGone()
            rootLayout.getChildAt(8).beGone()
            rootLayout.getChildAt(9).beGone()
            rootLayout.getChildAt(10).beGone()

            question.options.forEachIndexed { index, option ->
                val text = rootLayout.getChildAt(index + 3) as MyTextView

                if(question.selectedAnswer == option.id) {
                    text.text = option.text!!.highlightTextPart(option.text!!, adjustedPrimaryColor)
                    val image = rootLayout.getChildAt(index + 7) as ImageView
                    image.setColorFilter(adjustedPrimaryColor)
                    image.beVisible()
                } else {
                    text.text = option.text
                }
                text.setOnClickListener { quizActivity?.optionClicked(question, option.id) }


            }
        }
    }
}
