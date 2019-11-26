package ru.pavelcoder.modulbankdemo.application

import android.app.Application
import ru.pavelcoder.modulbankdemo.dagger.DaggerHolder
import ru.pavelcoder.modulbankdemo.dagger.DaggerModule

class MBApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DaggerHolder.init(DaggerModule())
    }
}