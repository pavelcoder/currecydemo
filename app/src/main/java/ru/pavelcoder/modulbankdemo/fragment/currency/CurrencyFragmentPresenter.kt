package ru.pavelcoder.modulbankdemo.fragment.currency

import android.content.Context
import moxy.InjectViewState
import moxy.MvpPresenter
import ru.pavelcoder.modulbankdemo.R
import ru.pavelcoder.modulbankdemo.activity.main.MainPresenter
import ru.pavelcoder.modulbankdemo.dagger.DaggerHolder
import javax.inject.Inject

@InjectViewState
class CurrencyFragmentPresenter(
    private val identifier: CurrencyFragmentIdentifier,
    private val parent: MainPresenter
) : MvpPresenter<CurrencyView>() {

    @Inject
    internal lateinit var applicationContext: Context

    init {
        DaggerHolder.getDagger().inject(this)
    }

    fun setCurrency(currency: String) {
        viewState.setCurrency(currency)
    }

    fun setAmount(amount: Float) {
        val formatted = if( amount == 0f ) {
            ""
        } else {
            String.format(applicationContext.getString(R.string.currency_amount_format), amount)
        }
        viewState.setAmount(formatted)
    }

    fun setRate(leftAmount: Float, leftSymbol: String, rightAmount: Float, rightSymbol: String) {
        val formatted = String.format(
            applicationContext.getString(R.string.rate_format),
            leftAmount,
            leftSymbol,
            rightAmount,
            rightSymbol
        )
        viewState.setRate(formatted)
    }

    fun setAmountPrefix(prefix: String) {
        viewState.setAmountPrefix(prefix)
    }

    fun setAvailableAmount(available: Float, symbol: String) {
        val text = applicationContext.getString(R.string.currency_you_have, available, symbol)
        viewState.setAvailableAmount(text)
    }

    fun onAmountChanged(newAmount: String) {
        val amount = newAmount.replace(',', '.').toFloatOrNull() ?: 0f
        parent.onAmountChanged(identifier, amount)
    }
}