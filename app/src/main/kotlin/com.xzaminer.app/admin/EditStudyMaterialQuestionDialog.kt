package com.xzaminer.app.admin

import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.xzaminer.app.R
import com.xzaminer.app.studymaterial.Question
import com.xzaminer.app.studymaterial.QuestionOption
import kotlinx.android.synthetic.main.dialog_edit_question_option_item.view.*
import kotlinx.android.synthetic.main.dialog_edit_question_studymaterial.view.*

class EditStudyMaterialQuestionDialog(val activity: BaseSimpleActivity, val question: Question, val callback: (question: Question) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_edit_question_studymaterial, null)
        view.question_title.setText(question.text)
        initDialog(view.options_holder as LinearLayout)
        view.add_question.setOnClickListener {
            val viewOption = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_question_option_item, null)
            viewOption.edit_delete_image.setColorFilter(activity.getAdjustedPrimaryColor())
            viewOption.edit_delete_image.setOnClickListener {
                view.options_holder.removeView(viewOption)
            }

            view.options_holder.addView(viewOption)
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, 0,"Edit Question") {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
                        when {
                            view.question_title.value.isEmpty() -> activity.toast("Please enter Question Text")
                            else -> sendSuccess(this, view, question)
                        }
                    })
                }
            }
    }

    private fun initDialog(parentView: LinearLayout) {
        parentView.removeAllViews()
        for (option in question.options) {
            val viewOption = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_question_option_item, null)
            viewOption.option_text.setText(option.text)
            viewOption.edit_delete_image.setColorFilter(activity.getAdjustedPrimaryColor())
            viewOption.edit_delete_image.setOnClickListener {
                parentView.removeView(viewOption)
            }

            parentView.addView(viewOption)
        }
    }

    private fun sendSuccess(alertDialog: AlertDialog, view : View, question: Question) {
        question.text = view.question_title.value
        question.options.clear()
        val childCount = view.options_holder.childCount
        if(childCount > 0) {
            for (i in 0 until childCount) {
                val optionView = view.options_holder.getChildAt(i)
                question.options.add(QuestionOption(i.toLong(), optionView.option_text.text.toString(), ""))
            }
        }

//        question.options[0].text = view.question_option_1.value
//        question.options[1].text = view.question_option_2.value
//        question.options[2].text = view.question_option_3.value
//        question.options[3].text = view.question_option_4.value
//
//        question.options[0].explanation = view.question_option_1_desc.value
//        question.options[1].explanation = view.question_option_2_desc.value
//        question.options[2].explanation = view.question_option_3_desc.value
//        question.options[3].explanation = view.question_option_4_desc.value

        callback(question)
        alertDialog.dismiss()
    }
}
