package com.xzaminer.app.admin

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.toast
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.loadIconImageView
import com.xzaminer.app.extensions.loadImageImageView
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.Video
import com.xzaminer.app.utils.COURSE_ID
import com.xzaminer.app.utils.DOMAIN_ID
import com.xzaminer.app.utils.SECTION_ID
import com.xzaminer.app.utils.VIDEO_ID
import kotlinx.android.synthetic.main.activity_edit_course.*
import java.io.File

class EditVideoActivity : SimpleActivity() {

    private var courseId: Long = -1
    private var sectionId: Long = -1
    private var domainId: Long = -1
    private var videoId: Long = -1
    private lateinit var video: Video
    private lateinit var studyMaterial: StudyMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)
        supportActionBar?.title = "Edit Video"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.apply {
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            domainId = getLongExtra(DOMAIN_ID, -1)
            videoId = getLongExtra(VIDEO_ID, -1)
            if (domainId == (-1).toLong() || courseId == (-1).toLong() || sectionId == (-1).toLong() || videoId == (-1).toLong()) {
                toast("Error opening Video")
                finish()
                return
            }
        }

        dataSource.getCourseStudyMaterialById(courseId, sectionId, domainId) { studyMaterial_ ->
            if(studyMaterial_ != null && !studyMaterial_.videos.isEmpty() && studyMaterial_.fetchVideo(videoId) != null) {
                val loadedVideo = studyMaterial_.fetchVideo(videoId) as Video
                studyMaterial = studyMaterial_
                loadVideo(loadedVideo)
            } else {
                toast("Error opening Video")
                finish()
                return@getCourseStudyMaterialById
            }
        }

        edit_edit_image.setOnClickListener {
            FilePickerDialog(this, Environment.getExternalStorageDirectory().toString(), true, false, false) {
                toast("Uploading image. Please wait till image is uploaded.")
                val imageFile = File(it)
                val file = Uri.fromFile(imageFile)
                val name = courseId.toString() + "_" + imageFile.name
                val riversRef = dataSource.getStorage().getReference("courses/" + courseId + "/").child(name)
                val uploadTask = riversRef.putFile(file)

                uploadTask.addOnFailureListener {
                    toast("Failed to Upload Image")
                }.addOnSuccessListener {
                    toast("Image Uploaded successfully. Save to update course")
                    video.image = "courses/" + courseId + "/" + name
                    loadImageImageView(video.image!!, edit_course_image, false, null, false, R.drawable.im_placeholder_video)
                }
            }
        }

        edit_delete_image.setOnClickListener {
            video.image = null
            edit_course_image.setImageDrawable(null)
        }

        edit_short_name_root.beVisible()
        monetization_root.beGone()
        monetization_label.beGone()
        monetization_spinner.beGone()
        edit_edit_image.setColorFilter(getAdjustedPrimaryColor())
        edit_delete_image.setColorFilter(getAdjustedPrimaryColor())
        file_edit_image.setColorFilter(getAdjustedPrimaryColor())
        file_delete_image.setColorFilter(getAdjustedPrimaryColor())

        file_edit_image.setOnClickListener {
            FilePickerDialog(this, Environment.getExternalStorageDirectory().toString(), true, false, false) {
                toast("Uploading video. Please wait till video is completely uploaded....")
                val videoFile = File(it)
                val file = Uri.fromFile(videoFile)
                val name = courseId.toString() + "_" + videoFile.name
                val riversRef = dataSource.getStorage().getReference("courses/" + courseId + "/").child(name)
                val uploadTask = riversRef.putFile(file)

                uploadTask.addOnFailureListener {
                    toast("Failed to Upload Video")
                }.addOnSuccessListener {
                    toast("Video Uploaded successfully. Save to update course")
                    video.fileName = name
                    edit_file_name.setText(video.fileName)
                }.addOnProgressListener {
                    val progress = (100 * it.bytesTransferred) / it.totalByteCount
                    toast("Uploading " + progress + "% ...")
                }.addOnPausedListener {
                    toast("Upload is paused....")
                }
            }
        }

        file_delete_image.setOnClickListener {
            video.fileName = ""
            edit_file_name.setText("")
        }
    }

    private fun loadVideo(loadedvideo: Video) {
        video = loadedvideo
        edit_name.setText(loadedvideo.name)
        edit_desc.setText(loadedvideo.desc)
        edit_short_name_root.hint = "Duration"
        edit_short_name.setText(loadedvideo.duration)
        edit_file_name.setText(video.fileName)

        if(loadedvideo.image != null && loadedvideo.image != "") {
            loadImageImageView(loadedvideo.image!!, edit_course_image, false, null, false, R.drawable.im_placeholder_video)
        } else {
            val img : Int = R.drawable.im_placeholder
            loadIconImageView(img, edit_course_image, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manage, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.manage_finish -> validateAndSaveEntity()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun validateAndSaveEntity() {
        if(edit_name.text == null || edit_name.text.toString() == "") {
            ConfirmationDialog(this, "Please enter Name", R.string.yes, R.string.ok, 0) { }
            return
        }
        if(edit_file_name.text == null || edit_file_name.text.toString() == "") {
            ConfirmationDialog(this, "Upload video file for this Video", R.string.yes, R.string.ok, 0) { }
            return
        }

        ConfirmDialog(this, "Are you sure you want to update the Video?") {
            video.name = edit_name.text.toString()
            video.desc = edit_desc.text.toString()
            video.duration = edit_short_name.text.toString()

            dataSource.updateVideoProperties(courseId, sectionId, studyMaterial)
            ConfirmationDialog(this, "Video has been updated successfully", R.string.yes, R.string.ok, 0) { }
        }
    }
}
