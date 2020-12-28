package com.example.myaccessiblityserv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class KSUtil {
    private val MissionType_SCROLL = 1
    private val MissionType_FULI = 2
    private val MissionType_LIVE = 3


    private var context: Context = App.instance()
    private var accessibilityService: AccessibilityService? = null
    private var handler = Handler()
    private var autoScrolling = false
    private var execMissionType = MissionType_SCROLL
    private val scrollRunnable = object : Runnable {

        override fun run() {
            startScroll()
        }
    }

    companion object {
        private var instance: KSUtil? = null
        fun getInstance(accessibilityService: AccessibilityService): KSUtil {
            if (instance == null) {
                instance = KSUtil()
                instance!!.accessibilityService = accessibilityService
            }
            return instance!!
        }
    }

    fun init() {

    }

    fun stop() {
        this.autoScrolling = false
    }

    fun execScrollMission() {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null && !autoScrolling) {
            Log.e("------step--------", "11111111111111111111111111111111111111111")
            handler.removeCallbacks(scrollRunnable)
            handler.postDelayed(scrollRunnable, 800)
        }
    }

    private fun scrollMissionTime(): Long {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        var delayMis = 1L
        if (rootWindow != null) {
            delayMis = SharePrefUtil.getLongValue("videoS")
            // 查找直播关键字
            var nodeList = rootWindow.findAccessibilityNodeInfosByText("说点什么")
            if (nodeList != null && nodeList.size > 0) {//是否是直播页面 30秒滑屏
                delayMis = SharePrefUtil.getLongValue("liveS")
            }
        }
        return delayMis * 1000
    }

    private fun canScroll(): Boolean {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val result = rootWindow.findAccessibilityNodeInfosByText("分享")
            return result != null && result.size > 0
        }
        return true
    }

    fun analyLiveWindows() {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        AccesNodeUtil.logAllNodes(rootWindow, "#", null)
    }

    private fun canClickDaily(): Boolean {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val zq_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "去赚钱")
            if (zq_node == null) {
                return false
            }
            val fuli_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "福利")
            if (fuli_node != null) {
                fuli_node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                handler.postDelayed(Runnable {
                    checkGuangGaoWindow()
                }, 32 * 1000)
                return true
            } else {
                val t_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "明日再来")
                val l_node = AccesNodeUtil.findAllNodesByText(rootWindow, 1, "看直播")
                val l_succes_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "今日已成功领取直播奖励金币")
                if (l_succes_node == null && l_node != null && l_node.size > 0) {
                    l_node[1].performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                    handler.postDelayed(Runnable {
//                        analyLiveWindows()
//                    },5)
                    return true
                }
                if (t_node != null) {
                    SharePrefUtil.fuLiDailyMissionFinish()
                    accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    return false
                }
            }
            return fuli_node != null
        }
        return false
    }

    private fun checkGuangGaoWindow() {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        val node = AccesNodeUtil.findNodeByText(rootWindow, 1, "继续观看")
        if (node != null) {
            AccesNodeUtil.tapNode(accessibilityService!!, node)
        } else {
            accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
        handler.postDelayed(Runnable {
            checkGuangGaoWindow()
        }, 32 * 1000)
    }

    private fun startScroll() {
        Log.e("------step--------", "2222222222222222222222222222222222222")
        val widthHeight = ScreenUtils.GetWidthAndHeight(this.context!!)
        this.autoScrolling = true
        var path = Path()
        path.moveTo((widthHeight.first / 2f), widthHeight.second - 150f);//设置Path的起点
        path.lineTo((widthHeight.first / 2f), 150f);
        var builder = GestureDescription.Builder()
        var stroke = GestureDescription.StrokeDescription(path, 0, 800, false)
        var callback = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                Log.e("------step--------", "66666666666666 66666666666666666666666666")
                handler.postDelayed(scrollRunnable, 800)
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                Log.e("------step--------", "7777777777777777777777777777777777777777")
            }
        }
        val rootWindow = accessibilityService!!.rootInActiveWindow
        var delay = scrollMissionTime()
        if (canClickDaily()) {
            handler.postDelayed(scrollRunnable, 800)
        } else if (canScroll()) {
            handler.postDelayed(Runnable {
                if (!SharePrefUtil.fuLiDailyMissionIsFinished()) {
                    // 应该检测当天是否可以点击福利、广告等按钮
                    val redNode =
                        AccesNodeUtil.findNodeById(
                            rootWindow!!,
                            1,
                            "com.kuaishou.nebula:id/num"
                        )
                    if (redNode != null) {
                        AccesNodeUtil.tapNode(accessibilityService!!, redNode)
                        handler.postDelayed(scrollRunnable, 800)
                        return@Runnable
                    }
                }
                Log.e("------step--------", "44444444444444444444444444444444444444444444")
                var dispatchGestureresult = this.accessibilityService!!.dispatchGesture(
                    builder.addStroke(stroke).build(),
                    callback,
                    null
                )
                Log.e("-----dispatchGesture----result---------", "" + dispatchGestureresult)
                Log.e("------step--------", "555555555555555555555555555555555555555555555")
            }, delay)
        } else {
            handler.postDelayed(scrollRunnable, 1000)
        }
        Log.e("------step--------", "333333333333333333333333333333333333")
    }
}