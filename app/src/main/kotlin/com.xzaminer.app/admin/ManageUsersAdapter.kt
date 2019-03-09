package com.xzaminer.app.admin

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.highlightTextPart
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.extensions.getXzaminerDataDir
import com.xzaminer.app.user.User
import com.xzaminer.app.utils.USERTYPE_ADMIN
import kotlinx.android.synthetic.main.course_domain_video_item.view.*
import java.io.File


class ManageUsersAdapter(
    activity: ManageUsersActivity, var users: ArrayList<User>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private var parentActivity: ManageUsersActivity? = null
    private var xzaminerDataDir: File

    init {
        setupDragListener(true)
        this.parentActivity = activity
        xzaminerDataDir = activity.getXzaminerDataDir()
    }

    override fun getActionMenuId() = R.menu.cab_empty

    override fun prepareItemSelection(viewHolder: ViewHolder) {
    }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(R.layout.course_domain_video_item, parent)
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val user = users.getOrNull(position) ?: return
        val view = holder.bindView(user, true, false) { itemView, adapterPosition ->
            setupView(itemView, user, position)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = users.size

    override fun prepareActionMode(menu: Menu) {
        return
    }

    override fun actionItemPressed(id: Int) {
        return
    }

    override fun getSelectableItemCount() = users.size

    override fun getIsItemSelectable(position: Int) = true

    fun updateUsers(newQuestions: java.util.ArrayList<User>) {
        users = newQuestions.clone() as ArrayList<User>
        notifyDataSetChanged()
    }

    private fun setupView(view: View, user: User, adapterPosition: Int) {
        view.apply {
            if(user.userType == USERTYPE_ADMIN) {
                vid_name.text = ("(Admin) " + user.name).highlightTextPart(("(Admin) " + user.name), resources.getColor(R.color.md_red))
            } else {
                vid_name.text = user.name
            }
            vid_desc.text = user.email
            vid_time.beGone()
            vid_image.beGone()
            vid_download_status.beGone()
            vid_download_status_text.beGone()

            manage_root.beVisible()
            manage_edit.setOnClickListener {
                parentActivity?.editUser(user)
            }

            manage_delete.setOnClickListener {
                parentActivity?.deleteUser(user)
            }
        }
    }
}
