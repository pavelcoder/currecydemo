package ru.pavelcoder.modulbankdemo.fragment.currency

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_currency.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.pavelcoder.modulbankdemo.R


class CurrencyFragment : MvpAppCompatFragment(R.layout.fragment_currency), CurrencyView {
    companion object {
        private const val PAYLOAD = "PAYLOAD"
        fun instance(params: CurrencyFragmentIdentifier) = CurrencyFragment().apply {
            arguments = Bundle().apply {
                putSerializable(PAYLOAD, params)
            }
        }
    }

    @InjectPresenter
    internal lateinit var fragmentPresenter: CurrencyFragmentPresenter

    @ProvidePresenter
    internal fun providePresenter(): CurrencyFragmentPresenter {
        val presenterProvider = activity as CurrencyFragmentPresenterProvider
        return presenterProvider.providePresenter(getParams())
    }

    private fun getParams() = arguments?.getSerializable(PAYLOAD) as CurrencyFragmentIdentifier

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fcCurrencyValue.onTextChanged = {amount ->
            fragmentPresenter.onAmountChanged(amount)
        }
    }

    override fun setCurrency(name: String) {
        fcCurrencyName.text = name
    }

    override fun setAmount(amount: Float) {
        val formatted = String.format(getString(R.string.currency_amount_format), amount / 100f)
        fcCurrencyValue.setTextWithoutCallbacks(formatted)
    }

    override fun setRate(leftAmount: Float, leftSymbol: String, rightAmount: Float, rightSymbol: String) {
        val text = String.format(getString(R.string.rate_format), leftAmount, leftSymbol, rightAmount, rightSymbol)
        faCurrencyConversion.text = text
    }

    override fun setAvailableAmount(available: Float, symbol: String) {
        val text = getString(R.string.currency_you_have, available, symbol)
        fcCurrencyAvailable.text = text
    }

    override fun setAmountPrefix(prefix: String) {
        fcCurrencyValue.prefix = prefix
    }
}