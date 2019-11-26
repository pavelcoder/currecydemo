package ru.pavelcoder.modulbankdemo.dagger

import dagger.Component
import ru.pavelcoder.modulbankdemo.activity.main.MainPresenter
import javax.inject.Singleton

@Singleton
@Component(modules = [DaggerModule::class])
interface DaggerComponent {
    fun inject(mainPresenter: MainPresenter)

}