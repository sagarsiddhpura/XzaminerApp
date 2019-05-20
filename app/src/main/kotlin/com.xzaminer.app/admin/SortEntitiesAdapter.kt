package com.xzaminer.app.admin

import android.graphics.Color
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.xzaminer.app.R
import java.util.*

class SortEntitiesAdapter(eciActivity: ItemTouchHelperAdapter, var entities: ArrayList<String>, private val mDragStartListener: OnStartDragListener) :
    RecyclerView.Adapter<SortEntitiesAdapter.ItemViewHolder>(), ItemTouchHelperAdapter {

    private var parentActivity: ItemTouchHelperAdapter = eciActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sort_entities, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.handleView.setColorFilter(holder.handleView.resources.getColor(com.xzaminer.app.R.color.md_blue_800_dark))
        holder.name.text = entities[position]

        // Start a drag whenever the handle view it touched
        holder.handleView.setOnTouchListener { _, event ->
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

        val handleView: ImageView = itemView.findViewById<View>(R.id.move_icon) as ImageView
        val name: TextView = itemView.findViewById<View>(R.id.sort_name) as TextView

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(Color.WHITE)
        }
    }

    override fun loadImageImageView(
        url: String,
        imageView: ImageView,
        cropThumbnails: Boolean,
        textView: TextView?,

        hasRoundEdges: Boolean,
        im_placeholder_video: Int
    ) {}

    override fun deleteEntity(entity: String) {}
}

