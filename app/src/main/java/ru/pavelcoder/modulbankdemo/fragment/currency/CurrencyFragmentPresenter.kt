package ru.pavelcoder.modulbankdemo.fragment.currency

import moxy.InjectViewState
import moxy.MvpPresenter
import ru.pavelcoder.modulbankdemo.activity.main.MainPresenter

@InjectViewState
class CurrencyFragmentPresenter(
    private val identifier: CurrencyFragmentIdentifier,
    private val parent: MainPresenter
) : MvpPresenter<CurrencyView>() {

    private var prefix = ""
    private var amount: Float? = null

    fun setCurrency(currency: String) {
        viewState.setCurrency(currency)
    }

    fun setAmount(amount: Float) {
        this.amount = amount
        updateAmount()
    }

    fun setRate(leftAmount: Float, leftSymbol: String, rightAmount: Float, rightSymbol: String) {
        viewState.setRate(leftAmount, leftSymbol, rightAmount, rightSymbol)
    }

    fun setAmountPrefix(prefix: String) {
        this.prefix = prefix
        updateAmount()
    }

    fun setAvailableAmount(available: Float, symbol: String) {
        viewState.setAvailableAmount(available, symbol)
    }

    private fun updateAmount() {
        viewState.setAmount(prefix, amount ?: 0f)
    }

    fun onAmountChanged(newAmount: String) {

    }
}