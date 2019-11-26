package ru.pavelcoder.modulbankdemo.activity.main

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentIdentifier

@StateStrategyType(AddToEndSingleStrategy::class)
interface MainActivityView : MvpView {
    fun setLoading(loading: Boolean)
    fun showDestinationCurrencies(destinationIdentifiers: List<CurrencyFragmentIdentifier>)
    fun showSourceCurrencies(sourceIdentifiers: List<CurrencyFragmentIdentifier>)

}