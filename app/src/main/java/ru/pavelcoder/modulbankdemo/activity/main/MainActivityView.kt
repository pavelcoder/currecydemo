package ru.pavelcoder.modulbankdemo.activity.main

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentIdentifier

@StateStrategyType(AddToEndSingleStrategy::class)
interface MainActivityView : MvpView {
    fun setState(state: MainViewState)
    fun showDestinationCurrencies(destinationIdentifiers: List<CurrencyFragmentIdentifier>)
    fun showSourceCurrencies(sourceIdentifiers: List<CurrencyFragmentIdentifier>)
    fun setToolbarTitle(title: String)
    fun setSelectedSourceCurrency(position: Int)
    fun setSelectedDestinationCurrency(position: Int)
}