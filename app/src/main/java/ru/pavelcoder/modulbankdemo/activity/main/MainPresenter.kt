package ru.pavelcoder.modulbankdemo.activity.main

import android.content.Context
import moxy.InjectViewState
import moxy.MvpPresenter
import ru.pavelcoder.modulbankdemo.R
import ru.pavelcoder.modulbankdemo.dagger.DaggerHolder
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentIdentifier
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenter
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenterProvider
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
class MainPresenter : MvpPresenter<MainActivityView>(), CurrencyRatesListener,
    CurrencyFragmentPresenterProvider {

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

    private var bank: Bank? = null
    private lateinit var availableCurrencies: List<Currency>

    private lateinit var selectedSourceCurrency: Currency
    private lateinit var selectedDestinationCurrency: Currency
    private var sourceAmount = 0L
    private var destinationAmount = 0L

    init {
        DaggerHolder.getDagger().inject(this)
        viewState.setState(MainViewState.LOADING)
        viewState.setExchangeButtonVisible(false)
        currencyRates.addListener(this)
    }

    override fun onCurrencyRatesUpdateFinished(success: Boolean) {
        if( bank != null ) return
        if (success) {
            bank = LocalBankFactory.createBank(currencyRates)
            availableCurrencies = bank!!.getAvailableCurrencies().sortedBy { it.code }
            selectedDestinationCurrency = availableCurrencies[DEFAULT_SELECTED_CURRENCY_INDEX]
            selectedSourceCurrency = availableCurrencies[DEFAULT_SELECTED_CURRENCY_INDEX]
            showExchangeView()
        }
        else {
            viewState.setState(MainViewState.ERROR)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currencyRates.removeListener(this)
    }

    private fun showExchangeView() {
        viewState.setState(MainViewState.CURRENCY_EXCHANGE)
        showSourceCurrencies()
        showDestinationCurrencies()
        showZeroTransaction()
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

    private fun showZeroTransaction() {
        sourceAmount = 0
        destinationAmount = 0
        updateSourceAmount()
        updateSourceRates()
        updateSourceAmountLeft()
        updateDestinationAmount()
        updateDestinationAmountLeft()
    }

    private fun firstSetupCurrencyPresenter(identifier: CurrencyFragmentIdentifier, amountPrefix: String) {
        val presenter = providePresenter(identifier)
        val currency = availableCurrencies[identifier.position]
        presenter.setCurrency(currency.code)
        presenter.setAmountPrefix(amountPrefix)
        val available = bank!!.getAvailableFunds(currency)
        presenter.setAvailableAmount(available / 100f, currency.symbol)
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

    fun onExchangeClick() {
        try {
            val transaction = Transaction(selectedSourceCurrency, selectedDestinationCurrency, sourceAmount, destinationAmount)
            bank!!.executeTransaction(transaction)
            sourceAmount = 0
            destinationAmount = 0
            updateSourceAmount()
            updateSourceAmountLeft()
            updateDestinationAmount()
            updateDestinationAmountLeft()
        } catch (e: ZeroAmountTransactionException) {
            viewState.showErrorAlert(applicationContext.getString(R.string.zero_transaction_error))
            logger.log(e)
        } catch (e: SameCurrencyTransactionException) {
            viewState.showErrorAlert(applicationContext.getString(R.string.same_currency_transaction_error))
            logger.log(e)
        } catch (e: NotEnoughFundsException) {
            showNotEnoughFundsError()
            logger.log(e)
        } catch (e: RatesWrongException) {
            viewState.showErrorAlert(applicationContext.getString(R.string.currency_rates_wrong))
            logger.log(e)
        }
    }

    private fun showNotEnoughFundsError() {
        val available = bank!!.getAvailableFunds(selectedSourceCurrency)
        val message = applicationContext.getString(
            R.string.not_enough_money,
            sourceAmount / 100f,
            selectedSourceCurrency.code,
            available / 100f,
            selectedSourceCurrency.code
        )
        viewState.showErrorAlert(message)
    }

    fun onAmountChanged(fragmentIdentifier: CurrencyFragmentIdentifier, amount: Float) {
        val amountInCents = (amount * 100).toLong()
        when (fragmentIdentifier.type) {
            CurrencyFragmentType.SOURCE -> {
                sourceAmount = amountInCents
                updateDestinationAmount()
            }
            CurrencyFragmentType.DESTINATION -> {
                destinationAmount = amountInCents
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
            val transaction = bank!!.prepareTransactionWithActualRates(transactionRequest)
            sourceAmount = transaction.sourceAmount
            srcPresenter.setAmount(transaction.sourceAmount / 100f)
        } catch (e: Exception) {
            logger.log(e)
            srcPresenter.setAmount(0f)
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
            val transaction = bank!!.prepareTransactionWithActualRates(transactionRequest)
            destinationAmount = transaction.destinationAmount
            dstPresenter.setAmount(transaction.destinationAmount / 100f)
        } catch (e: Exception) {
            logger.log(e)
            dstPresenter.setAmount(0f)
        }
    }

    private fun updateSourceRates() {
        val srcPresenter = getActiveSourceCurrencyPresenter()
        val rate = bank!!.getConversionRate(selectedSourceCurrency, selectedDestinationCurrency)
        srcPresenter.setRate(
            1f,
            selectedSourceCurrency.symbol,
            rate,
            selectedDestinationCurrency.symbol
        )
    }

    private fun updateDestinationRates() {
        val dstPresenter = getActiveDestinationCurreucyPresenter()
        val rate = bank!!.getConversionRate(selectedDestinationCurrency, selectedSourceCurrency)
        dstPresenter.setRate(
            1f,
            selectedDestinationCurrency.symbol,
            rate,
            selectedSourceCurrency.symbol
        )
    }


    private fun updateSourceAmountLeft() {
        val amount = bank!!.getAvailableFunds(selectedSourceCurrency)
        val srcPresenter = getActiveSourceCurrencyPresenter()
        srcPresenter.setAvailableAmount(amount / 100f, selectedSourceCurrency.symbol)
    }

    private fun updateDestinationAmountLeft() {
        val amount = bank!!.getAvailableFunds(selectedDestinationCurrency)
        val dstPresenter = getActiveDestinationCurreucyPresenter()
        dstPresenter.setAvailableAmount(amount / 100f, selectedDestinationCurrency.symbol)
    }

    private fun getActiveSourceCurrencyPresenter(): CurrencyFragmentPresenter {
        val sourceCurrencyIndex = availableCurrencies.indexOf(selectedSourceCurrency)
        val sourcePresenterIdentifier = CurrencyFragmentIdentifier(CurrencyFragmentType.SOURCE, sourceCurrencyIndex)
        return providePresenter(sourcePresenterIdentifier)
    }

    private fun getActiveDestinationCurreucyPresenter(): CurrencyFragmentPresenter {
        val sourceCurrencyIndex = availableCurrencies.indexOf(selectedDestinationCurrency)
        val sourcePresenterIdentifier = CurrencyFragmentIdentifier(CurrencyFragmentType.DESTINATION, sourceCurrencyIndex)
        return providePresenter(sourcePresenterIdentifier)
    }

    fun onReloadClick() {
        viewState.setState(MainViewState.LOADING)
        currencyRates.reloadRatesNow()
    }

    //--------------
    private val currencyPresenters = HashMap<CurrencyFragmentIdentifier, CurrencyFragmentPresenter>()
    override fun providePresenter(identifier: CurrencyFragmentIdentifier): CurrencyFragmentPresenter {
        return currencyPresenters[identifier]
            ?: CurrencyFragmentPresenter(identifier, this).apply {
                currencyPresenters[identifier] = this
            }
    }
}