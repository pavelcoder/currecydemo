package ru.pavelcoder.modulbankdemo

import org.junit.Test
import org.mockito.Mockito.*
import ru.pavelcoder.modulbankdemo.model.bank.Currency
import ru.pavelcoder.modulbankdemo.model.bank.CurrencyRate
import ru.pavelcoder.modulbankdemo.model.bank.LocalBank
import ru.pavelcoder.modulbankdemo.model.bank.Transaction
import ru.pavelcoder.modulbankdemo.model.bank.exception.RatesWrongException
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesSource

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
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

    @Test
    fun sampleBankTransactionChangeAmountCorrectTest() {
        val bank = LocalBank(createRatesSource())
        val initialRubs = 100.0
        bank.setAmount(hashMapOf(
            Currency.EUR to 100.0,
            Currency.RUB to initialRubs
        ))
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
        val bank = LocalBank(createRatesSource())
        bank.setAmount(hashMapOf(
            Currency.EUR to 100.0,
            Currency.RUB to 100.0
        ))
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
