package com.xzaminer.app.admin

import android.support.v7.app.AlertDialog
import android.view.View
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.xzaminer.app.R
import com.xzaminer.app.studymaterial.Question
import kotlinx.android.synthetic.main.dialog_edit_question.view.*
import java.io.File

class EditQuestionDialog(val activity: BaseSimpleActivity, val question: Question, val callback: (question: Question) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_edit_question, null)
        view.question_title.setText(question.text)
        view.question_option_1.setText(question.options[0].text)
        view.question_option_2.setText(question.options[1].text)
        view.question_option_3.setText(question.options[2].text)
        view.question_option_4.setText(question.options[3].text)
        view.question_option_1_desc.setText(question.options[0].explanation)
        view.question_option_2_desc.setText(question.options[1].explanation)
        view.question_option_3_desc.setText(question.options[2].explanation)
        view.question_option_4_desc.setText(question.options[3].explanation)

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, 0,"Edit Question") {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
                        when {
                            view.question_title.value.isEmpty() -> activity.toast("Please enter Question Text")
                            view.question_option_1.value.isEmpty() -> activity.toast("Please enter Option 1 Text")
                            view.question_option_2.value.isEmpty() -> activity.toast("Please enter Option 2 Text")
                            view.question_option_3.value.isEmpty() -> activity.toast("Please enter Option 3 Text")
                            view.question_option_4.value.isEmpty() -> activity.toast("Please enter Option 4 Text")

                            else -> sendSuccess(this, view, question)
                        }
                    })
                }
            }
    }

    private fun sendSuccess(alertDialog: AlertDialog, view : View, question: Question) {
        question.text = view.question_title.value
        question.options[0].text = view.question_option_1.value
        question.options[1].text = view.question_option_2.value
        question.options[2].text = view.question_option_3.value
        question.options[3].text = view.question_option_4.value

        question.options[0].explanation = view.question_option_1_desc.value
        question.options[1].explanation = view.question_option_2_desc.value
        question.options[2].explanation = view.question_option_3_desc.value
        question.options[3].explanation = view.question_option_4_desc.value

        callback(question)
        alertDialog.dismiss()
    }
}
