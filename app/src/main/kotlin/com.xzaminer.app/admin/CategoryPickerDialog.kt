package com.xzaminer.app.admin

import android.os.Parcelable
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.FilepickerItemsAdapter
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.onGlobalLayout
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.models.FileDirItem
import com.xzaminer.app.R
import com.xzaminer.app.category.Category
import com.xzaminer.app.course.CourseSection
import com.xzaminer.app.extensions.dataSource
import kotlinx.android.synthetic.main.dialog_category_picker.view.*
import java.util.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * The only filepicker constructor with a couple optional parameters
 *
 * @param activity has to be activity to avoid some Theme.AppCompat issues
 * @param callback the callback used for returning the selected file/folder
 */
class CategoryPickerDialog(val activity: BaseSimpleActivity,
                           val callback: (section: CourseSection) -> Unit) {

    private var mFirstUpdate = true
    private var mPrevPath = ""
    private var mScrollStates = HashMap<String, Parcelable>()
    private var currPath: String = ""
    private lateinit var mDialog: AlertDialog
    private var mDialogView = activity.layoutInflater.inflate(R.layout.dialog_category_picker, null)
    private var currCatId: Long = -1
    var catHistory = arrayListOf<Long>(-1)
    var catPathHistory = arrayListOf<String>("")
    val objMap : HashMap<String, Any> = hashMapOf()

    init {
        tryUpdateItems()

        val builder = AlertDialog.Builder(activity)
            .setNegativeButton(R.string.cancel, null)
            .setOnKeyListener { dialogInterface, i, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                    if(catHistory.size > 1) {
                        catHistory.removeAt(catHistory.lastIndex)
                        catPathHistory.removeAt(catPathHistory.lastIndex)
                        currCatId = catHistory.last()
                        tryUpdateItems()
                    } else {
                        mDialog.dismiss()
                    }
                }
                true
            }

        builder.setPositiveButton(R.string.ok, null)

//        mDialogView.filepicker_fab.apply {
//            beVisible()
//            setOnClickListener { createNewCategory() }
//        }

        mDialog = builder.create().apply {
            activity.setupDialogStuff(mDialogView, this, getTitle())
        }

        mDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            verifyPath()
        }
    }

    private fun getTitle() = R.string.pick_category

    private fun createNewCategory() {
        CreateNewCategoryDialog(activity, currPath) {
            tryUpdateItems()
        }
    }

    private fun tryUpdateItems() {
        getItems(currPath) {
            updateItems(it)
        }
    }

    private fun updateItems(items: List<FileDirItem>) {
        val sortedItems = items.sortedWith(compareBy({ !it.isDirectory }, { it.name.toLowerCase() }))

        val adapter = FilepickerItemsAdapter(activity, sortedItems, mDialogView.filepicker_list) {
            currPath = (it as FileDirItem).path
            currCatId = it.size
            if(currCatId != -1L) { catHistory.add(currCatId) }
            catPathHistory.add(getChildPath(currCatId))
            tryUpdateItems()
        }
        adapter.addVerticalDividers(true)

        val layoutManager = mDialogView.filepicker_list.layoutManager as LinearLayoutManager
        mScrollStates[mPrevPath.trimEnd('/')] = layoutManager.onSaveInstanceState()

        mDialogView.apply {
            filepicker_list.adapter = adapter
            filepicker_fastscroller.allowBubbleDisplay = context.baseConfig.showInfoBubble
            filepicker_fastscroller.setViews(filepicker_list) {
                filepicker_fastscroller.updateBubbleText(sortedItems.getOrNull(it)?.getBubbleText() ?: "")
            }

            layoutManager.onRestoreInstanceState(mScrollStates[currPath.trimEnd('/')])
            filepicker_list.onGlobalLayout {
                filepicker_fastscroller.setScrollToY(filepicker_list.computeVerticalScrollOffset())
            }
        }

        mFirstUpdate = false
        mPrevPath = currPath
    }

    private fun verifyPath() {
        if(objMap[currCatId.toString()] != null && objMap[currCatId.toString()] is CourseSection) {
            sendSuccess()
        } else  {
            activity.toast("You can add study material only to section of a course")
        }
    }

    private fun sendSuccess() {
        val courseSection = objMap[currCatId.toString()] as CourseSection
        courseSection.desc = catPathHistory.joinToString ( separator = "" ) + "/studyMaterials"
        callback(courseSection)
        mDialog.dismiss()
    }

    private fun getItems(path: String, callback: (List<FileDirItem>) -> Unit) {
        val obj = objMap[currCatId.toString()]
        if(obj == null) {
            activity.dataSource.getChildCategories(if(currCatId == -1L) null else currCatId) { catsAll: ArrayList<Category>, name: String ->
                catsAll.forEach {
                    objMap[it.id.toString()] = it
                }
                callback(catsAll.map { it -> FileDirItem("/" + it.id, it.name!!, true, it.subCategories?.size ?: 0, it.id) })
            }
        } else {
            if(obj is Category) {
                if(!obj.isCourse) {
                    activity.dataSource.getChildCategories(if(currCatId == -1L) null else currCatId) { catsAll: ArrayList<Category>, name: String ->
                        catsAll.forEach {
                            objMap[it.id.toString()] = it
                        }
                        callback(catsAll.map { it -> FileDirItem("/subCategories/" + it.id, it.name!!, true, it.subCategories?.size ?: 0, it.id) })
                    }
                } else {
                    activity.dataSource.getCourseById(currCatId) {
                        it?.sections?.values?.forEach {
                            objMap[it.id.toString()] = it
                        }
                        callback(it?.sections!!.values.map { it -> FileDirItem(currPath + "/sections/" + it.id, it.name, true, 0, it.id) })
                    }
                }
            } else {
                callback(arrayListOf())
            }
        }
    }

    private fun getChildPath(currentId: Long): String {
        if(catPathHistory.size == 1) {
            return "/" + currentId
        }
        val obj = objMap[currCatId.toString()]
        if(obj is Category) {
            if(!obj.isCourse) {
                return "/subCategories/" + currentId
            } else {
                return "/courses/" + currentId
            }
        } else {
            return "/sections/" + currentId
        }
    }
}
