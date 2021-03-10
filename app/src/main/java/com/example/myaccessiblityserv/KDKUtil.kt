package com.example.myaccessiblityserv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class KDKUtil {
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
        private var instance: KDKUtil? = null
        private var autoScrolling = false
        private var boxCanClickTime: Long = System.currentTimeMillis()
        fun getInstance(accessibilityService: AccessibilityService): KDKUtil {
            if (instance == null) {
                instance = KDKUtil()
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
//com.yuncheapp.android.pearl:id/channel_tab_item_name
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
            delayMis = 8
        }
        return delayMis * 1000
    }
    // 获取选中的主屏幕tab
    private fun selectedTabNode():AccessibilityNodeInfo?{
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            // 查找是否有 tabs   selected表示选中的tab
            val tabs = AccesNodeUtil.findAllNodesByResId(
                rootWindow!!,
                1,
                "com.yuncheapp.android.pearl:id/tab_tv"
            )
            if(tabs != null && tabs.size > 0){
                for (index in 0..tabs.size - 1){
                    val node = tabs[index]
                    if(node.isSelected){
                        return  node
                    }
                }
            }
        }
        return null
    }
    // 是否APP是主页面多个tabs
    private fun isMainView():Boolean{
        //
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            // 查找是否有 tabs   selected表示选中的tab
            val result = AccesNodeUtil.findAllNodesByResId(
                rootWindow!!,
                1,
                "com.yuncheapp.android.pearl:id/tab_tv"
            )
            return result != null && result.size > 0
        }
        return false
    }
    // 是否是新闻页面
    private fun isNewsView(): Boolean {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            // com.yuncheapp.android.pearl:id/follow_name 文章内容创作者
            // 正文 文章内容页面关键字
            val result = rootWindow.findAccessibilityNodeInfosByText("正文")
            return result != null && result.size > 0
        }
        return false
    }

    fun anaView(rootWindow: AccessibilityNodeInfo) {
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
            this.anaView(rootWindow)
            if (adDescNode == null) {
                return false
            }
            val closeNode = AccesNodeUtil.findNodeById(
                rootWindow!!,
                1,
                "com.kuaishou.nebula:id/video_close_icon"
            )
            Log.e("----------------",closeNode.toString())
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
            if (isMainView()) { // 列表页面
                val selNode = selectedTabNode()
                Log.e("-------------",selNode.toString())
                //com.yuncheapp.android.pearl:id/initpanel_video_length
                if(selNode!=null&&selNode!!.text.contains("视频")){
                    val widthHeight = ScreenUtils.GetWidthAndHeight(App.instance())
                    // 获取视频列表的时长
//                    val videoL = AccesNodeUtil.findAllNodesByResId(rootWindow,1,"com.yuncheapp.android.pearl:id/initpanel_video_length")
                    GestureDescHelper.scrollNode(accessibilityService!!,
                        { gestureDescription: GestureDescription ->
                            handler.postDelayed(missionRunnable, 30*1000)
                        },
                        {
                            autoScrolling = false
                        }, GestureDescHelper.GestureConfig(widthHeight.second/3f, 1000)
                    )
                }
            } else if (isNewsView()) {
//                var delay = scrollMissionTime()
//                handler.postDelayed(Runnable {
//                    GestureDescHelper.scrollNode(accessibilityService!!,
//                        { gestureDescription: GestureDescription ->
//                            handler.postDelayed(missionRunnable, 800)
//                        },
//                        {
//                            autoScrolling = false
//                        })
//                }, delay)
            } else {
                handler.postDelayed(missionRunnable, 1000)
            }
        }
    }
}