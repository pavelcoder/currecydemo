package ru.pavelcoder.modulbankdemo.model.bank.exception

import ru.pavelcoder.modulbankdemo.model.bank.Currency

class SameCurrencyTransactionException(
    private val currency: Currency): Exception()