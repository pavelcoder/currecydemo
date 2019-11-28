package ru.pavelcoder.modulbankdemo.model.bank

data class CurrencyRate (
    val base: Currency,
    val target: Currency,
    val rate: Double
)