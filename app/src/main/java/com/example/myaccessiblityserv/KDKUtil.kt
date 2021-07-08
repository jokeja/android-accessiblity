package com.example.myaccessiblityserv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
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

    //模拟滑动的时间 否则领红包时间会变慢
    private var moniScrollTime = System.currentTimeMillis() + 30 * 1000


    private val missionRunnable = object : Runnable {

        override fun run() {
            execMissions()
        }
    }

    companion object {
        private var instance: KDKUtil? = null
        private var autoScrolling = false

        // 宝箱点击时间
        private var boxClickMissionTime: Long = System.currentTimeMillis()
        //幸运转盘点击时间
        private var luckyZPMissionTime = System.currentTimeMillis()
        //金币悬赏点击时间
        private var jbxsMissionTime = System.currentTimeMillis()
        //红包雨点击时间
        private var hbyMissionTime = System.currentTimeMillis()
        fun getInstance(accessibilityService: AccessibilityService): KDKUtil {
            if (instance == null) {
                instance = KDKUtil()
                instance!!.accessibilityService = accessibilityService
            }
            DateUtil.getTodayEnd()
            return instance!!
        }

        fun restartMission() {
            autoScrolling = false
            boxClickMissionTime = System.currentTimeMillis()
            luckyZPMissionTime = System.currentTimeMillis()
            jbxsMissionTime = System.currentTimeMillis()
            hbyMissionTime = System.currentTimeMillis()
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
    private fun selectedTabNode(text: String = "视频"): AccessibilityNodeInfo? {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            // 查找是否有 tabs   selected表示选中的tab
            val tabs = AccesNodeUtil.findAllNodesByResId(
                rootWindow!!,
                1,
                "com.yuncheapp.android.pearl:id/tab_tv"
            )
            if (tabs != null && tabs.size > 0) {
                for (index in 0..tabs.size - 1) {
                    val node = tabs[index]
                    if (node.text == text) {
                        if (!node.isSelected) {
                            GestureDescHelper.tapNodeCenter(this.accessibilityService!!, node)
                            Thread.sleep(1000)
                        }
                        return node
                    }
                }
            }
        }
        return null
    }

    // 是否APP是主页面多个tabs
    private fun isMainView(): Boolean {
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

    private fun clickRedButton(): Boolean {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val result = AccesNodeUtil.findNodeById(
                rootWindow,
                1,
                "com.yuncheapp.android.pearl:id/background"
            )
            val curMillis = System.currentTimeMillis()

            if (curMillis >= KDKUtil.boxClickMissionTime || curMillis >= luckyZPMissionTime || curMillis >= jbxsMissionTime || curMillis >= hbyMissionTime) {
                GestureDescHelper.tapNodeCenter(this.accessibilityService!!, result)
            } else {
                return false
            }
            return result != null
        }
        return false
    }

    // 福利中心
    private fun isFuliCenter(): Boolean {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val result = AccesNodeUtil.findNodeByEqualsText(rootWindow, 1, "福利中心")
            //
            return result != null
        }
        return false
    }

    private fun clickAfterOpenBaoXiang(): Boolean {
        // viewIdRes:com.yuncheapp.android.pearl:id/close  关闭按钮
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val opened = AccesNodeUtil.findNodeByText(rootWindow, 1, "恭喜你，获得")
            val result = AccesNodeUtil.findNodeByText(rootWindow, 1, "观看视频")
            if (result == null) {
                val closeBtn = AccesNodeUtil.findNodeById(
                    rootWindow,
                    1,
                    "com.yuncheapp.android.pearl:id/close"
                )
                if(closeBtn!=null){
                    GestureDescHelper.tapNodeCenter(this.accessibilityService!!, closeBtn)
                }
            } else {
                GestureDescHelper.tapNodeCenter(this.accessibilityService!!, result)
            }
            return opened != null && result != null
        }
        return false
    }

    //检查广告页面是否可以回退
    private fun checkGuangGaoWindow(): Boolean {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (rootWindow == null) {
            return false
        }
        val abandonNode = AccesNodeUtil.findNodeById(rootWindow,1,"com.yuncheapp.android.pearl:id/award_video_close_dialog_abandon_button")
        if(abandonNode!=null){
            this.execMissionType = MissionType_AD
            GestureDescHelper.tapNodeCenter(accessibilityService!!, abandonNode)
            return true
        }
        val node = AccesNodeUtil.findNodeByText(rootWindow, 1, "继续观看")
        if (node != null) {
            this.execMissionType = MissionType_AD
            GestureDescHelper.tapNodeCenter(accessibilityService!!, node)
            return true
        } else {
            val adDescNode = AccesNodeUtil.findNodeById(
                rootWindow!!,
                1,
                "com.yuncheapp.android.pearl:id/video_ad_description"
            )
            if (adDescNode != null) {
                val closeNode = AccesNodeUtil.findNodeById(
                    rootWindow!!,
                    1,
                    "com.yuncheapp.android.pearl:id/video_close_icon"
                )
                if (closeNode != null) {
                    GestureDescHelper.tapNodeCenter(accessibilityService!!, closeNode)
                    return true
                }
                return true
            }
            return false
        }
    }

    //宝箱
    private fun clickBX(rootWindow: AccessibilityNodeInfo): Boolean {
        val bxNode = AccesNodeUtil.findNodeById(
            rootWindow,
            1,
            "com.yuncheapp.android.pearl:id/time_reward_root"
        )
        if (bxNode != null) {
            var countDownNode = AccesNodeUtil.findNodeById(
                rootWindow,
                1,
                "com.yuncheapp.android.pearl:id/pendant_text_double_coin"
            )
            if (countDownNode == null) {
                GestureDescHelper.tapNodeCenter(this.accessibilityService!!, bxNode)
                return true
            } else {
                if (countDownNode.text.contains("已达上限")) {
                    KDKUtil.boxClickMissionTime = DateUtil.getTodayEnd().time
                } else {
                    val minute = countDownNode.text.substring(0, 2).toInt()
                    val second = countDownNode.text.substring(3, 5).toInt()
                    KDKUtil.boxClickMissionTime = DateUtil.nowAdd(minute, second).time
                }
            }
        }
        return false
    }

    //广告 金币悬赏
    private fun clickAdMission(): Boolean {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val adMiNode = AccesNodeUtil.findNodeByText(rootWindow, 1, "金币悬赏任务")
            if (adMiNode != null) {
                val zhuanqianNode = AccesNodeUtil.findNodeByText(adMiNode.parent, 1, "去赚钱")
                if (zhuanqianNode != null) {
                    GestureDescHelper.tapNodeCenter(accessibilityService!!, zhuanqianNode.parent)
                    return true
                }else{
                    val jbNextTimeNode = AccesNodeUtil.findNodeByText(adMiNode.parent,1,":")
                    if(jbNextTimeNode!=null){
                        jbxsMissionTime =  System.currentTimeMillis() + DateUtil.getSecondByString(jbNextTimeNode.text) * 1000
                    }
                }
            } else if(System.currentTimeMillis() >= jbxsMissionTime){
                // 滚动一下
                val widthHeight = ScreenUtils.GetWidthAndHeight(App.instance())
                GestureDescHelper.scrollNode(
                    accessibilityService!!,
                    { gestureDescription: GestureDescription ->
                    },
                    {
                    },
                    GestureDescHelper.GestureConfig(
                        widthHeight.second - 80f,
                        300,
                        0
                    )
                )
                return true
            }
        }
        return false
    }

    //转盘
    private fun zhuanPanMission(): Boolean {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (rootWindow != null && System.currentTimeMillis() >= luckyZPMissionTime) {
            val zpNode = AccesNodeUtil.findNodeByResIdThenText(rootWindow,"com.yuncheapp.android.pearl:id/tv_menu_item","超级大转盘")
            if (zpNode == null) {
                val widthHeight = ScreenUtils.GetWidthAndHeight(App.instance())
                val gesConf = GestureDescHelper.GestureConfig()
                gesConf.beginP = PointF(widthHeight.first / 2f, widthHeight.second / 2f)
                gesConf.endP = PointF(widthHeight.first / 2f, widthHeight.second.toFloat())
                GestureDescHelper.scrollNode(
                    accessibilityService!!,
                    { gestureDescription: GestureDescription ->
                    },
                    {
                    }, gesConf
                )
                return true
            } else {
                GestureDescHelper.tapNodeCenter(accessibilityService!!, zpNode)
                return true
            }
        }
        return false
    }

    // 转盘详情
    private fun zhuanPanDetail(): Boolean {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val dzp = AccesNodeUtil.findNodeByEqualsText(rootWindow, 1, "幸运大转盘")
            if (dzp != null) {
                Thread.sleep(2000)
                // 后可继续
                val hkjx = AccesNodeUtil.findNodeByEqualsText(rootWindow, 1, "后可继续")
                if (hkjx != null) {
                    val zpNextTimeNode = AccesNodeUtil.findNodeByText(hkjx.parent,1,":")
                    Log.e("=================",zpNextTimeNode.toString())
                    if(zpNextTimeNode!=null){
                        luckyZPMissionTime =  System.currentTimeMillis() + DateUtil.getSecondByString(zpNextTimeNode.text) * 1000
                    }
                    accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    return true
                }
                // 继续抽奖
                val gxnNode = AccesNodeUtil.findNodeByText(rootWindow, 1, "恭喜您获得")
                val jxcNode = AccesNodeUtil.findNodeByText(rootWindow, 1, "继续抽奖")
                var kspfbNode = AccesNodeUtil.findNodeByText(rootWindow,1,"看视频金币翻倍")
                if(kspfbNode != null){
                    GestureDescHelper.tapNodeCenter(accessibilityService!!, kspfbNode)
                    return true
                }
                if (gxnNode != null && jxcNode != null) {
                    var children = AccesNodeUtil.findAllNodesByText(gxnNode.parent, 1)
                    GestureDescHelper.tapNodeCenter(accessibilityService!!, children[2])
                    return true
                }
                val syNode = AccesNodeUtil.findNodeByText(rootWindow, 1, "今日剩余")
                val gzNode = AccesNodeUtil.findNodeByText(rootWindow, 1, "每天有20次抽奖机会")
                if (syNode != null&&gzNode!=null) {
                    if (syNode.text.toString() == "今日剩余 : 0次") {
                        luckyZPMissionTime = DateUtil.getTodayEnd().time
                        accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                        return false
                    }
                    var gzRect = Rect()
                    var syRect = Rect()
                    gzNode.getBoundsInScreen(gzRect)
                    syNode.getBoundsInScreen(syRect)
                    var midY = syRect.bottom + (gzRect.top - syRect.bottom) / 2f
                    val widthHeight = ScreenUtils.GetWidthAndHeight(App.instance())
                    GestureDescHelper.tapPoint(
                        accessibilityService!!,
                        PointF(widthHeight.first / 2f, midY)
                    )
                }
            }
        }
        return false
    }

    //红包雨
    private fun hbyMission():Boolean{
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (rootWindow != null && System.currentTimeMillis() >= hbyMissionTime) {
            val honaboyu = AccesNodeUtil.findNodeByResIdThenText(rootWindow,"com.yuncheapp.android.pearl:id/tv_menu_item","红包雨")
            if (honaboyu == null) {
                val zjbNode = AccesNodeUtil.findNodeByResIdThenText(rootWindow,"com.yuncheapp.android.pearl:id/total_text","总金币")
                if(zjbNode!=null){
                    hbyMissionTime = DateUtil.getTodayEnd().time
                    return false
                }
                val widthHeight = ScreenUtils.GetWidthAndHeight(App.instance())
                val gesConf = GestureDescHelper.GestureConfig()
                gesConf.beginP = PointF(widthHeight.first / 2f, widthHeight.second / 2f)
                gesConf.endP = PointF(widthHeight.first / 2f, widthHeight.second.toFloat())
                GestureDescHelper.scrollNode(
                    accessibilityService!!,
                    { gestureDescription: GestureDescription ->
                    },
                    {
                    }, gesConf
                )
                Thread.sleep(2000)
                return true
            } else {
                GestureDescHelper.tapNodeCenter(accessibilityService!!, honaboyu)
                return true
            }
        }
        return false
    }

    //红包雨详情
    private fun hbyDetailMission():Boolean{
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (rootWindow != null) {
            val cqhbyNode = AccesNodeUtil.findNodeByEqualsText(rootWindow, 1, "超强红包雨")
            val titleNode = AccesNodeUtil.findNodeById(rootWindow, 1, "title-container")
            // 今日剩余: 5次
//            val hbygzNode = AccesNodeUtil.findNodeByEqualsText(rootWindow, 1, "活动规则")
            if (cqhbyNode != null) {
                val gksp = AccesNodeUtil.findNodeByText(rootWindow, 1, "观看视频领金币")
                if(gksp!=null){
                    GestureDescHelper.tapNodeCenter(accessibilityService!!,gksp)
                    return true
                }
                val ljksNode = AccesNodeUtil.findNodeByEqualsText(rootWindow, 1, "立即开始")
                if(ljksNode!=null){
                    GestureDescHelper.tapNodeCenter(accessibilityService!!,ljksNode)
                }else{
                    val mrzlNode = AccesNodeUtil.findNodeByEqualsText(rootWindow, 1, "明日再来")
                    if(mrzlNode!=null){
                        accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                        hbyMissionTime = DateUtil.getTodayEnd().time
                        return false
                    }
                    val rainNode = AccesNodeUtil.findNodeById(rootWindow, 1, "rainStage")
                    if(rainNode!=null){
                        var ourRect = Rect()
                        rainNode.getBoundsInScreen(ourRect)
                        if(titleNode!=null){
                            var tOurRect = Rect()
                            titleNode.getBoundsInScreen(tOurRect)
                            ourRect.top = tOurRect.bottom
                        }
                        GestureDescHelper.tapAllArea(accessibilityService!!,ourRect)
                    }
                }
                return true
            }
        }
        return false
    }

    // 视频页滑动
    private fun scrollShiPin() {
        val rootWindow = this.accessibilityService!!.rootInActiveWindow
        if (rootWindow == null) {
            return
        }
        var next = false
        // 获取正在播放的组件
        val playPanelNode = AccesNodeUtil.findNodeById(
            rootWindow,
            1,
            "com.yuncheapp.android.pearl:id/play_panel"
        )
        // 如果有重播则滑动
        val replayNode = AccesNodeUtil.findNodeById(
            rootWindow,
            1,
            "com.yuncheapp.android.pearl:id/replay"
        )
        if (playPanelNode != null) {
            val tvAdNode = AccesNodeUtil.findNodeById(
                playPanelNode.parent,
                1,
                "com.yuncheapp.android.pearl:id/tv_ad_tag"
            )
            if (tvAdNode != null) {
                next = true
            }
        } else {
            next = true
        }
        val widthHeight = ScreenUtils.GetWidthAndHeight(App.instance())
        if (next) {
            GestureDescHelper.scrollNode(
                accessibilityService!!,
                { gestureDescription: GestureDescription ->
                },
                {
                    autoScrolling = false
                },
                GestureDescHelper.GestureConfig(
                    widthHeight.second / 3f,
                    1000,
                    0
                )
            )
        } else if (System.currentTimeMillis() >= moniScrollTime) {
            this.moniScrollTime = System.currentTimeMillis() + 30 * 1000
            GestureDescHelper.scrollNode(
                accessibilityService!!,
                { gestureDescription: GestureDescription ->
//                    GestureDescHelper.scrollNode(
//                        accessibilityService!!, null, null, GestureDescHelper.GestureConfig(
//                            -widthHeight.second / 4f,
//                            1000,
//                            0
//                        )
//                    )
                },
                {
                    autoScrolling = false
                },
                GestureDescHelper.GestureConfig(
                    widthHeight.second / 3f,
                    1000,
                    0
                )
            )
        }
    }

    // 检查执行任务
    private fun execMissions() {
        val rootWindow = accessibilityService!!.rootInActiveWindow
        if (this.execMissionType != MissionType_None) {
            autoScrolling = true
            if (checkGuangGaoWindow()) {
                handler.postDelayed(missionRunnable, 1000)
            } else if (clickAfterOpenBaoXiang()) {
                handler.postDelayed(missionRunnable, 1000)
            }else if(hbyDetailMission()){
                handler.postDelayed(missionRunnable, 1000)
            } else if (zhuanPanDetail()) {
                handler.postDelayed(missionRunnable, 1000)
            } else if (isFuliCenter()) {
                if (clickAdMission()) {

                } else if (clickBX(rootWindow)) {
                }else if (hbyMission()) {

                } else if (zhuanPanMission()) {
                } else {
                    accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                }
                handler.postDelayed(missionRunnable, 1000)
            } else if (clickRedButton()) {
                handler.postDelayed(missionRunnable, 1000)
            } else if (isMainView()) { // 列表页面
                // 搜索旁边的金币 com.yuncheapp.android.pearl:id/gold_view
                val selNode = selectedTabNode()
                //com.yuncheapp.android.pearl:id/initpanel_video_length
                if (selNode!!.text.contains("视频")) {
                    scrollShiPin()
                }
                handler.postDelayed(missionRunnable, 1000)
            } else {
                handler.postDelayed(missionRunnable, 3000)
            }
        }
    }
}