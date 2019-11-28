package ru.pavelcoder.modulbankdemo

import org.junit.Test
import org.mockito.Mockito.*
import ru.pavelcoder.modulbankdemo.model.bank.Currency
import ru.pavelcoder.modulbankdemo.model.bank.CurrencyRate
import ru.pavelcoder.modulbankdemo.model.bank.LocalBank
import ru.pavelcoder.modulbankdemo.model.bank.Transaction
import ru.pavelcoder.modulbankdemo.model.bank.exception.RatesWrongException
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesSource
import ru.pavelcoder.modulbankdemo.model.db.MoneyRepository

/**
 * Two sample tests
 */
class ExampleUnitTest {
    companion object {
        private const val rateEurToRub = 10.0
    }

    private fun createRatesSource(): CurrencyRatesSource {
        val currencyRatesSource = mock(CurrencyRatesSource::class.java)
        `when`(currencyRatesSource.rateForCurrency(Currency.EUR)).thenReturn(
            CurrencyRate(Currency.EUR, Currency.EUR, 1.0)
        )
        `when`(currencyRatesSource.rateForCurrency(Currency.RUB)).thenReturn(
            CurrencyRate(Currency.EUR, Currency.RUB, rateEurToRub)
        )
        return currencyRatesSource
    }

    private fun createEmptyRepository(): MoneyRepository {
        val moneyRepository = mock(MoneyRepository::class.java)
        Currency.values().forEach { currency ->
            doThrow(RuntimeException()).`when`(moneyRepository).getAmount(currency)
        }
        return moneyRepository
    }

    @Test
    fun sampleBankTransactionChangeAmountCorrectTest() {
        val initialRubs = 100.0
        val default = hashMapOf(
            Currency.EUR to 100.0,
            Currency.RUB to initialRubs
        )
        val bank = LocalBank(createRatesSource(), createEmptyRepository(), default.keys.toList(), default)
        val eurToTransfer = 5.0
        bank.executeTransaction(Transaction(
            Currency.EUR,
            Currency.RUB,
            eurToTransfer,
            eurToTransfer * rateEurToRub
        ))
        val rubsShouldBecome = initialRubs + eurToTransfer * rateEurToRub
        assert( bank.getAvailableFunds(Currency.RUB) == rubsShouldBecome )
    }

    @Test(expected = RatesWrongException::class)
    fun sampleBankTransactionExceptionTest() {
        val initialRubs = 100.0
        val default = hashMapOf(
            Currency.EUR to 100.0,
            Currency.RUB to initialRubs
        )
        val bank = LocalBank(createRatesSource(), createEmptyRepository(), default.keys.toList(), default)
        val eurToTransfer = 5.0
        val wrongRubsAmount = 1.0
        bank.executeTransaction(Transaction(
            Currency.EUR,
            Currency.RUB,
            eurToTransfer,
            wrongRubsAmount
        ))
    }
}
