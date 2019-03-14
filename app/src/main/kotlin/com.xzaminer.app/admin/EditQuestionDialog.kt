package com.xzaminer.app.admin

import android.net.Uri
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.View
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.value
import com.xzaminer.app.R
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.Question
import com.xzaminer.app.studymaterial.Video
import kotlinx.android.synthetic.main.dialog_edit_question.view.*
import java.io.File

class EditQuestionDialog(val activity: BaseSimpleActivity, val question: Question, val courseId: Long, val callback: (question: Question) -> Unit) {
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
            view.edit_file_name.hint = "No Audio File Attached"
            view.edit_file_name.setText("")
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
