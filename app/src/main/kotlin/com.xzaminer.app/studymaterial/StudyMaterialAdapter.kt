package com.xzaminer.app.studymaterial

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.mikkipastel.videoplanet.player.PlaybackStatus
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.extensions.getXzaminerDataDir
import com.xzaminer.app.utils.AUDIO_PLAYBACK_STATE
import com.xzaminer.app.utils.QUESTION_ID
import com.xzaminer.app.utils.checkFileExists
import kotlinx.android.synthetic.main.study_material_item_grid.view.*
import java.io.File
import java.util.*



class StudyMaterialAdapter(
    activity: StudyMaterialActivity, var questions: ArrayList<Question>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {
    private var xzaminerDataDir: File
    private var studyMaterialActivity: StudyMaterialActivity? = null

    init {
        setupDragListener(true)
        this.studyMaterialActivity = activity
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
            option_text.text = question.options.joinToString (separator = "\n\n")  { it -> "\u25CF  ${it.text}" }

            if(question.audios.isEmpty()) {
                divider_options_audio.beGone()
                audio_parent.beGone()
            } else {
                val audio = question.audios.first()
                audio.details[QUESTION_ID] = arrayListOf(question.id.toString())
                divider_options_audio.beVisible()
                audio_parent.beVisible()
                audio_name.text = audio.name
                audio_icon.setColorFilter(resources.getColor(R.color.md_blue_800_dark))
                audio_download.setColorFilter(resources.getColor(R.color.md_blue_800_dark))

                if(checkFileExists(xzaminerDataDir, "audios/" + audio.fileName)) {
                    audio_download.beGone()
                } else {
                    audio_download.beVisible()
                }

                if(audio.details[AUDIO_PLAYBACK_STATE] != null && !audio.details[AUDIO_PLAYBACK_STATE]!!.isEmpty()
                    && audio.details[AUDIO_PLAYBACK_STATE]!!.first() == PlaybackStatus.PLAYING) {
                    audio_icon.setImageResource(R.drawable.ic_pause)
                } else {
                    audio_icon.setImageResource(R.drawable.ic_play)
                }

                audio_icon.setOnClickListener {
                    studyMaterialActivity?.handleAudioPlayback(audio)
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
