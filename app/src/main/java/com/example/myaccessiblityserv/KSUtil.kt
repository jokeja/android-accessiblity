package com.example.myaccessiblityserv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import java.util.*

class KSUtil {
    private val MissionType_SCROLL = 1
    private val MissionType_FULI = 2
    private val MissionType_LIVE = 4
    private val MissionType_AD = 4 // 广告
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
        private var instance: KSUtil? = null
        private var autoScrolling = false
        private var boxCanClickTime: Long = System.currentTimeMillis()
        fun getInstance(accessibilityService: AccessibilityService): KSUtil {
            if (instance == null) {
                instance = KSUtil()
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
                this.execMissionType = MissionType_LIVE
                if (liveCountDownNode == null) {
                    delayMis = 3
                    accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                }
            } else {
                this.execMissionType = MissionType_SCROLL
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
        AccesNodeUtil.logAllNodes(rootWindow,  null)
    }

    // 做每日任务
    // return true 标识可以做任务
    private fun execDailyMission(): Boolean {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            // 检测是否是任务主页面
            val zq_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "日常任务")
            if (zq_node == null) {
                return false
            }
            this.anaView(rootWindow)
            val sign_dao_node = AccesNodeUtil.findButtonNodeByText(rootWindow, 8, "去签到")
            if(sign_dao_node!=null){
                sign_dao_node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
            // 看广告
            val fuli_node = AccesNodeUtil.findButtonNodeByText(rootWindow, 8, "福利")
            if (fuli_node != null) {
                fuli_node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                this.execMissionType = MissionType_AD
                return true
            } else {
                // 看直播
                val l_node = AccesNodeUtil.findAllNodesByEqualsText(rootWindow, 1, "看直播")
                val l_succes_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "今日已成功领取直播奖励金币")
                if (l_succes_node == null && l_node != null && l_node.size > 0) {
                    l_node[1].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
                // 宝箱node
                val tb_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "treasurebox")
                val tb_opend_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "开宝箱奖励")
                // 宝箱是否已点击
                if (tb_opend_node != null) {
                    val tb_opend_more_vedio_node =
                        AccesNodeUtil.findNodeByText(rootWindow, 1, "看精彩视频赚更多")
                    if (tb_opend_more_vedio_node != null) {
                        GestureDescHelper.tapNode(
                            this.accessibilityService!!,
                            tb_opend_more_vedio_node
                        )
                        this.execMissionType = MissionType_AD
                        return true
                    }
                }
                // 未点击宝箱分析宝箱是否可以点击
                if (tb_node != null) {
                    val tb_count_down_node = AccesNodeUtil.findNodeByText(tb_node.parent, 1, "分")
                    if (tb_count_down_node == null) {
                        GestureDescHelper.tapNode(this.accessibilityService!!, tb_node)
                        val tb_t_node = AccesNodeUtil.findNodeByText(tb_node.parent, 1, "明日再来")
                        // 宝箱任务未完成
                        if (tb_t_node == null) {
                            return true
                        }
                        boxCanClickTime = DateUtil.getTodayEnd().time
                    } else {
                        // 可以点击则分析宝箱的倒计时
                        val minute = tb_count_down_node.text.substring(0, 2).toInt()
                        val second = tb_count_down_node.text.substring(3, 5).toInt()
                        boxCanClickTime = DateUtil.nowAdd(minute, second).time
                    }
                } else {
                    boxCanClickTime = DateUtil.getTodayEnd().time
                }
                // 广告福利结束node
                val t_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "明日再来")
                if (t_node != null) {
                    SharePrefUtil.fuLiDailyMissionFinish()
                    accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    return false
                }
            }
        }
        return false
    }

    //检查广告页面是否可以回退
    private fun checkGuangGaoWindow(): Boolean {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        val node = AccesNodeUtil.findNodeByText(rootWindow, 1, "继续观看")
        if (node != null) {
            this.execMissionType = MissionType_AD
            GestureDescHelper.tapNode(accessibilityService!!, node)
            return true
        } else {
            val adDescNode = AccesNodeUtil.findNodeById(
                rootWindow!!,
                1,
                "com.kuaishou.nebula:id/video_ad_description"
            )
            if (adDescNode == null) {
                return false
            }
            val closeNode = AccesNodeUtil.findNodeById(
                rootWindow!!,
                1,
                "com.kuaishou.nebula:id/video_close_icon"
            )
            if (closeNode != null) {
                GestureDescHelper.tapNode(accessibilityService!!, closeNode)
                this.execMissionType = MissionType_AD
                return true
            }
            return false
        }
    }

    // 检查执行任务
    private fun execMissions() {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (this.execMissionType != MissionType_None) {
            autoScrolling = true
            if (this.checkGuangGaoWindow()) {
                handler.postDelayed(missionRunnable, 1000)
            } else if (execDailyMission()) {
                handler.postDelayed(missionRunnable, 800)
            } else if (canScroll()) {
                var delay = scrollMissionTime()
                handler.postDelayed(Runnable {
                    // 应该检测当天是否可以点击福利、广告等按钮
                    if (SharePrefUtil.autoDailyMission() && (!SharePrefUtil.fuLiDailyMissionIsFinished() || System.currentTimeMillis() >= boxCanClickTime)) {
                        // 获取视频页面的每日任务按钮
                        val dailyNode =
                            AccesNodeUtil.findNodeById(
                                rootWindow!!,
                                1,
                                "com.kuaishou.nebula:id/num"
                            )
                        if (dailyNode != null && this.execMissionType != MissionType_LIVE) {
                            GestureDescHelper.tapNode(accessibilityService!!, dailyNode)
                            handler.postDelayed(missionRunnable, 800)
                            return@Runnable
                        }
                    }
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