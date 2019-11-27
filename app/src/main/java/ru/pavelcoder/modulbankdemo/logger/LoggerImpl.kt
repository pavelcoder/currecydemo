package ru.pavelcoder.modulbankdemo.logger

import android.util.Log

class LoggerImpl(
    private val silent: Boolean
) : Logger {
    override fun log(str: String) {
        if( ! silent ) {
            Log.d(Logger::class.simpleName, str)
        }
    }

    override fun log(e: Exception) {
        if( ! silent ) {
            e.printStackTrace()
        }
    }

}