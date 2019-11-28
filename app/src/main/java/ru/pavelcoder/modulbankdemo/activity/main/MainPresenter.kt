package ru.pavelcoder.modulbankdemo.activity.main

import android.content.Context
import moxy.InjectViewState
import moxy.MvpPresenter
import ru.pavelcoder.modulbankdemo.R
import ru.pavelcoder.modulbankdemo.dagger.DaggerHolder
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentIdentifier
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenter
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentType
import ru.pavelcoder.modulbankdemo.logger.Logger
import ru.pavelcoder.modulbankdemo.model.bank.*
import ru.pavelcoder.modulbankdemo.model.bank.exception.NotEnoughFundsException
import ru.pavelcoder.modulbankdemo.model.bank.exception.RatesWrongException
import ru.pavelcoder.modulbankdemo.model.bank.exception.SameCurrencyTransactionException
import ru.pavelcoder.modulbankdemo.model.bank.exception.ZeroAmountTransactionException
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesFetcher
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesListener
import javax.inject.Inject

@InjectViewState
class MainPresenter : MvpPresenter<MainActivityView>(), CurrencyRatesListener {

    companion object {
        private const val SOURCE_PREFIX = "-"
        private const val DESTINATION_PREFIX = "+"
        private const val DEFAULT_SELECTED_CURRENCY_INDEX = 0
    }

    @Inject
    internal lateinit var applicationContext: Context
    @Inject
    internal lateinit var currencyRates: CurrencyRatesFetcher
    @Inject
    internal lateinit var logger: Logger
    @Inject
    internal lateinit var bank: Bank

    private lateinit var availableCurrencies: List<Currency>

    private lateinit var selectedSourceCurrency: Currency
    private lateinit var selectedDestinationCurrency: Currency
    private var sourceAmount = 0.0
    private var destinationAmount = 0.0
    private var bankReady = false

    val childPresenterHolder = CurrencyFragmentPresentersHolder(this)

    init {
        DaggerHolder.getDagger().inject(this)
        viewState.setState(MainViewState.LOADING)
        viewState.setExchangeButtonVisible(false)
        currencyRates.addListener(this)
    }

    fun onActivityResume() {
        currencyRates.setForceOffline(false)
        currencyRates.reloadRatesNow()
    }

