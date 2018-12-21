package com.xzaminer.app.billing

import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.isActivityDestroyed
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import kotlinx.android.synthetic.main.category_item_grid.view.*
import kotlinx.android.synthetic.main.show_purchase_item.view.*


class ShowPurchasesAdapter(
    activity: ShowPurchasesActivity, var purchases: ArrayList<Purchase>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private var currentPurchasesHash = purchases.hashCode()
    private var showPurchaseActivity : ShowPurchasesActivity? = null

    init {
        this.showPurchaseActivity = activity
    }

    override fun getActionMenuId() = R.menu.cab_empty

    override fun prepareItemSelection(viewHolder: ViewHolder) {
    }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutType =  R.layout.show_purchase_item
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

    fun updatePurchases(newPurs: ArrayList<Purchase>) {
        val purs = newPurs.clone() as ArrayList<Purchase>
        if (purs.hashCode() != currentPurchasesHash) {
            currentPurchasesHash = purs.hashCode()
            purchases = purs
            notifyDataSetChanged()
            finishActMode()
        }
    }

    private fun setupView(view: View, purchase: Purchase) {
        view.apply {
            purchase_title.text = purchase.name
            purchase_desc.text = purchase.desc

            if(purchase.originalPrice != "") {
                val content = SpannableString("INR "+ purchase.originalPrice + " " + purchase.actualPrice)
                content.setSpan(StrikethroughSpan(), 4, purchase.originalPrice.length + 4, 0)
                purchase_price.text = content
            } else {
                purchase_price.text = "INR "+ purchase.actualPrice
            }

            purchase_buy.setOnClickListener {
                showPurchaseActivity!!.initPurchase(purchase)
            }
        }
    }
}
