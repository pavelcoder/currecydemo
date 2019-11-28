package ru.pavelcoder.modulbankdemo.model.db

import ru.pavelcoder.modulbankdemo.model.bank.Currency

interface MoneyRepository {
    fun saveAmount(currency: Currency, amount: Double)
    fun getAmount(currency: Currency): Double
}