package ru.pavelcoder.modulbankdemo.model.bank

data class TransactionRequest(
    val sourceCurrency: Currency,
    val destinationCurrency: Currency,
    val amount: Long,
    val calcFrom: TransactionCalcFrom
) {

    enum class TransactionCalcFrom {
        Source, Destination
    }
}