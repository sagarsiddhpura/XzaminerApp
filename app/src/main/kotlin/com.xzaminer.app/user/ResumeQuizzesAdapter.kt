package com.xzaminer.app.user

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.isActivityDestroyed
import com.simplemobiletools.commons.views.FastScroller
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.R.string.purchases
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.QB_LAST_ACCESSED
import com.xzaminer.app.utils.QB_STARTED_ON
import kotlinx.android.synthetic.main.category_item_grid.view.*
import kotlinx.android.synthetic.main.course_domain_video_item.view.*


class ResumeQuizzesAdapter(
    activity: ListUnfinishedQuizzesActivity, var quizzes: ArrayList<StudyMaterial>, recyclerView: MyRecyclerView, fastScroller: FastScroller? = null,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, fastScroller, itemClick) {

    private var currentPurchasesHash = purchases.hashCode()
    private var showPurchaseActivity : ListUnfinishedQuizzesActivity? = null

    init {
        this.showPurchaseActivity = activity
    }

    override fun getActionMenuId() = R.menu.cab_empty

    override fun prepareItemSelection(viewHolder: ViewHolder) {
    }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutType =  R.layout.course_domain_video_item
        return createViewHolder(layoutType, parent)
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val quiz = quizzes.getOrNull(position) ?: return
        val view = holder.bindView(quiz, true, false) { itemView, adapterPosition ->
            setupView(itemView, quiz)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = quizzes.size

    override fun prepareActionMode(menu: Menu) {
        return
    }

    override fun actionItemPressed(id: Int) {
        return
    }

    override fun getSelectableItemCount() = quizzes.size

    override fun getIsItemSelectable(position: Int) = true

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isActivityDestroyed()) {
            if(holder.itemView != null && holder.itemView.cat_thumbnail != null) {
                Glide.with(activity).clear(holder.itemView.cat_thumbnail)
            }
        }
    }

    fun updateQuizzes(newQuizzes: ArrayList<StudyMaterial>) {
        val newQuizzesClone = newQuizzes.clone() as ArrayList<StudyMaterial>
        if (newQuizzesClone.hashCode() != currentPurchasesHash) {
            currentPurchasesHash = newQuizzesClone.hashCode()
            quizzes = newQuizzesClone
            notifyDataSetChanged()
            finishActMode()
        }
    }

    private fun setupView(view: View, quiz: StudyMaterial) {
        view.apply {
            vid_name.text = quiz.name
            vid_desc.text = "Started On:" + quiz.properties[QB_STARTED_ON]?.first() + "\nLast accessed: " + quiz.properties[QB_LAST_ACCESSED]?.first()
            vid_image.beGone()
            vid_download_status.beGone()
            vid_time.beGone()
//            vid_time.text = video.duration
        }
    }
}
