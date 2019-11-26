package ru.pavelcoder.modulbankdemo.model.bank

import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesSource

class LocalBankFactory {
    companion object {
        private const val DEFAULT_FUNDS = 100_00L

        fun createBank(ratesFetcher: CurrencyRatesSource): Bank {
            val localBank = LocalBank(ratesFetcher)
            val funds = Currency.values().map {
                it to DEFAULT_FUNDS
            }.toMap()
            localBank.setAmount(funds)
            return localBank
        }
    }
}