package com.xzaminer.app.admin

import android.net.Uri
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.value
import com.xzaminer.app.R
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.Question
import com.xzaminer.app.studymaterial.QuestionOption
import com.xzaminer.app.studymaterial.Video
import kotlinx.android.synthetic.main.dialog_edit_question_option_item.view.*
import kotlinx.android.synthetic.main.dialog_edit_question_studymaterial.view.*
import java.io.File

class EditStudyMaterialQuestionDialog(val activity: BaseSimpleActivity, val question: Question, val courseId: Long, val callback: (question: Question) -> Unit) {
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

        view.file_edit_image.setColorFilter(activity.getAdjustedPrimaryColor())
        view.file_delete_image.setColorFilter(activity.getAdjustedPrimaryColor())
        if(!question.audios.isEmpty()) {
            val audio = question.audios.first()
            if(audio != null){
                view.edit_file_name.setText(audio.name)
                view.edit_file_name.hint = ""
            }
        } else {
            view.edit_file_name.hint = "No Audio File Attached"
        }
        view.file_edit_image.setOnClickListener {
            FilePickerDialog(activity, Environment.getExternalStorageDirectory().toString(), true, false, false) {

                activity.toast("Uploading Audio. Please wait till Audio is completely uploaded....")
                val audioFile = File(it)
                val file = Uri.fromFile(audioFile)
                if(!audioFile.absolutePath.contains(".")) {
                    activity.toast("File does not have proper naming convention of filename.ext. Please select file with proper naming convention.")
                    return@FilePickerDialog
                }
                val name = courseId.toString() + "_" + question.id.toString() + "_audio." + audioFile.absolutePath.split(".").last()
                val riversRef = activity.dataSource.getStorage().getReference("courses/" + courseId + "/").child(name)
                val uploadTask = riversRef.putFile(file)

                uploadTask.addOnFailureListener {
                    activity.toast("Failed to Upload Audio")
                    view.edit_file_name.setText("")
                }.addOnSuccessListener {

                    activity.toast("Audio Uploaded successfully.")
                    question.audios = arrayListOf(Video(question.id, audioFile.name, "", null, name, "courses/" + courseId + "/"))
                    view.edit_file_name.setText(audioFile.name)

                }.addOnProgressListener {
                    val progress = (100 * it.bytesTransferred) / it.totalByteCount
                    view.edit_file_name.setText("Uploading " + progress + "% ...")
                }.addOnPausedListener {
                    activity.toast("Upload is paused....")
                }
            }
        }

        view.file_delete_image.setOnClickListener {
            question.audios = arrayListOf()
            view.edit_file_name.setText("")
            view.edit_file_name.hint = "No Audio File Attached"
            activity.toast("Audio File removed...")
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

        if(!question.audios.isEmpty()) {
            val audio = question.audios.first()
            if(audio != null){
                audio.name = view.edit_file_name.text.toString()
            }
        }

        callback(question)
        alertDialog.dismiss()
    }
}
