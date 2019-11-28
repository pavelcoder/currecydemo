package ru.pavelcoder.modulbankdemo.model.bank

interface Bank {
    fun prepareTransactionWithActualRates(request: TransactionRequest): Transaction
    /**
     * Execute transaction of throw exception
     */
    fun executeTransaction(transaction: Transaction)
    fun getAvailableFunds(currency: Currency): Double
    fun getAvailableCurrencies(): List<Currency>
    fun getConversionRate(from: Currency, to: Currency): Double
}