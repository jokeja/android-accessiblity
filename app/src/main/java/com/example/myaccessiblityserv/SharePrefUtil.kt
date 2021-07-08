package com.example.myaccessiblityserv

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import java.text.SimpleDateFormat
import java.util.*

class SharePrefUtil {
    companion object {
        val sharePerName = "com.haha.hehe.sss"
        fun getLongValue(key: String): Long {
            val sp = App.instance().getSharedPreferences(sharePerName, Context.MODE_PRIVATE)
            val result = sp.getLong(key, 0)
            Log.e("-----------" + key + "------------", result.toString())
            if (result > 0) return result else return 5
        }

        fun putLongValue(key: String, value: Long?) {
            val sp = App.instance().getSharedPreferences(sharePerName, Context.MODE_PRIVATE)
            if (value == null || ((value!!) == 0L)) {
                sp.edit().putLong(key, 30).apply()
            } else {
                sp.edit().putLong(key, value!!).apply()
            }
            val result = sp.edit().commit()
        }

        fun getBooleanValue(key: String): Boolean {
            val sp = App.instance().getSharedPreferences(sharePerName, Context.MODE_PRIVATE)
            val result = sp.getBoolean(key, false)
            return result
        }

        fun putBooleanValue(key: String, value: Boolean?) {
            val sp = App.instance().getSharedPreferences(sharePerName, Context.MODE_PRIVATE)
            if (value == null) {
                sp.edit().putBoolean(key, false).apply()
            } else {
                sp.edit().putBoolean(key, value!!).apply()
            }
            val result = sp.edit().commit()
        }

        fun getStringValue(key: String): String? {
            val sp = App.instance().getSharedPreferences(sharePerName, Context.MODE_PRIVATE)
            val result = sp.getString(key,null)
            if(result==null){
                return ""
            }
            return result
        }

        fun putStringValue(key: String, value: String) {
            val sp = App.instance().getSharedPreferences(sharePerName, Context.MODE_PRIVATE)
            sp.edit().putString(key, value).apply()
            val result = sp.edit().commit()
        }

        fun autoDailyMission(): Boolean {
            return this.getBooleanValue("KS_DAILY_MISSION")
        }

        fun putAutoDailyMission(value: Boolean) {
            this.putBooleanValue("KS_DAILY_MISSION", value)
        }
    }
}