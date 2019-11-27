package ru.pavelcoder.modulbankdemo.fragment.currency

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface CurrencyView : MvpView {
    fun setCurrency(name: String)
    fun setAmount(amount: Float)
    fun setRate(leftAmount: Float, leftSymbol: String, rightAmount: Float, rightSymbol: String)
    fun setAvailableAmount(available: Float, symbol: String)
    fun setAmountPrefix(prefix: String)
}