package ru.pavelcoder.modulbankdemo.fragment.currency

interface CurrencyFragmentPresenterProvider {
    fun providePresenter(identifier: CurrencyFragmentIdentifier): CurrencyFragmentPresenter
}