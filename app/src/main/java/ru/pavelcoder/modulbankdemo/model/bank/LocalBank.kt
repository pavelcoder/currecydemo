package ru.pavelcoder.modulbankdemo.model.bank

import ru.pavelcoder.modulbankdemo.model.bank.exception.*
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesSource

class LocalBank (
    private val ratesSource: CurrencyRatesSource
) : Bank {
    private val currencies = HashMap<Currency, Long>()

    fun setAmount(currencies: Map<Currency, Long>) {
        currencies.forEach { currency ->
            if( currency.value < 0 ) throw AmountNotPositiveException("Currency amount must be >= 0")
        }
        this.currencies.clear()
        currencies.toMap(this.currencies)
    }

    override fun prepareTransactionWithActualRates(request: TransactionRequest): Transaction {
        val sourceRate = ratesSource.rateForCurrency(request.sourceCurrency)
        val targetRate = ratesSource.rateForCurrency(request.destinationCurrency)
        val conversionRate = targetRate.rate / sourceRate.rate
        return when( request.calcFrom ) {
            TransactionRequest.TransactionCalcFrom.Source -> {
                val sourceAmount = request.amount
                val targetAmount = (sourceAmount * conversionRate).toLong()
                Transaction(request.sourceCurrency, request.destinationCurrency, sourceAmount, targetAmount, conversionRate)
            }
            TransactionRequest.TransactionCalcFrom.Destination -> {
                val targetAmount = request.amount
                val sourceAmount = (targetAmount / conversionRate).toLong()
                Transaction(request.sourceCurrency, request.destinationCurrency, sourceAmount, targetAmount, conversionRate)
            }
        }
    }

    override fun executeTransaction(transaction: Transaction) {
        validateTransaction(transaction)
        val sourceAmount = getAvailableFunds(transaction.sourceCurrency)
        val targetAmount = getAvailableFunds(transaction.destinationCurrency)
        if( sourceAmount < transaction.sourceAmount ) {
            throw NotEnoughFundsException("Available $sourceAmount ${transaction.sourceCurrency}, required ${transaction.sourceAmount}")
        }
        currencies[transaction.sourceCurrency] = sourceAmount - transaction.sourceAmount
        currencies[transaction.destinationCurrency] = targetAmount + transaction.destinationAmount
    }

    override fun getAvailableFunds(currency: Currency): Long {
        return currencies[currency] ?: throw NoSuchCurrencyException(currency.toString())
    }

    override fun getAvailableCurrencies(): List<Currency> {
        return currencies.keys.toList()
    }

    private fun validateTransaction(transaction: Transaction) {
        val actualTransaction = prepareTransactionWithActualRates(
            TransactionRequest(
                transaction.sourceCurrency,
                transaction.destinationCurrency,
                transaction.sourceAmount,
                TransactionRequest.TransactionCalcFrom.Source
            )
        )
        if( actualTransaction != transaction ) {
            throw RatesWrongExceptionException("Wrong transaction $transaction, actual: $actualTransaction")
        }
    }
}