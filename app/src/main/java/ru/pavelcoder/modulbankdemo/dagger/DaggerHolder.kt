package ru.pavelcoder.modulbankdemo.dagger

object DaggerHolder {
    private var instanse: DaggerComponent? = null

    fun init(module: DaggerModule) {
        instanse = DaggerDaggerComponent.builder().daggerModule(module).build()
    }

    fun getDagger() = instanse!!

}