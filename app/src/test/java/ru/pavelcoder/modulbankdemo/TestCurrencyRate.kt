package ru.pavelcoder.modulbankdemo

import ru.pavelcoder.modulbankdemo.model.bank.Currency
import ru.pavelcoder.modulbankdemo.model.bank.CurrencyRate
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesSource

class TestCurrencyRate(
    private val rates: Map<Currency, CurrencyRate>
) : CurrencyRatesSource {
    override fun rateForCurrency(currency: Currency): CurrencyRate {
        return rates[currency]!!
    }

}