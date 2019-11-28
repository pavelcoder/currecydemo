package ru.pavelcoder.modulbankdemo.model.currencyrates

import kotlinx.coroutines.*
import ru.pavelcoder.modulbankdemo.logger.Logger
import ru.pavelcoder.modulbankdemo.model.bank.Currency
import ru.pavelcoder.modulbankdemo.model.bank.CurrencyRate
import ru.pavelcoder.modulbankdemo.model.bank.exception.RateNotFoundException
import ru.pavelcoder.modulbankdemo.model.bank.exception.RatesNotReadyException
import ru.pavelcoder.modulbankdemo.model.retrofit.CurrencyRatesResponse
import ru.pavelcoder.modulbankdemo.model.retrofit.ExchangeService

/**
 * Update currency rates every REFRESH_RATE_MILLIS
 */
class CurrencyRatesFetcher(
    private val exchangeService: ExchangeService,
    private val logger: Logger
): CurrencyRatesSource, CoroutineScope {
    companion object {
        private const val REFRESH_RATE_MILLIS = 30_000L
    }

    private data class State(
        val updating: Boolean,
        val shouldUpdate: Boolean,
        val forceOffline: Boolean
    )

    private var state = State(updating = false, shouldUpdate = false, forceOffline = true)
    private val listeners = arrayListOf<CurrencyRatesListener>()
    private var currencyRates: List<CurrencyRate>? = null
    private var timerJob: Job? = null

    override val coroutineContext = Dispatchers.Main

    init {
        switchState(state.copy(shouldUpdate = true))
    }

    fun addListener(listener: CurrencyRatesListener) {
        if( listeners.contains(listener) ) throw RuntimeException("Listener already added")
        listeners.add(listener)
    }

    fun removeListener(listener: CurrencyRatesListener) {
        if( ! listeners.remove(listener) ) {
            throw RuntimeException("Listener was not added")
        }
    }

    fun reloadRatesNow() {
        switchState(state.copy(shouldUpdate = true))
    }

    fun setForceOffline(forceOffline: Boolean) {
        switchState(state.copy(forceOffline = forceOffline))
    }

    private fun switchState(newState: State) {
        if( newState == state ) return
        val oldState = state
        state = newState
        if( state.forceOffline == false && state.shouldUpdate && state.updating == false ) {
            state = state.copy(updating = true)
            cancelTimer()
            updateCurrencyRates()
        }

        val justStopUpdating = oldState.updating == true && state.updating == false
        if( justStopUpdating ) {
            setupTimerForNextUpdate()
        }

        if( oldState.forceOffline == true && state.forceOffline == false ) {
            cancelTimer()
        }
    }

    private fun updateCurrencyRates() {
        launch {
            val response = tryDownloadRates()
            try {
                currencyRates = convertResponseToRates(response)
                notifyRatesUpdated(true)
            } catch (e: RatesResponseNotConsistentException) {
                logger.log(e)
                notifyRatesUpdated(false)
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

    private fun notifyRatesUpdated(success: Boolean) {
        listeners.forEach { it.onCurrencyRatesUpdateFinished(success) }
    }

    private fun setupTimerForNextUpdate() {
        cancelTimer()
        timerJob = launch {
            delay(REFRESH_RATE_MILLIS)
            switchState(state.copy(shouldUpdate = true))
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * @return null if data is not valid
     */
    private fun convertResponseToRates(response: CurrencyRatesResponse?): List<CurrencyRate> {
        val baseCurrencyCode = response?.base ?: throw RatesResponseNotConsistentException("No base currency code")
        val baseCurrency = Currency.currencyForCode(baseCurrencyCode) ?: throw RatesResponseNotConsistentException("Unknown base currency code")
        val currencies = Currency.values().filter { it != baseCurrency }.map { currency ->
            val rate = response.rates?.get(currency.code)
                ?: throw RatesResponseNotConsistentException("Can't find currency rate for ${currency.code}")
            CurrencyRate(baseCurrency, currency, rate)
        }.toMutableList()
        currencies.add(0, CurrencyRate(baseCurrency, baseCurrency, 1.0))
        return currencies
    }

    override fun rateForCurrency(currency: Currency): CurrencyRate {
        if( currencyRates == null ) throw RatesNotReadyException()
        return currencyRates!!.find { it.target == currency }
            ?: throw RateNotFoundException(currency.toString())
    }
}