package ru.pavelcoder.modulbankdemo.activity.main

import android.os.Bundle
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import ru.pavelcoder.modulbankdemo.R

class MainActivity : MvpAppCompatActivity(), MainActivityView {

    @InjectPresenter
    internal lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}
