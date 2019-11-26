package ru.pavelcoder.modulbankdemo.model.account

enum class Currency(
    val code: String
) {
    EUR("EUR"),
    USD("USD"),
    GBP("GBP");

    companion object {
        fun currencyForCode(code: String) = values().find { it.code == code }
    }
}