package com.xzaminer.app.utils

import android.content.Context
import android.content.res.Configuration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.helpers.BaseConfig
import com.xzaminer.app.R
import com.xzaminer.app.data.User

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    fun getLoggedInUser(): User? {
        val listType = object : TypeToken<User>() {}.type
        return Gson().fromJson<User>(loggedInUserString, listType)
    }

    fun setLoggedInUser(user : User?) {
        if(user == null) {
            loggedInUserString = null
        } else {
            val listType = object : TypeToken<User>() {}.type
            val userJson = Gson().toJson(user, listType)
            loggedInUserString = userJson
        }
    }

    private var loggedInUserString: String?
        get() = prefs.getString("logged_in_user", null)
        set(loggedInUserString) = prefs.edit().putString("logged_in_user", loggedInUserString).apply()

    var dirColumnCnt: Int
        get() = prefs.getInt(getDirectoryColumnsField(), getDefaultDirectoryColumnCount())
        set(dirColumnCnt) = prefs.edit().putInt(getDirectoryColumnsField(), dirColumnCnt).apply()

    private fun getDirectoryColumnsField(): String {
        val isPortrait = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        return if (isPortrait) {
            DIR_COLUMN_CNT
        } else {
            DIR_LANDSCAPE_COLUMN_CNT
        }
    }

    private fun getDefaultDirectoryColumnCount() = context.resources.getInteger(R.integer.category_columns_vertical_scroll)
}