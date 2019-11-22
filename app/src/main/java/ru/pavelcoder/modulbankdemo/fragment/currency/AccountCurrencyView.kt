package ru.pavelcoder.modulbankdemo.fragment.currency

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface AccountCurrencyView : MvpView {

}