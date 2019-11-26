package ru.pavelcoder.modulbankdemo.model.account

data class CurrencyRate (
    val base: Currency,
    val target: Currency,
    val rate: Double
)