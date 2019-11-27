package ru.pavelcoder.modulbankdemo.model.bank

enum class Currency(
    val code: String,
    val symbol: String
) {
    EUR("EUR", "€"),
    USD("USD", "\$"),
    RUB("RUB", "\u20BD"),
    GBP("GBP", "£");

    companion object {
        fun currencyForCode(code: String) = values().find { it.code == code }
    }


}