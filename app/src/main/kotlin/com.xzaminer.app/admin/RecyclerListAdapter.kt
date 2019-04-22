package com.xzaminer.app.admin

import android.graphics.Color
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.xzaminer.app.R
import com.xzaminer.app.extensions.loadImageImageView
import java.util.*

class RecyclerListAdapter(eciActivity: EditCourseImagesActivity, var entities: ArrayList<String>, private val mDragStartListener: OnStartDragListener) :
    RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder>(), ItemTouchHelperAdapter {

    private var parentActivity: EditCourseImagesActivity = eciActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.course_image_grid, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.handleView.setColorFilter(holder.handleView.resources.getColor(com.xzaminer.app.R.color.md_blue_800_dark))
        holder.deleteView.setColorFilter(holder.handleView.resources.getColor(com.xzaminer.app.R.color.md_blue_800_dark))
        parentActivity.loadImageImageView(entities[position], holder.imageView, false, null, false, com.xzaminer.app.R.drawable.im_placeholder_video)

        holder.deleteView.setOnClickListener {
            parentActivity?.deleteEntity(entities[position])
        }

        // Start a drag whenever the handle view it touched
        holder.handleView.setOnTouchListener { v, event ->
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                mDragStartListener.onStartDrag(holder)
            }
            false
        }
    }

    override fun onItemDismiss(position: Int) {
        entities.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(entities, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        parentActivity?.onItemMove(fromPosition, toPosition)
        return true
    }

    override fun getItemCount(): Int {
        return entities.size
    }

    fun updateEntities(images: ArrayList<String>) {
        entities = images.clone() as ArrayList<String>
        notifyDataSetChanged()
    }

    /**
     * Simple example of a view holder that implements [ItemTouchHelperViewHolder] and has a
     * "handle" view that initiates a drag event when touched.
     */
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder {

        val imageView: ImageView = itemView.findViewById<View>(R.id.manage_image) as ImageView
        val handleView: ImageView = itemView.findViewById<View>(R.id.move_icon) as ImageView
        val deleteView: ImageView = itemView.findViewById<View>(R.id.manage_delete) as ImageView

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(Color.WHITE)
        }
    }
}

//Data2added.zip
//        Dawa2added.zew
//        Dawa2added.zew
//        gmDawa
//        gmDawa2added.zew
//        gDaw a2ad ded. zewm
//        gDawa2.zewm
//        gDawa2.ultm