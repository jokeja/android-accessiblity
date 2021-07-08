package com.example.myaccessiblityserv

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*

private class EditTextWatcher(key:String,type:Int) : TextWatcher{
    var m_key:String
    var m_type:Int // 1 Long 2 String
    init{
        this.m_key = key
        this.m_type = type
    }
    override fun afterTextChanged(s: Editable?) {
        val videoMis = s.toString()
        if(videoMis.isEmpty()){
            Toast.makeText(App.instance(),"未检测到输入",Toast.LENGTH_SHORT).show()
            return
        }
        if(this.m_type==1){
            SharePrefUtil.putLongValue(this.m_key,videoMis.toLong())
        }
        if(this.m_type==2){
            SharePrefUtil.putStringValue(this.m_key,videoMis)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}

open class MainActivity : Activity() {
    private var setAccBtn:Button? = null
    private var videoET: EditText? = null
    private var liveET: EditText? = null
    private var bxKeyET: EditText? = null
    private var ksBtn:Button? = null
    private var dailySwitch:Switch? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.setAccBtn = this.findViewById<Button>(R.id.set_button)
        this.videoET = this.findViewById<EditText>(R.id.video_et)
        this.ksBtn = this.findViewById(R.id.ks_button)
        this.liveET = this.findViewById<EditText>(R.id.live_et)
        this.dailySwitch = this.findViewById(R.id.auto_daily_switch)
        this.bxKeyET = this.findViewById(R.id.bxKey_et)
        this.ksBtn!!.setOnClickListener(View.OnClickListener {
            v:View ->
            KSUtil.restartMission()
            DYUtil.restartMission()
            KDKUtil.restartMission()
        })
        this.videoET!!.addTextChangedListener(EditTextWatcher("videoS",1))
        this.liveET!!.addTextChangedListener(EditTextWatcher("liveS",1))
        this.bxKeyET!!.addTextChangedListener(EditTextWatcher("bxKeyword",2))
        this.videoET!!.setText(SharePrefUtil.getLongValue("videoS").toString(),TextView.BufferType.NORMAL)
        this.liveET!!.setText(SharePrefUtil.getLongValue("liveS").toString(),TextView.BufferType.NORMAL)
        this.bxKeyET!!.setText(SharePrefUtil.getStringValue("bxKeyword"),TextView.BufferType.NORMAL)

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
