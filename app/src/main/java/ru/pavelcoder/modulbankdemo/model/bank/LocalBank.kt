package ru.pavelcoder.modulbankdemo.model.bank

import ru.pavelcoder.modulbankdemo.model.bank.exception.*
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesSource
import ru.pavelcoder.modulbankdemo.model.db.MoneyRepository
import java.lang.Exception
import kotlin.math.round

class LocalBank (
    private val ratesSource: CurrencyRatesSource,
    private val moneyRepository: MoneyRepository,
    currencies: List<Currency>,
    defaultValues: Map<Currency, Double>
) : Bank {
    private val funds = HashMap<Currency, Double>()

    init {
        loadFundsFromRepositoryOrDefaults(currencies, defaultValues)
    }

    fun setAmount(currencies: Map<Currency, Double>) {
        currencies.forEach { currency ->
            if( currency.value < 0 ) throw AmountNotPositiveException("Currency amount must be >= 0")
        }
        this.funds.clear()
        currencies.toMap(this.funds)
        saveFundsToRepository()
    }

    override fun prepareTransactionWithActualRates(request: TransactionRequest): Transaction {
        val conversionRate = getConversionRate(request.sourceCurrency, request.destinationCurrency)
        return when( request.calcFrom ) {
            TransactionRequest.TransactionCalcFrom.Source -> {
                val sourceAmount = request.amount
                val targetAmount = sourceAmount * conversionRate
                Transaction(request.sourceCurrency, request.destinationCurrency, sourceAmount, targetAmount)
            }
            TransactionRequest.TransactionCalcFrom.Destination -> {
                val targetAmount = request.amount
                val sourceAmount = targetAmount / conversionRate
                Transaction(request.sourceCurrency, request.destinationCurrency, sourceAmount, targetAmount)
            }
        }
    }

    override fun executeTransaction(transaction: Transaction) {
        validateTransaction(transaction)
        val sourceAmount = getAvailableFunds(transaction.sourceCurrency)
        val targetAmount = getAvailableFunds(transaction.destinationCurrency)
        if( sourceAmount < transaction.sourceAmount ) {
            throw NotEnoughFundsException("Available $sourceAmount ${transaction.sourceCurrency},"
                    + " required ${transaction.sourceAmount} ${transaction.sourceCurrency}")
        }
        funds[transaction.sourceCurrency] = (sourceAmount - transaction.sourceAmount).roundToCents()
        funds[transaction.destinationCurrency] = (targetAmount + transaction.destinationAmount).roundToCents()
        saveFundsToRepository()
    }

    override fun getAvailableFunds(currency: Currency): Double {
        return funds[currency] ?: throw NoSuchCurrencyException(currency.toString())
    }

    override fun getAvailableCurrencies(): List<Currency> {
        return funds.keys.toList()
    }

    override fun getConversionRate(from: Currency, to: Currency): Double {
        val sourceRate = ratesSource.rateForCurrency(from)
        val targetRate = ratesSource.rateForCurrency(to)
        return targetRate.rate / sourceRate.rate
    }

    private fun validateTransaction(transaction: Transaction) {
        if( transaction.sourceCurrency == transaction.destinationCurrency ) {
            throw SameCurrencyTransactionException(transaction.sourceCurrency)
        }

        if( transaction.sourceAmount.roundToCents() == 0.0 ) {
            throw ZeroAmountTransactionException("Source amount is 0")
        }

        if( transaction.destinationAmount.roundToCents() == 0.0 ) {
            throw ZeroAmountTransactionException("Destination amount is 0")
        }

        val actualTransaction = prepareTransactionWithActualRates(
            TransactionRequest(
                transaction.sourceCurrency,
                transaction.destinationCurrency,
                transaction.sourceAmount,
                TransactionRequest.TransactionCalcFrom.Source
            )
        )
        if( actualTransaction != transaction ) {
            throw RatesWrongException("Wrong transaction $transaction, actual: $actualTransaction")
        }
    }

    private fun Double.roundToCents() = round(this * 100) / 100.0

    private fun saveFundsToRepository() {
        funds.forEach {
            moneyRepository.saveAmount(it.key, it.value)
        }
    }

    private fun loadFundsFromRepositoryOrDefaults(currencies: List<Currency>, defaultValues: Map<Currency, Double>) {
        try {
            currencies.forEach {currency ->
                val amount = moneyRepository.getAmount(currency)
                funds[currency] = amount
            }
        } catch (e: Exception) {
            funds.clear()
            defaultValues.toMap(funds)
        }
    }
}