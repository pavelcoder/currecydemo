package ru.pavelcoder.modulbankdemo.model.bank

enum class Currency(
    val code: String
) {
    EUR("EUR"),
    USD("USD"),
    RUB("RUB"),
    GBP("GBP");

    companion object {
        fun currencyForCode(code: String) = values().find { it.code == code }
    }
}