package ru.pavelcoder.modulbankdemo.model.currencyrates

import kotlinx.coroutines.*
import ru.pavelcoder.modulbankdemo.BuildConfig
import ru.pavelcoder.modulbankdemo.model.account.Currency
import ru.pavelcoder.modulbankdemo.model.account.CurrencyRate
import ru.pavelcoder.modulbankdemo.model.retrofit.CurrencyRatesResponse
import ru.pavelcoder.modulbankdemo.model.retrofit.ExchangeService
import java.lang.Exception
import java.lang.RuntimeException

/**
 * Update currency rates every REFRESH_RATE_MILLIS
 *
 * Don't update when no listeners.
 */
class CurrencyRatesFetcher(
    private val exchangeService: ExchangeService
): CoroutineScope {
    companion object {
        private const val REFRESH_RATE_MILLIS = 30_000L
    }

    private data class State(
        val updating: Boolean,
        val shouldUpdate: Boolean,
        val updateAllowed: Boolean
    )

    private var state = State(updating = false, shouldUpdate = false, updateAllowed = false)
    private val listeners = arrayListOf<CurrencyRatesListener>()
    var currencyRates: List<CurrencyRate>? = null
        private set

    override val coroutineContext = Dispatchers.Main

    init {
        switchState(state.copy(shouldUpdate = true))
    }

    fun addListener(listener: CurrencyRatesListener) {
        if( listeners.contains(listener) ) throw RuntimeException("Listener already added")
        listeners.add(listener)
        switchState(state.copy(updateAllowed = true))
    }

    fun removeListener(listener: CurrencyRatesListener) {
        if( ! listeners.remove(listener) ) {
            throw RuntimeException("Listener was not added")
        }
        if( listeners.isEmpty() ) {
            switchState(state.copy(updateAllowed = false))
        }
    }

    private fun switchState(newState: State) {
        if( newState == state ) return
        val oldState = state
        state = newState
        if( state.updateAllowed && state.shouldUpdate && state.updating == false ) {
            state = state.copy(updating = true)
            updateCurrencyRates()
        }

        if( oldState.updating == true && state.updating == false ) {
            setupTimerForNextUpdate()
            notifyRatesUpdated()
        }
    }

    private fun updateCurrencyRates() {
        launch {
            val response = tryDownloadRates()
            try {
                currencyRates = convertResponseToRates(response)
            } catch (e: RatesResponseNotConsistentException) {
                if(BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
            }
            switchState(state.copy(updating = false, shouldUpdate = false))
        }
    }

    private suspend fun tryDownloadRates(): CurrencyRatesResponse? {
        return try {
            exchangeService.currencyRates().body()
        } catch (e: Exception) {
            null
        }
    }

    private fun notifyRatesUpdated() {
        listeners.forEach { it.onNewCurrencyRates() }
    }

    private fun setupTimerForNextUpdate() {
        launch {
            delay(REFRESH_RATE_MILLIS)
            switchState(state.copy(shouldUpdate = true))
        }
    }

    /**
     * @return null if data is not valid
     */
    @Throws(RatesResponseNotConsistentException::class)
    private fun convertResponseToRates(response: CurrencyRatesResponse?): List<CurrencyRate> {
        val baseCurrencyCode = response?.base ?: throw RatesResponseNotConsistentException("No base currency code")
        val baseCurrency = Currency.currencyForCode(baseCurrencyCode) ?: throw RatesResponseNotConsistentException("Unknown base currency code")
        return Currency.values().filter { it != baseCurrency }.map { currency ->
            val rate = response.rates?.get(currency.code) ?: throw RatesResponseNotConsistentException("Can't find currency rate for ${currency.code}")
            CurrencyRate(baseCurrency, currency, rate)
        }
    }
}