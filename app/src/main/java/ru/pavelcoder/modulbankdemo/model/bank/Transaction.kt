package ru.pavelcoder.modulbankdemo.model.bank

data class Transaction(
    val sourceCurrency: Currency,
    val targetCurrency: Currency,
    val sourceAmount: Long,
    val targetAmount: Long
)