package com.example.myaccessiblityserv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.gesture.GestureStroke
import android.graphics.BitmapFactory
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
//import sun.invoke.util.VerifyAccess.getPackageName
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast


class AccessiblityServiceSub : AccessibilityService()  {
    private var widthHeight = Pair(1080,2244)
    private var handler = Handler()
    private var startAutoScroll = false
    private var delayMis = arrayOf(5000L,5560L,5500L,5723L,6000L,6260L,6546L,6843L,7000L,7345L,7645L,7948L,8000L)
    companion object{
        private var NotifyId = 1001
    }
    //android.ugc.aweme 抖音    com.kuaishou.nebula快手极速版
    private var autoType = -1
    override fun onCreate() {
        super.onCreate()
        this.widthHeight = ScreenUtils.GetWidthAndHeight(this)
        Log.e("create",widthHeight.toString())
//        startForeground()
    }
    override fun onInterrupt() {
        Log.e("onInterrupt","----------------------")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.e("onUnbind","----------------------")
        this.startAutoScroll = false
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var builder = Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        var nfIntent = Intent(this.packageName);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)).setLargeIcon(
            BitmapFactory.decodeResource(this.getResources(),R.mipmap.ic_launcher)).setContentTitle("下拉列表中的Title").setSmallIcon(R.mipmap.ic_launcher).setContentText("要显示的内容").setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        var notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(NotifyId,notification)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.e("onDestroy","----------------------")
        super.onDestroy()
    }

    fun findAllNodes(parentNode: AccessibilityNodeInfo,space: String,canPerform: ((AccessibilityNodeInfo)->Boolean)?){
        Log.e(space,parentNode.toString())
        for (index in 0 .. parentNode.childCount-1){
            var child = parentNode.getChild(index)
            if(child!=null){
//                if(child.text!=null&&child.text.contains("转发 ")&&canPerform(child)){
//                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                }
                findAllNodes(child,space+"-",canPerform)
            }
//
        }
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if(event!!.eventType!==AccessibilityEvent.TYPE_VIEW_SCROLLED){
            return
        }
        Log.e("--------------",event.toString())
//        Log.e("------this.startAutoScroll--------",""+this.startAutoScroll)
        if(!this.startAutoScroll){
            val pkgName = event!!.getPackageName().toString()
            var className = event.className
            Log.e("---------className---------",className.toString())
            if(pkgName.contains("com.kuaishou.nebula")&&className.contains("androidx.viewpager.widget.ViewPager")){
                // 快手极速版
                autoType = 1
                var delayMis = SharePrefUtil.getLongValue(applicationContext,"videoS")
                if(rootInActiveWindow!=null){
                    // 查找直播关键字
                    var nodeList = rootInActiveWindow.findAccessibilityNodeInfosByText("说点什么")
                    if(nodeList!=null&&nodeList.size>0){//是否是直播页面 30秒滑屏
                        delayMis = SharePrefUtil.getLongValue(applicationContext,"liveS")
                        Log.e("------nodeList--------",nodeList.toString())
                    }else{ // 视频页面 检查是否是图集
                        var tuNodeList = rootInActiveWindow.findAccessibilityNodeInfosByText("打开图集")
                        if(tuNodeList!=null&&tuNodeList.size>0){
                            delayMis = 5L
                        }
                    }
                }
                Log.e("------step--------","11111111111111111111111111111111111111111")
                this.startAutoScroll = true
                this.startScroll(delayMis*1000)
            } else if(pkgName.contains("com.ss.android.ugc.aweme")){
                // 抖音
                autoType = 2
            }else{
                autoType = -1
            }
        }

        // 此方法是在主线程中回调过来的，所以消息是阻塞执行的
        // 获取包名
//        val pkgName = event!!.getPackageName().toString()
//        val eventType = event.getEventType()
        // AccessibilityOperator封装了辅助功能的界面查找与模拟点击事件等操作

//        AccessibilityOperator.getInstance().updateEvent(this, event)
//        AccessibilityLog.printLog("eventType: $eventType pkgName: $pkgName")
//        when (eventType) {
//            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
//            }
//        }
    }
    private  fun startScroll(delay:Long){
        Log.e("------delay--------",""+delay)
        Log.e("------step--------","2222222222222222222222222222222222222")
        var path = Path()
        path.moveTo((this.widthHeight.first/2f), this.widthHeight.second-150f);//设置Path的起点
        path.lineTo((this.widthHeight.first/2f),150f);
        var builder = GestureDescription.Builder()
        var stroke = GestureDescription.StrokeDescription(path,0,800,false)
        var callback = object: GestureResultCallback() {
            override fun onCompleted(gestureDescription:GestureDescription){
//                Log.e("---------onCompleted---------",gestureDescription.toString())
//                if(startAutoScroll){
//                    startScroll()
//                }
                Log.e("------step--------","66666666666666 66666666666666666666666666")
            }
            override fun onCancelled(gestureDescription:GestureDescription){
//                Log.e("---------onCancelled---------",gestureDescription.toString())
//                startAutoScroll = false
                Log.e("------step--------","7777777777777777777777777777777777777777")
            }
        }
        var dispatchHandler = Handler(Handler.Callback {
            msg ->
            Log.e("dispatchHandler",msg.toString())
            true
        })

        var index = (0 until (delayMis.size)).random()
        handler.postDelayed(Runnable {
            startAutoScroll = false
            Log.e("------step--------","44444444444444444444444444444444444444444444")
            var dispatchGestureresult= dispatchGesture(builder.addStroke(stroke).build(),callback,dispatchHandler)
            Log.e("-----dispatchGesture----result---------",""+dispatchGestureresult)
            Log.e("------step--------","555555555555555555555555555555555555555555555")
        },delay)
        Log.e("------step--------","333333333333333333333333333333333333")
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        Toast.makeText(this,"aaaaaaaaaaaaa",Toast.LENGTH_SHORT)
        Log.e("---------------","onServiceConnected")
    }
}