    fun onActivityPause() {
        currencyRates.setForceOffline(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        currencyRates.removeListener(this)
    }

    override fun onCurrencyRatesUpdateFinished(success: Boolean) {
        if( bankReady && success ) {
            viewState.showToast(applicationContext.getString(R.string.rates_updated))
            updateDestinationAmount()
            updateSourceRates()
            updateDestinationRates()
        } else if (success) {
            bankReady = true
            availableCurrencies = bank.getAvailableCurrencies().sortedBy { it.code }
            selectedDestinationCurrency = availableCurrencies[DEFAULT_SELECTED_CURRENCY_INDEX]
            selectedSourceCurrency = availableCurrencies[DEFAULT_SELECTED_CURRENCY_INDEX]
            showExchangeView()
        }
        else {
            viewState.setState(MainViewState.ERROR)
        }
    }

    private fun showExchangeView() {
        viewState.setState(MainViewState.CURRENCY_EXCHANGE)
        showSourceCurrencies()
        showDestinationCurrencies()
        updateSourceAmount()
        updateSourceRates()
        updateSourceAmountLeft()
        updateDestinationAmount()
        updateDestinationRates()
        updateDestinationAmountLeft()
        viewState.setExchangeButtonVisible(true)
    }

    private fun showSourceCurrencies() {
        val currencyCount = availableCurrencies.size
        val sourceIdentifiers = (0 until currencyCount).map {
            CurrencyFragmentIdentifier(CurrencyFragmentType.SOURCE, it)
        }
        viewState.showSourceCurrencies(sourceIdentifiers)
        viewState.setSelectedSourceCurrency(DEFAULT_SELECTED_CURRENCY_INDEX)
        sourceIdentifiers.forEach { identifier -> firstSetupCurrencyPresenter(identifier, SOURCE_PREFIX) }
    }

    private fun showDestinationCurrencies() {
        val currencyCount = availableCurrencies.size
        val destinationIdentifiers = (0 until currencyCount).map {
            CurrencyFragmentIdentifier(CurrencyFragmentType.DESTINATION, it)
        }
        viewState.showDestinationCurrencies(destinationIdentifiers)
        viewState.setSelectedDestinationCurrency(DEFAULT_SELECTED_CURRENCY_INDEX)
        destinationIdentifiers.forEach { identifier -> firstSetupCurrencyPresenter(identifier, DESTINATION_PREFIX) }
    }

    private fun firstSetupCurrencyPresenter(identifier: CurrencyFragmentIdentifier, amountPrefix: String) {
        val presenter = childPresenterHolder.providePresenter(identifier)
        val currency = availableCurrencies[identifier.position]
        presenter.setCurrency(currency.code)
        presenter.setAmountPrefix(amountPrefix)
        val available = bank.getAvailableFunds(currency)
        presenter.setAvailableAmount(available, currency.symbol)
    }

    fun onSourceSelectionChanged(position: Int) {
        selectedSourceCurrency = availableCurrencies[position]
        viewState.setSelectedSourceCurrency(position)
        updateSourceAmount()
        updateSourceRates()
        updateSourceAmountLeft()
        updateDestinationRates()
    }

    fun onDestinationSelectionChanged(position: Int) {
        selectedDestinationCurrency = availableCurrencies[position]
        viewState.setSelectedDestinationCurrency(position)
        updateDestinationAmount()
        updateDestinationAmountLeft()
        updateSourceRates()
        updateDestinationRates()
    }

    fun onClearClick() {
        sourceAmount = 0.0
        destinationAmount = 0.0
        updateSourceAmount()
        updateDestinationAmount()
    }

    fun onExchangeClick() {
        try {
            val transaction = Transaction(selectedSourceCurrency, selectedDestinationCurrency, sourceAmount, destinationAmount)
            bank.executeTransaction(transaction)
            val successMessage = applicationContext.getString(
                R.string.exchange_success,
                sourceAmount,
                selectedSourceCurrency.code,
                destinationAmount,
                selectedDestinationCurrency.code
            )
            viewState.showAlert(successMessage)
            sourceAmount = 0.0
            destinationAmount = 0.0
            updateSourceAmount()
            updateSourceAmountLeft()
            updateDestinationAmount()
            updateDestinationAmountLeft()
        } catch (e: ZeroAmountTransactionException) {
            viewState.showAlert(applicationContext.getString(R.string.zero_transaction_error))
            logger.log(e)
        } catch (e: SameCurrencyTransactionException) {
            viewState.showAlert(applicationContext.getString(R.string.same_currency_transaction_error))
            logger.log(e)
        } catch (e: NotEnoughFundsException) {
            showNotEnoughFundsError()
            logger.log(e)
        } catch (e: RatesWrongException) {
            viewState.showAlert(applicationContext.getString(R.string.currency_rates_wrong))
            logger.log(e)
        }
    }

    private fun showNotEnoughFundsError() {
        val available = bank.getAvailableFunds(selectedSourceCurrency)
        val message = applicationContext.getString(
            R.string.not_enough_money,
            sourceAmount,
            selectedSourceCurrency.code,
            available,
            selectedSourceCurrency.code
        )
        viewState.showAlert(message)
    }

    fun onAmountChanged(fragmentIdentifier: CurrencyFragmentIdentifier, amount: Double) {
        when (fragmentIdentifier.type) {
            CurrencyFragmentType.SOURCE -> {
                sourceAmount = amount
                updateDestinationAmount()
            }
            CurrencyFragmentType.DESTINATION -> {
                destinationAmount = amount
                updateSourceAmount()
            }
        }
    }

    private fun updateSourceAmount() {
        val transactionRequest = TransactionRequest(
            selectedSourceCurrency,
            selectedDestinationCurrency,
            destinationAmount,
            TransactionRequest.TransactionCalcFrom.Destination
        )
        val srcPresenter = getActiveSourceCurrencyPresenter()
        try {
            val transaction = bank.prepareTransactionWithActualRates(transactionRequest)
            sourceAmount = transaction.sourceAmount
            srcPresenter.setAmount(transaction.sourceAmount)
        } catch (e: Exception) {
            logger.log(e)
            srcPresenter.setAmount(0.0)
        }
    }

    private fun updateDestinationAmount() {
        val transactionRequest = TransactionRequest(
            selectedSourceCurrency,
            selectedDestinationCurrency,
            sourceAmount,
            TransactionRequest.TransactionCalcFrom.Source
        )
        val dstPresenter = getActiveDestinationCurreucyPresenter()
        try {
            val transaction = bank.prepareTransactionWithActualRates(transactionRequest)
            destinationAmount = transaction.destinationAmount
            dstPresenter.setAmount(transaction.destinationAmount)
        } catch (e: Exception) {
            logger.log(e)
            dstPresenter.setAmount(0.0)
        }
    }

    private fun updateSourceRates() {
        val srcPresenter = getActiveSourceCurrencyPresenter()
        val rate = bank.getConversionRate(selectedSourceCurrency, selectedDestinationCurrency)
        srcPresenter.setRate(
            1.0,
            selectedSourceCurrency.symbol,
            rate,
            selectedDestinationCurrency.symbol
        )
    }

    private fun updateDestinationRates() {
        val dstPresenter = getActiveDestinationCurreucyPresenter()
        val rate = bank.getConversionRate(selectedDestinationCurrency, selectedSourceCurrency)
        dstPresenter.setRate(
            1.0,
            selectedDestinationCurrency.symbol,
            rate,
            selectedSourceCurrency.symbol
        )
    }

    private fun updateSourceAmountLeft() {
        val amount = bank.getAvailableFunds(selectedSourceCurrency)
        val srcPresenter = getActiveSourceCurrencyPresenter()
        srcPresenter.setAvailableAmount(amount, selectedSourceCurrency.symbol)
    }

    private fun updateDestinationAmountLeft() {
        val amount = bank.getAvailableFunds(selectedDestinationCurrency)
        val dstPresenter = getActiveDestinationCurreucyPresenter()
        dstPresenter.setAvailableAmount(amount, selectedDestinationCurrency.symbol)
    }

    private fun getActiveSourceCurrencyPresenter(): CurrencyFragmentPresenter {
        val sourceCurrencyIndex = availableCurrencies.indexOf(selectedSourceCurrency)
        val sourcePresenterIdentifier = CurrencyFragmentIdentifier(CurrencyFragmentType.SOURCE, sourceCurrencyIndex)
        return childPresenterHolder.providePresenter(sourcePresenterIdentifier)
    }

    private fun getActiveDestinationCurreucyPresenter(): CurrencyFragmentPresenter {
        val sourceCurrencyIndex = availableCurrencies.indexOf(selectedDestinationCurrency)
        val sourcePresenterIdentifier = CurrencyFragmentIdentifier(CurrencyFragmentType.DESTINATION, sourceCurrencyIndex)
        return childPresenterHolder.providePresenter(sourcePresenterIdentifier)
    }

    fun onReloadClick() {
        viewState.setState(MainViewState.LOADING)
        currencyRates.reloadRatesNow()
    }
}