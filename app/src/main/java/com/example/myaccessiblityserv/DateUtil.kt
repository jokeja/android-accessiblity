package com.example.myaccessiblityserv

import android.util.Log
import java.util.*

class DateUtil {
    companion object {
        private fun getYMDHmS(date: Date): Array<Int> {
            val currentCalendar = Calendar.getInstance()
            currentCalendar.time = Date()
            val year = currentCalendar.get(Calendar.YEAR)
            val month = currentCalendar.get(Calendar.MONTH)
            val date = currentCalendar.get(Calendar.DATE)
            val hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = currentCalendar.get(Calendar.MINUTE)
            val second = currentCalendar.get(Calendar.SECOND)
            return arrayOf(year, month, date, hour, minute, second)
        }

        fun getTodayEnd(): Date {
            val today = getYMDHmS(Date())
            val calendar = Calendar.getInstance()
            calendar.set(today[0], today[1], today[2],23,59,59)
            Log.e("------getTodayEnd------",calendar.time.time.toString())
            return calendar.time
        }

        fun nowAdd(minute: Int, second: Int): Date {
            val today = getYMDHmS(Date())
            val calendar = Calendar.getInstance()
            calendar.set(
                today[0],
                today[1],
                today[2],
                today[3],
                today[4] + minute,
                today[5] + second
            )
            val result = calendar.time
            return result
        }

        fun getSecondByString(formatStr: CharSequence?): Long {
            if(formatStr==null){
                return 0
            }
            val minute = formatStr.substring(0, 2).toLong()
            val second = formatStr.substring(3, 5).toLong()
            return minute * 60 + second
        }
    }
}