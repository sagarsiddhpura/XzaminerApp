package com.xzaminer.app.course

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.extensions.getXzaminerDataDir
import com.xzaminer.app.extensions.loadIconImageView
import com.xzaminer.app.extensions.loadImageImageView
import com.xzaminer.app.studymaterial.Video
import com.xzaminer.app.utils.VIDEO_DOWNLOAD_PROGRESS
import com.xzaminer.app.utils.checkFileExists
import kotlinx.android.synthetic.main.course_domain_video_item.view.*




class CourseSectionDomainVideosAdapter(
    activity: CourseSectionVideosDomainActivity, var videos: ArrayList<Video>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private var courseSectionVideosDomainActivity: CourseSectionVideosDomainActivity

    init {
        courseSectionVideosDomainActivity = activity
    }

    override fun getActionMenuId() = R.menu.cab_empty

    override fun prepareItemSelection(viewHolder: ViewHolder) { }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) { }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(R.layout.course_domain_video_item, parent)
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val video = videos.getOrNull(position) ?: return
        val view = holder.bindView(video, true, false) { itemView, adapterPosition ->
            setupView(itemView, video)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = videos.size

    override fun prepareActionMode(menu: Menu) {
        return
    }

    override fun actionItemPressed(id: Int) {
        return
    }

    override fun getSelectableItemCount() = videos.size

    override fun getIsItemSelectable(position: Int) = true


    private fun setupView(view: View, video: Video) {
//        val fetchDataDirFile = fetchDataDirFile(activity.getXzaminerDataDir(), "videos/" + video.fileName)
//        if(fetchDataDirFile.exists()) fetchDataDirFile.delete()

        view.apply {
            vid_name.text = video.name
            vid_desc.text = video.desc
            vid_time.text = video.duration
            if(video.image != null && video.image != "") {
                activity.loadImageImageView(video.image!!, vid_image, false, null, false, R.drawable.im_placeholder_video)
            } else {
                val img : Int = R.drawable.im_placeholder_video
                activity.loadIconImageView(img, vid_image, false)
            }

            val xzaminerDataDir = activity.getXzaminerDataDir()
            if(checkFileExists(xzaminerDataDir, "videos/" + video.fileName)) {
                vid_download_status.beGone()
                vid_download_status_text.beGone()
            } else {
                if(video.details[VIDEO_DOWNLOAD_PROGRESS] == null) {
                    vid_download_status.beVisible()
                    vid_download_status_text.beGone()

                    val res = resources.getDrawable(R.drawable.ic_download)
                    vid_download_status.setImageDrawable(res)

                    vid_download_status.setOnClickListener {
                        courseSectionVideosDomainActivity.addDownload(video)
                    }
                } else {
                    vid_download_status.beVisible()
                    vid_download_status_text.beVisible()

                    vid_download_status_text.text = video.details[VIDEO_DOWNLOAD_PROGRESS]?.first()
                    val res = resources.getDrawable(R.drawable.ic_cancel)
                    vid_download_status.setImageDrawable(res)

                    vid_download_status.setOnClickListener {
                        courseSectionVideosDomainActivity.cancelDownload(video)
                    }
                }
            }

        }
    }

    fun updateVideos(values: ArrayList<Video>) {
        videos = values
        notifyDataSetChanged()
    }
}
