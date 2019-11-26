package ru.pavelcoder.modulbankdemo.model.currencyrates

interface CurrencyRatesListener {
    /**
     * Called when currencies fetched successfully
     */
    fun onNewCurrencyRates()
}