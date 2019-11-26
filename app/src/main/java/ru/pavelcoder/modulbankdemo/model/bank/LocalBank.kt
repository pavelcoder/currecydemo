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

    override fun prepareTransaction(sourceCurrency: Currency, sourceAmount: Long, targetCurrency: Currency): Transaction {
        val sourceRate = ratesSource.rateForCurrency(sourceCurrency)
        val targetRate = ratesSource.rateForCurrency(targetCurrency)
        val conversionRate = targetRate.rate / sourceRate.rate
        val targetAmount = (sourceAmount * conversionRate).toLong()
        return Transaction(sourceCurrency, targetCurrency, sourceAmount, targetAmount)
    }

    override fun prepareTransaction(sourceCurrency: Currency, targetCurrency: Currency, targetAmount: Long): Transaction {
        val sourceRate = ratesSource.rateForCurrency(sourceCurrency)
        val targetRate = ratesSource.rateForCurrency(targetCurrency)
        val conversionRate = targetRate.rate / sourceRate.rate
        val sourceAmount = (targetAmount / conversionRate).toLong()
        return Transaction(sourceCurrency, targetCurrency, sourceAmount, targetAmount)
    }

    override fun executeTransaction(transaction: Transaction) {
        validateTransaction(transaction)
        val sourceAmount = getAvailableFunds(transaction.sourceCurrency)
        val targetAmount = getAvailableFunds(transaction.targetCurrency)
        if( sourceAmount < transaction.sourceAmount ) {
            throw NotEnoughFundsException("Available $sourceAmount ${transaction.sourceCurrency}, required ${transaction.sourceAmount}")
        }
        currencies[transaction.sourceCurrency] = sourceAmount - transaction.sourceAmount
        currencies[transaction.targetCurrency] = targetAmount + transaction.targetAmount
    }

    override fun getAvailableFunds(currency: Currency): Long {
        return currencies[currency] ?: throw NoSuchCurrencyException(currency.toString())
    }

    override fun getAvailableCurrencies(): List<Currency> {
        return currencies.keys.toList()
    }

    private fun validateTransaction(transaction: Transaction) {
        val actualTransaction = prepareTransaction(
            transaction.sourceCurrency,
            transaction.sourceAmount,
            transaction.targetCurrency
        )
        if( actualTransaction != transaction ) {
            throw RatesWrongExceptionException("Wrong transaction $transaction, actual: $actualTransaction")
        }
    }
}