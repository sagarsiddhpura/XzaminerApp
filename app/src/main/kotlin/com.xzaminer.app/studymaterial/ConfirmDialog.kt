package com.xzaminer.app.studymaterial

import android.app.Activity
import android.support.v7.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.xzaminer.app.R
import kotlinx.android.synthetic.main.dialog_confirm.view.*

class ConfirmDialog(val activity: Activity, val message: String, val callback: (remember: Boolean) -> Unit) {
    private var dialog: AlertDialog
    val view = activity.layoutInflater.inflate(R.layout.dialog_confirm, null)!!

    init {
        view.delete_remember_title.text = message
        val builder = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.yes) { dialog, which -> dialogConfirmed() }
                .setNegativeButton(R.string.no, null)

        dialog = builder.create().apply {
            activity.setupDialogStuff(view, this)
        }
    }

    private fun dialogConfirmed() {
        dialog.dismiss()
        callback(true)
    }
}
