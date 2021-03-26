package com.example.myaccessiblityserv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class GestureDescHelper {
    class GestureConfig {
        var mDelay = 0L
        var mDuration: Long = 800
        var mDistance = 150f
        var beginP = PointF(0f, 0f)
        var endP = PointF(0f, 0f)

        constructor() {
            val widthHeight = ScreenUtils.GetWidthAndHeight(App.instance())
            this.mDistance = widthHeight.second - 150f
            this.beginP = PointF(widthHeight.first / 2f, widthHeight.second - 80f)
            this.endP = PointF(this.beginP.x, this.beginP.y - this.mDistance)
        }

        constructor(distance: Float, duration: Long) : this() {
            this.mDuration = duration
            this.mDistance = distance
            this.endP = PointF(this.beginP.x, this.beginP.y - this.mDistance)
        }

        constructor(distance: Float, duration: Long, delay: Long) : this(distance, duration) {
            this.mDelay = delay
        }

    }

    companion object {
        private var handler = Handler()
        fun tapAllArea(service: AccessibilityService,rect:Rect){
            val blockNum = 10
            val blocks = ArrayList<PointF>()
            val widthHeight = ScreenUtils.GetWidthAndHeight(App.instance())
            val width = rect.width()
            val heigth = rect.height()
            val wStep = width/blockNum
            val hStep = heigth/blockNum
            for (wi in 0..blockNum-1){
                val x = rect.left+wStep/2f + wStep*wi
                for (hi in 0..blockNum-1){
                    val y = rect.top + hStep/2f + hStep*hi
                    blocks!!.add(PointF(x,y))
                }
            }
            for (wi in 0..blockNum-1){
                for (hi in 0..blockNum-1){
                    tapPoint(service,blocks!![wi*blockNum+hi])
                    Log.e("================",blocks!![wi*blockNum+hi].toString())
                    Thread.sleep(5)
                }
            }
        }

        fun tapPoint(service: AccessibilityService, point: PointF) {
            var path = Path()
            path.moveTo(point.x, point.y)
            var builder = GestureDescription.Builder()
            var stroke = GestureDescription.StrokeDescription(path, 0, 1, false)
            var callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    Log.e("------tapPoint---completed-----", gestureDescription.toString())
                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    Log.e("------tapPoint---cancel-----", gestureDescription.toString())
                }
            }
            var dispatchGestureresult = service.dispatchGesture(
                builder.addStroke(stroke).build(),
                callback,
                null
            )
        }

        fun tapNode(service: AccessibilityService, parentNode: AccessibilityNodeInfo?) {
            if (parentNode == null) {
                Log.e("------tapNode-----", "error parentNode is null")
                return
            }
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
                    Log.e("------tapNode---completed-----", gestureDescription.toString())
                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    Log.e("------tapNode---cancel-----", gestureDescription.toString())
                }
            }
            var dispatchGestureresult = service.dispatchGesture(
                builder.addStroke(stroke).build(),
                callback,
                null
            )
        }

        fun scrollNode(
            service: AccessibilityService,
            scrollCompleted: ((GestureDescription) -> Unit)?,
            scrollCancelled: ((GestureDescription) -> Unit)?,
            config: GestureConfig = GestureConfig()
        ) {
            val widthHeight = ScreenUtils.GetWidthAndHeight(App.instance())
            Log.e("-------scrollNode------", widthHeight.toString())
            var path = Path()
            path.moveTo(config.beginP.x, config.beginP.y);//设置Path的起点
            path.lineTo(config.endP.x, config.endP.y);
            var builder = GestureDescription.Builder()
            var stroke = GestureDescription.StrokeDescription(
                path,
                0,
                config.mDuration,
                false
            )
            var callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    if (scrollCompleted != null) {
                        scrollCompleted(gestureDescription)
                    }
                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    if (scrollCancelled != null) {
                        scrollCancelled(gestureDescription)
                    }
                }
            }
            Log.e("--------------scrollNode-------------", "config.mDelay:" + config.mDelay)
            handler.postDelayed(Runnable {
                service.dispatchGesture(
                    builder.addStroke(stroke).build(),
                    callback,
                    null
                )
            }, config.mDelay)

        }
    }
}