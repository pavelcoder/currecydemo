package ru.pavelcoder.modulbankdemo.fragment.currency

import moxy.InjectViewState
import moxy.MvpPresenter
import ru.pavelcoder.modulbankdemo.activity.main.MainPresenter

@InjectViewState
class CurrencyFragmentPresenter(
    private val identifier: CurrencyFragmentIdentifier,
    private val parent: MainPresenter
) : MvpPresenter<CurrencyView>() {

    fun setCurrency(currency: String) {
        viewState.setCurrency(currency)
    }

    fun setAmount(amount: Float) {
        viewState.setAmount(amount)
    }

    fun setRate(leftAmount: Float, leftSymbol: String, rightAmount: Float, rightSymbol: String) {
        viewState.setRate(leftAmount, leftSymbol, rightAmount, rightSymbol)
    }

    fun setAmountPrefix(prefix: String) {
        viewState.setAmountPrefix(prefix)
    }

    fun setAvailableAmount(available: Float, symbol: String) {
        viewState.setAvailableAmount(available, symbol)
    }

    fun onAmountChanged(newAmount: String) {
        val amount = newAmount.replace(',', '.').toFloatOrNull()
        if( amount != null ) {
            parent.onAmountChanged(identifier, amount)
        }
    }
}