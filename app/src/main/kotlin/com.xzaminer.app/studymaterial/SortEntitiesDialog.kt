package com.xzaminer.app.studymaterial

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.widget.ImageView
import android.widget.TextView
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.admin.ItemTouchHelperAdapter
import com.xzaminer.app.admin.OnStartDragListener
import com.xzaminer.app.admin.SimpleItemTouchHelperCallback
import com.xzaminer.app.admin.SortEntitiesAdapter
import com.xzaminer.app.extensions.loadImageImageView
import kotlinx.android.synthetic.main.dialog_sort_entities.view.*
import java.util.*

class SortEntitiesDialog(val activity: Activity, val entities: ArrayList<String>, val callback: (entities: ArrayList<String>) -> Unit) : ItemTouchHelperAdapter, OnStartDragListener {

    private var dialog: AlertDialog
    val view = activity.layoutInflater.inflate(R.layout.dialog_sort_entities, null)!!
    private var mItemTouchHelper: ItemTouchHelper? = null

    init {
        val layoutManager = view.sort_entities_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1

        val currAdapter = view.sort_entities_grid.adapter
        if (currAdapter == null) {
            SortEntitiesAdapter(
                this,
                entities.clone() as java.util.ArrayList<String>,
                this
            )
            .apply {
                view.sort_entities_grid.adapter = this
                val callback = SimpleItemTouchHelperCallback(this)
                mItemTouchHelper = ItemTouchHelper(callback)
                mItemTouchHelper!!.attachToRecyclerView(view.sort_entities_grid)

            }
        } else {
            (currAdapter as SortEntitiesAdapter).updateEntities(entities)
        }

        val builder = AlertDialog.Builder(activity)
            .setTitle("Drag and drop to reorder")
            .setPositiveButton(R.string.save) { dialog, which -> dialogConfirmed() }

        dialog = builder.create().apply {
            activity.setupDialogStuff(view, this)
        }
    }

    private fun dialogConfirmed() {
        dialog.dismiss()
        callback(entities)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(entities, fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {}

    override fun loadImageImageView(
        url: String,
        imageView: ImageView,
        cropThumbnails: Boolean,
        textView: TextView?,
        hasRoundEdges: Boolean,
        im_placeholder_video: Int
    ) {
        activity.loadImageImageView(url, imageView, cropThumbnails,  textView, hasRoundEdges, im_placeholder_video)
    }

    override fun deleteEntity(entity: String) {}

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper!!.startDrag(viewHolder)
    }
}
