package com.xzaminer.app.billing

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.isActivityDestroyed
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import kotlinx.android.synthetic.main.category_item_grid.view.*
import kotlinx.android.synthetic.main.course_domain_video_item.view.*


class ListUserPurchasesAdapter(
    activity: ListUserPurchasesActivity, var purchases: ArrayList<Purchase>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private var currentPurchasesHash = purchases.hashCode()
    private var showPurchaseActivity : ListUserPurchasesActivity? = null

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
        val purchase = purchases.getOrNull(position) ?: return
        val view = holder.bindView(purchase, true, false) { itemView, adapterPosition ->
            setupView(itemView, purchase)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = purchases.size

    override fun prepareActionMode(menu: Menu) {
        return
    }

    override fun actionItemPressed(id: Int) {
        return
    }

    override fun getSelectableItemCount() = purchases.size

    override fun getIsItemSelectable(position: Int) = true

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isActivityDestroyed()) {
            if(holder.itemView != null && holder.itemView.cat_thumbnail != null) {
                Glide.with(activity).clear(holder.itemView.cat_thumbnail)
            }
        }
    }

    fun updatePurchases(newPurs: java.util.ArrayList<Purchase>) {
        val quizzes = newPurs.clone() as ArrayList<Purchase>
        if (quizzes.hashCode() != currentPurchasesHash) {
            currentPurchasesHash = quizzes.hashCode()
            purchases = quizzes
            notifyDataSetChanged()
            finishActMode()
        }
    }

    private fun setupView(view: View, purchase: Purchase) {
        view.apply {
            vid_name.text = purchase.name
            vid_desc.text = "Type:" + purchase.extraPurchaseInfo
            vid_time.text = "Purchased On: " + purchase.purchased
            vid_image.beGone()
            vid_download_status.beGone()

//            vid_time.text = video.duration
        }
    }
}
