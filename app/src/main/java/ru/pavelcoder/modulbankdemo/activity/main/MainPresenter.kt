package ru.pavelcoder.modulbankdemo.activity.main

import moxy.InjectViewState
import moxy.MvpPresenter
import ru.pavelcoder.modulbankdemo.dagger.DaggerHolder
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesFetcher
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesListener
import javax.inject.Inject

@InjectViewState
class MainPresenter : MvpPresenter<MainActivityView>(), CurrencyRatesListener {

    @Inject
    internal lateinit var currencyRates: CurrencyRatesFetcher

    init {
        DaggerHolder.getDagger().inject(this)
        currencyRates.addListener(this)
    }

    override fun onNewCurrencyRates() {

    }
}