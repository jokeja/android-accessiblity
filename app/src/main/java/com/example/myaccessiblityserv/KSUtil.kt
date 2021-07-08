package com.example.myaccessiblityserv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import kotlin.math.log

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
            this.execMissionType = MissionType_SCROLL
        }
        return delayMis * 1000
    }

    // 直播关注退出
    private fun liveFollow(): Boolean {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val followNode = AccesNodeUtil.findNodeByText(rootWindow, 1, "关注并退出")
            val exitNode = AccesNodeUtil.findNodeByEqualsText(rootWindow, 1, "退出")
            if (followNode != null && exitNode != null) {
                exitNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
        }
        return false
    }

    // 是否是视频或者直播页面
    private fun canScroll(): Boolean {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val result = rootWindow.findAccessibilityNodeInfosByText("分享")
            val follow = rootWindow.findAccessibilityNodeInfosByText("关注")
            return (result != null && result.size > 0) || (follow != null && follow.size > 0)
        }
        return true
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
            val sign_dao_node = AccesNodeUtil.findButtonNodeByEqualsText(rootWindow, 8, "去签到")
            if (sign_dao_node != null) {
                sign_dao_node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
            // 看广告
            val fuli_node = AccesNodeUtil.findNodeByText(rootWindow, 8, "悬赏任务")
            val f_succes_node = AccesNodeUtil.findNodeByText(fuli_node!!.parent, 8, "明天")
            if (fuli_node != null && f_succes_node == null) {
                fuli_node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                this.execMissionType = MissionType_AD
                return true
            } else {
                // 看直播
                val l_node = AccesNodeUtil.findAllNodesByText(rootWindow, 1, "看直播领")
                val l_succes_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "今日已成功领取直播奖励金币")
                if (l_succes_node == null && l_node != null && l_node.size > 0) {
                    l_node[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
                AccesNodeUtil.anaView(rootWindow,"execDailyMission")
                val tb_opend_node = AccesNodeUtil.findNodeByText(rootWindow, 1, SharePrefUtil.getStringValue("bxKeyword"))
                // 宝箱是否已点击
                if (tb_opend_node != null) {
                    // 点击宝箱
                    GestureDescHelper.tapNodeCenter(
                        this.accessibilityService!!,
                        tb_opend_node
                    )
                    val tb_opend_more_vedio_node =
                        AccesNodeUtil.findNodeByText(rootWindow, 1, "看精彩视频赚更多")
                    if (tb_opend_more_vedio_node != null) {
                        GestureDescHelper.tapNodeCenter(
                            this.accessibilityService!!,
                            tb_opend_more_vedio_node
                        )
                        this.execMissionType = MissionType_AD
                        return true
                    }
                }
                // 宝箱node
                val tb_node = AccesNodeUtil.findNodeByText(rootWindow, 1, "treasurebox")
                if(tb_node == null){
                    boxCanClickTime = DateUtil.getTodayEnd().time
                    accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    return true
                }
                val tb_t_node = AccesNodeUtil.findNodeByText(tb_node.parent, 1, "明日再来")
                // 宝箱任务完成
                if (tb_t_node != null) {
                    boxCanClickTime = DateUtil.getTodayEnd().time
                    accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    return true
                }
                val tb_count_down_node = AccesNodeUtil.findNodeByText(tb_node.parent, 1, "分")
                if (tb_count_down_node != null) {
                    // 可以点击则分析宝箱的倒计时
                    val minute = tb_count_down_node.text.substring(0, 2).toInt()
                    val second = tb_count_down_node.text.substring(3, 5).toInt()
                    boxCanClickTime = DateUtil.nowAdd(minute, second).time
                    accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    return true
                }
                Log.e("============boxCanClickTime==============",boxCanClickTime.toString())
            }
        }
        return false
    }

    //检查广告页面是否可以回退
    private fun checkGuangGaoWindow(): Boolean {
        val rootWindow = accessibilityService!!.rootInActiveWindow ?: return  false
        val node = AccesNodeUtil.findNodeByText(rootWindow, 1, "继续观看")
        if (node != null) {
            this.execMissionType = MissionType_AD
            GestureDescHelper.tapNodeCenter(accessibilityService!!, node)
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
                GestureDescHelper.tapNodeCenter(accessibilityService!!, closeNode)
                this.execMissionType = MissionType_AD
                return true
            }
            return false
        }
    }

    private fun isLiveView(): Boolean {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val livePendantNode = AccesNodeUtil.findNodeById(
                rootWindow,
                1,
                "com.kuaishou.nebula:id/live_play_root_container"
            )
            return livePendantNode != null
        }
        return false
    }

    private fun isVedioView(): Boolean {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val rbNode = AccesNodeUtil.findNodeById(rootWindow,1,"com.kuaishou.nebula:id/slide_right_btn")
            val lbNode = AccesNodeUtil.findNodeById(rootWindow,1,"com.kuaishou.nebula:id/left_btn")
            val glNode = AccesNodeUtil.findNodeById(rootWindow,1,"com.kuaishou.nebula:id/group_right_action_bar_root_layout")
            return rbNode!=null&&lbNode!=null&&glNode!=null
        }
        return false
    }

    // 检查执行任务
    private fun execMissions() {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (this.execMissionType != MissionType_None) {
            autoScrolling = true
            if (this.isLiveView()) {
                Thread.sleep(1000)
                val liveCountDownNode = AccesNodeUtil.findNodeById(
                    rootWindow,
                    1,
                    "com.kuaishou.nebula:id/award_count_down_text"
                )
                Log.e("==========================",liveCountDownNode.toString())
                if (liveCountDownNode == null) {
                    Log.e("---KSUtil------------", "-----------liveCountDownNode--null---------")
                    accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                }
                handler.postDelayed(missionRunnable, 1000)
            } else if (liveFollow()) {
                handler.postDelayed(missionRunnable, 1000)
            } else if (this.checkGuangGaoWindow()) {
                handler.postDelayed(missionRunnable, 1000)
            } else if (execDailyMission()) {
                handler.postDelayed(missionRunnable, 1000)
            } else if (isVedioView()) {
                var delay = scrollMissionTime()
                Log.e("boxCanClickTime","${boxCanClickTime}")
                if (SharePrefUtil.autoDailyMission() && System.currentTimeMillis() >= boxCanClickTime) {
                    // 获取视频页面的每日任务按钮
                    val dailyNode =
                        AccesNodeUtil.findNodeById(
                            rootWindow!!,
                            1,
                            "com.kuaishou.nebula:id/num"
                        )
                    if (dailyNode != null && this.execMissionType != MissionType_LIVE) {
                        GestureDescHelper.tapNodeCenter(accessibilityService!!, dailyNode)
                        handler.postDelayed(missionRunnable, 800)
                        return
                    }
                }
                GestureDescHelper.scrollNode(accessibilityService!!,
                    { gestureDescription: GestureDescription ->
                    },
                    {
                        autoScrolling = false
                    })
                handler.postDelayed(missionRunnable, delay)
            } else {
                handler.postDelayed(missionRunnable, 1000)
            }
        }
    }
}