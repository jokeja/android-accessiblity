package com.example.myaccessiblityserv

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager

class ScreenUtils {
    companion object{
        fun GetWindowsDisplay(context:Context):Display{
            var windowManager:WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return windowManager.defaultDisplay
        }
        fun GetWidthAndHeight(context: Context):Pair<Int,Int>{
            var display = GetWindowsDisplay(context)
            var displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            return Pair(displayMetrics.widthPixels,displayMetrics.heightPixels)
        }
    }

}