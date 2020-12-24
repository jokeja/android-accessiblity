package com.example.myaccessiblityserv

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager

class SharePrefUtil {
    companion object{
        val sharePerName = "com.haha.hehe.sss"
        fun getLongValue(context: Context,key:String): Long {
            val sp = context.getSharedPreferences(sharePerName,Context.MODE_PRIVATE)
            val result = sp.getLong(key,0)
            Log.e("-----------"+key+"------------",result.toString())
            if(result > 0) return result else return 5
        }
        fun putLongValue(context: Context,key:String,value:Long?){
            Log.e("-----------"+key+"------------",value.toString())
            val sp = context.getSharedPreferences(sharePerName,Context.MODE_PRIVATE)
            Log.e("-----------sp------------",sp.toString())
            if(value==null||((value!!) == 0L)){
                sp.edit().putLong(key,30).apply()
            }else{
                sp.edit().putLong(key,value!!).apply()
            }
            val result= sp.edit().commit()
            Log.e("-----------------------",result.toString())
            SharePrefUtil.getLongValue(context,key)
        }
    }
}