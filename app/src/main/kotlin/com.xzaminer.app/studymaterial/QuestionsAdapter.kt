package com.xzaminer.app.studymaterial

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.mikkipastel.videoplanet.player.PlaybackStatus
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.commons.views.MyTextView
import com.xzaminer.app.R
import com.xzaminer.app.extensions.getXzaminerDataDir
import com.xzaminer.app.utils.AUDIO_PLAYBACK_STATE
import com.xzaminer.app.utils.QUESTION_ID
import com.xzaminer.app.utils.VIDEO_DOWNLOAD_PROGRESS
import com.xzaminer.app.utils.checkFileExists
import kotlinx.android.synthetic.main.question_item_grid.view.*
import java.io.File
import java.util.*



class QuestionsAdapter(activity: QuizActivity, var questions: ArrayList<Question>, recyclerView: MyRecyclerView,
                       itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private var quizActivity: QuizActivity? = null
    var adjustedPrimaryColor = activity.getAdjustedPrimaryColor()
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
        return createViewHolder(R.layout.question_item_grid, parent)
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

    private fun setupView(
        view: View,
        question: Question,
        position: Int
    ) {
        view.apply {
            question_text.text = "${position+1}. ${question.text}"
            question_text.setTextColor(resources.getColor(R.color.md_blue_800_dark))

            question_icon.setColorFilter(resources.getColor(R.color.md_blue_800_dark))
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
                root.setOnClickListener { quizActivity?.optionClicked(question, option.id) }
            }

            if(question.isMarkedForLater) {
                view.setCardBackgroundColor(resources.getColor(R.color.md_blue_100).adjustAlpha(1F))
                question_icon.setImageResource(R.drawable.ic_marked_later)
            } else {
                view.setCardBackgroundColor(Color.WHITE)
                question_icon.setImageResource(R.drawable.ic_mark_later)
            }

            question_icon.setOnClickListener {
                quizActivity?.markForLater(question)
            }

            // Audio Icon
            if(question.audios.isEmpty()) {
                divider_options_audio.beGone()
                audio_parent.beGone()
            } else {
                val audio = question.audios.first()
//                if(BuildConfig.DEBUG) {
//                    File(xzaminerDataDir, "audios/" + audio.fileName).delete()
//                    File(xzaminerDataDir, "audios/" + audio.fileName + "_temp").delete()
//                }

                audio.details[QUESTION_ID] = arrayListOf(question.id.toString())
                divider_options_audio.beVisible()
                audio_parent.beVisible()
                audio_name.text = audio.name
                audio_icon.setColorFilter(resources.getColor(R.color.md_blue_800_dark))

                if(audio.details[AUDIO_PLAYBACK_STATE] != null && !audio.details[AUDIO_PLAYBACK_STATE]!!.isEmpty()
                    && audio.details[AUDIO_PLAYBACK_STATE]!!.first() == PlaybackStatus.PLAYING) {
                    audio_icon.setImageResource(R.drawable.ic_pause)
                } else {
                    audio_icon.setImageResource(R.drawable.ic_play)
                }

                audio_icon.setOnClickListener {
                    quizActivity?.handleAudioPlayback(audio)
                }

                if(checkFileExists(xzaminerDataDir, "audios/" + audio.fileName)) {
                    download_status_text.beGone()
                    download_status.beGone()
                } else {
                    if(audio.details[VIDEO_DOWNLOAD_PROGRESS] == null) {
                        download_status.beVisible()
                        download_status_text.beGone()

                        val res = resources.getDrawable(R.drawable.ic_download)
                        download_status.setImageDrawable(res)

                        download_status.setOnClickListener {
                            quizActivity?.addDownload(audio)
                        }
                    } else {
                        download_status.beVisible()
                        download_status_text.beVisible()

                        download_status_text.text = audio.details[VIDEO_DOWNLOAD_PROGRESS]?.first()
                        val res = resources.getDrawable(R.drawable.ic_cancel)
                        download_status.setImageDrawable(res)

                        download_status.setOnClickListener {
                            quizActivity?.cancelDownload(audio)
                        }
                    }
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
