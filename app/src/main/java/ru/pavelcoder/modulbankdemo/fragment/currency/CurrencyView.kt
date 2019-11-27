package ru.pavelcoder.modulbankdemo.fragment.currency

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface CurrencyView : MvpView {
    fun setCurrency(name: String)
    fun setAmount(amount: String)
    fun setRate(rate: String)
    fun setAvailableAmount(text: String)
    fun setAmountPrefix(prefix: String)
}