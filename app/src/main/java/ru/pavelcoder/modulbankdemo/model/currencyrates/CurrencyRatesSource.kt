package ru.pavelcoder.modulbankdemo.model.currencyrates

import ru.pavelcoder.modulbankdemo.model.bank.Currency
import ru.pavelcoder.modulbankdemo.model.bank.CurrencyRate

interface CurrencyRatesSource {
    fun rateForCurrency(currency: Currency): CurrencyRate
}