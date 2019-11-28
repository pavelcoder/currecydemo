package ru.pavelcoder.modulbankdemo.activity.main

import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentIdentifier
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenter
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenterProvider

class CurrencyFragmentPresentersHolder(
    private val presenter: MainPresenter
): CurrencyFragmentPresenterProvider {

    private val currencyPresenters = HashMap<CurrencyFragmentIdentifier, CurrencyFragmentPresenter>()

    override fun providePresenter(identifier: CurrencyFragmentIdentifier): CurrencyFragmentPresenter {
        return currencyPresenters[identifier]
            ?: CurrencyFragmentPresenter(
                identifier,
                presenter
            ).apply {
                currencyPresenters[identifier] = this
            }
    }
}