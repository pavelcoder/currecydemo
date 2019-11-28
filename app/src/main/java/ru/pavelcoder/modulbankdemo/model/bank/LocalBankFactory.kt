package ru.pavelcoder.modulbankdemo.model.bank

import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesSource
import ru.pavelcoder.modulbankdemo.model.db.MoneyRepository

class LocalBankFactory {
    companion object {
        private const val DEFAULT_FUNDS = 100.0

        fun createBank(ratesFetcher: CurrencyRatesSource, moneyRepository: MoneyRepository): Bank {
            val currencies = Currency.values().toList()
            val defaultFunds = currencies.map { currency ->
                currency to DEFAULT_FUNDS
            }.toMap()
            return LocalBank(ratesFetcher, moneyRepository, currencies, defaultFunds)
        }
    }
}