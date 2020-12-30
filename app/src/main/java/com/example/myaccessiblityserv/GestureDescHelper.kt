package com.example.myaccessiblityserv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class GestureDescHelper {
    companion object {
        fun tapNode(service: AccessibilityService, parentNode: AccessibilityNodeInfo) {

            var path = Path()
            val rect = Rect()
            parentNode.getBoundsInScreen(rect)
            path.moveTo(
                Math.abs(rect!!.centerX().toFloat()),
                rect!!.centerY().toFloat()
            );//设置Path的起点
            var builder = GestureDescription.Builder()
            var stroke = GestureDescription.StrokeDescription(path, 0, 300, false)
            var callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    Log.e("------step--------", gestureDescription.toString())
                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    Log.e("------step--------", gestureDescription.toString())
                }
            }
            var dispatchGestureresult = service.dispatchGesture(
                builder.addStroke(stroke).build(),
                callback,
                null
            )
        }
        fun scrollNode(service: AccessibilityService, scrollCompleted: ((GestureDescription)->Unit), scrollCancelled: ((GestureDescription)->Unit)):Boolean{
            val widthHeight = ScreenUtils.GetWidthAndHeight(App.instance())
            var path = Path()
            path.moveTo((widthHeight.first / 2f), widthHeight.second - 150f);//设置Path的起点
            path.lineTo((widthHeight.first / 2f), 150f);
            var builder = GestureDescription.Builder()
            var stroke = GestureDescription.StrokeDescription(path, 0, 800, false)
            var callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    scrollCompleted(gestureDescription)
                }
                override fun onCancelled(gestureDescription: GestureDescription) {
                    scrollCancelled(gestureDescription)
                }
            }
            return service.dispatchGesture(
                builder.addStroke(stroke).build(),
                callback,
                null
            )
        }
    }
}