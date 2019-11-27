package ru.pavelcoder.modulbankdemo.model.bank

/**
 * amount of money: Long = count of 1/100 of 1 unit of currency. Not double to avoid double rounding.
 */
interface Bank {
    fun prepareTransactionWithActualRates(request: TransactionRequest): Transaction
    fun executeTransaction(transaction: Transaction)
    fun getAvailableFunds(currency: Currency): Long
    fun getAvailableCurrencies(): List<Currency>
}