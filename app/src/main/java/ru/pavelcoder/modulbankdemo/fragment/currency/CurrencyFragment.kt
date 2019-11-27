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

    override fun setAmount(amount: String) {
        fcCurrencyValue.setTextWithoutCallbacks(amount)
    }

    override fun setRate(rate: String) {
        faCurrencyConversion.text = rate
    }

    override fun setAvailableAmount(text: String) {
        fcCurrencyAvailable.text = text
    }

    override fun setAmountPrefix(prefix: String) {
        fcCurrencyValue.prefix = prefix
    }
}