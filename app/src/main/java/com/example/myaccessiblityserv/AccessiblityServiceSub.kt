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
    companion object{
        private var NotifyId = 1001
    }
    //android.ugc.aweme 抖音    com.kuaishou.nebula快手极速版
    override fun onCreate() {
        super.onCreate()
        KSUtil.getInstance(this).init()
//        startForeground()
    }
    override fun onInterrupt() {
        Log.e("onInterrupt","----------------------")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.e("onUnbind","----------------------")
        KSUtil.getInstance(this).pause()
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        Log.e("--------------",event.toString())
//        if(this.rootInActiveWindow!=null){
//            AccesNodeUtil.logAllNodes(this.rootInActiveWindow,"=",null)
//        }
        if(event!!.eventType===AccessibilityEvent.TYPE_VIEW_CLICKED){
            if(event!!.source!=null){
                Log.e("--------------","${event!!.source.toString()}")
                AccesNodeUtil.anaView(event!!.source.parent,"TYPE_VIEW_CLICKED")
            }
        }
        if(event!!.eventType===AccessibilityEvent.TYPE_VIEW_SCROLLED){
            val pkgName = event!!.getPackageName().toString()
            var className = event.className
            if(pkgName.contains("com.kuaishou.nebula")&&className.contains("androidx.viewpager.widget.ViewPager")){
                // 暂停抖音
                DYUtil.getInstance(this).pause()
                // 执行快手
                KSUtil.getInstance(this).init()
                KSUtil.getInstance(this).execScrollMission()
            } else if(pkgName.contains("com.ss.android.ugc.aweme")){
                // 暂停快手
                KSUtil.getInstance(this).pause()
                // 执行抖音
                DYUtil.getInstance(this).init()
                DYUtil.getInstance(this).execScrollMission()
            }else if(pkgName.contains("com.yuncheapp.android.pearl")){
                // 暂停抖音
                DYUtil.getInstance(this).pause()
                // 暂停快手
                KSUtil.getInstance(this).pause()
                // 开始快点阅读
                KDKUtil.getInstance(this).init()
                KDKUtil.getInstance(this).execScrollMission()
            }
            return
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
    override fun onServiceConnected() {
        super.onServiceConnected()
        Toast.makeText(this,"aaaaaaaaaaaaa",Toast.LENGTH_SHORT)
        Log.e("---------------","onServiceConnected")
    }
}