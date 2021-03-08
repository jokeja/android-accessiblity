package com.example.myaccessiblityserv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.os.Handler
import android.view.accessibility.AccessibilityNodeInfo

class DYUtil {
    private val MissionType_SCROLL = 1
    private val MissionType_FULI = 2
    private val MissionType_LIVE = 4
    private val MissionType_None = 8


    private var context: Context = App.instance()
    private var accessibilityService: AccessibilityService? = null
    private var handler = Handler()
    private var execMissionType = MissionType_SCROLL


    private val missionRunnable = object : Runnable {

        override fun run() {
            execMissions()
        }
    }

    companion object {
        private var instance: DYUtil? = null
        private var autoScrolling = false
        private var boxCanClickTime: Long = System.currentTimeMillis()
        fun getInstance(accessibilityService: AccessibilityService): DYUtil {
            if (instance == null) {
                instance = DYUtil()
                instance!!.accessibilityService = accessibilityService
            }
            return instance!!
        }

        fun restartMission() {
            autoScrolling = false
            boxCanClickTime = System.currentTimeMillis()
        }
    }

    fun init() {
        this.execMissionType = MissionType_SCROLL
    }

    fun pause() {
        autoScrolling = false
        this.execMissionType = MissionType_None
    }

    fun execScrollMission() {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null && !autoScrolling) {
            handler.removeCallbacks(missionRunnable)
            handler.postDelayed(missionRunnable, 800)
        }
    }

    // 获取滑屏间隔
    private fun scrollMissionTime(): Long {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        var delayMis = 1L
        if (rootWindow != null) {
            delayMis = SharePrefUtil.getLongValue("videoS")
            // 查找直播关键字
            var nodeList = rootWindow.findAccessibilityNodeInfosByText("说点什么")
            if (nodeList != null && nodeList.size > 0) {//是否是直播页面 30秒滑屏
                delayMis = SharePrefUtil.getLongValue("liveS")
                val liveCountDownNode = AccesNodeUtil.findNodeById(
                    rootWindow,
                    1,
                    "com.kuaishou.nebula:id/award_count_down_text"
                )
                if (liveCountDownNode == null) {
                    delayMis = 3
                    accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                }
            }
        }
        return delayMis * 1000
    }

    // 是否是视频或者直播页面
    private fun canScroll(): Boolean {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val result = rootWindow.findAccessibilityNodeInfosByText("分享")
            return result != null && result.size > 0
        }
        return true
    }

    private fun anaView(rootWindow: AccessibilityNodeInfo) {
        AccesNodeUtil.logAllNodes(rootWindow, "@", null)
    }

    // 做每日任务
    // return true 标识可以做任务
    private fun execDailyMission(): Boolean {
        return false
    }
    // 检查执行任务
    private fun execMissions() {
        if (this.execMissionType != MissionType_None) {
            autoScrolling = true
            val rootWindow = accessibilityService!!.rootInActiveWindow
            if (execDailyMission()) {
                handler.postDelayed(missionRunnable, 800)
            } else if (canScroll()) {
                var delay = scrollMissionTime()
                handler.postDelayed(Runnable {
                    GestureDescHelper.scrollNode(accessibilityService!!,
                        { gestureDescription: GestureDescription ->
                            handler.postDelayed(missionRunnable, 800)
                        },
                        {
                            autoScrolling = false
                        })
                }, delay)
            } else {
                handler.postDelayed(missionRunnable, 1000)
            }
        }
    }
}