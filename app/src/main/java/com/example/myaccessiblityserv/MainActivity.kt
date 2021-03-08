package com.example.myaccessiblityserv

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*

open class MainActivity : Activity() {
    private var setAccBtn:Button? = null
    private var videoET: EditText? = null
    private var liveET: EditText? = null
    private var confirmBtn: Button? = null
    private var ksBtn:Button? = null
    private var dailySwitch:Switch? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.setAccBtn = this.findViewById<Button>(R.id.set_button)
        this.confirmBtn = this.findViewById<Button>(R.id.confirm_button)
        this.videoET = this.findViewById<EditText>(R.id.video_et)
        this.ksBtn = this.findViewById(R.id.ks_button)
        this.liveET = this.findViewById<EditText>(R.id.live_et)
        this.dailySwitch = this.findViewById(R.id.auto_daily_switch)
        this.confirmBtn!!.setOnClickListener(View.OnClickListener {
            v:View ->
            val videoMis = videoET!!.text.toString()
            if(videoMis.isEmpty()){
                Toast.makeText(applicationContext,"请输入视频间隔",Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val liveMis = liveET!!.text.toString()
            if(liveMis.isEmpty()){
                Toast.makeText(applicationContext,"请输入直播间隔",Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            Log.e("videoET!.text",videoMis)
            Log.e("liveET!.text",liveMis)
            SharePrefUtil.putLongValue("videoS",videoMis.toLong())
            SharePrefUtil.putLongValue("liveS",liveMis.toLong())
            Toast.makeText(applicationContext,"保存成功",Toast.LENGTH_SHORT).show()
        })
        this.ksBtn!!.setOnClickListener(View.OnClickListener {
            v:View ->
            SharePrefUtil.fuLiDailyMissionBegin()
            KSUtil.restartMission()
            DYUtil.restartMission()
        })
        this.videoET!!.setText(SharePrefUtil.getLongValue("videoS").toString(),TextView.BufferType.NORMAL)
        this.liveET!!.setText(SharePrefUtil.getLongValue("liveS").toString(),TextView.BufferType.NORMAL)

        this.dailySwitch!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener{
            buttonView:CompoundButton,isChecked:Boolean ->
            SharePrefUtil.putAutoDailyMission(isChecked)
        })
        this.dailySwitch!!.isChecked = SharePrefUtil.autoDailyMission()

    }

    override fun onResume() {
        super.onResume()
        println("hhhhhhhhhhhhhhhhhhhhhhhh")
        var accessEnable = Settings.Secure.getInt(this.contentResolver,Settings.Secure.ACCESSIBILITY_ENABLED)
        this.setAccBtn!!.visibility = View.VISIBLE
        this.setAccBtn!!.setOnClickListener(View.OnClickListener {
                v: View? ->
            Log.e(v.toString(),"asd")
            var intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        })
//        var accessServices = Settings.Secure.getString(this.contentResolver,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
//        Log.e("'================'",accessServices)
    }
    override fun onPause() {
        super.onPause()
        Log.e("MainActivity","onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.e("MainActivity","onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("MainActivity","onDestroy")

    }
}
