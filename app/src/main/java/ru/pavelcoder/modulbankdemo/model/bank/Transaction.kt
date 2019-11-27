package ru.pavelcoder.modulbankdemo.model.bank

data class Transaction(
    val sourceCurrency: Currency,
    val destinationCurrency: Currency,
    val sourceAmount: Long,
    val destinationAmount: Long
)