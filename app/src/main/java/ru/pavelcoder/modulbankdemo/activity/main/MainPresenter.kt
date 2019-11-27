package ru.pavelcoder.modulbankdemo.activity.main

import moxy.InjectViewState
import moxy.MvpPresenter
import ru.pavelcoder.modulbankdemo.BuildConfig
import ru.pavelcoder.modulbankdemo.dagger.DaggerHolder
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentIdentifier
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenter
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenterProvider
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentType
import ru.pavelcoder.modulbankdemo.model.bank.*
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesFetcher
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesListener
import java.lang.Exception
import javax.inject.Inject

@InjectViewState
class MainPresenter : MvpPresenter<MainActivityView>(), CurrencyRatesListener,
    CurrencyFragmentPresenterProvider {

    companion object {
        private const val sourcePrefix = "-"
        private const val destinationPrefix = "+"
        private const val DEFAULT_SELECTED_CURRENCY_INDEX = 0
    }

    @Inject
    internal lateinit var currencyRates: CurrencyRatesFetcher

    private var bank: Bank? = null
    private lateinit var availableCurrencies: List<Currency>

    private lateinit var selectedSourceCurrency: Currency
    private lateinit var selectedDestinationCurrency: Currency
    private var sourceAmount = 0L
    private var destinationAmount = 0L

    init {
        DaggerHolder.getDagger().inject(this)
        viewState.setState(MainViewState.LOADING)
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
        prepareAndShowZeroTransaction()
    }

    private fun showSourceCurrencies() {
        val currencyCount = availableCurrencies.size
        val sourceIdentifiers = (0 until currencyCount).map {
            CurrencyFragmentIdentifier(CurrencyFragmentType.SOURCE, it)
        }
        viewState.showSourceCurrencies(sourceIdentifiers)
        viewState.setSelectedSourceCurrency(DEFAULT_SELECTED_CURRENCY_INDEX)
        sourceIdentifiers.forEach { identifier -> firstSetupCurrencyPresenter(identifier, sourcePrefix) }
    }

    private fun showDestinationCurrencies() {
        val currencyCount = availableCurrencies.size
        val destinationIdentifiers = (0 until currencyCount).map {
            CurrencyFragmentIdentifier(CurrencyFragmentType.DESTINATION, it)
        }
        viewState.showDestinationCurrencies(destinationIdentifiers)

        viewState.setSelectedDestinationCurrency(DEFAULT_SELECTED_CURRENCY_INDEX)
        destinationIdentifiers.forEach { identifier -> firstSetupCurrencyPresenter(identifier, destinationPrefix) }
    }

    private fun prepareAndShowZeroTransaction() {
        val firstCurrency = availableCurrencies.first()
        val transactionRequest = TransactionRequest(firstCurrency, firstCurrency, 0, TransactionRequest.TransactionCalcFrom.Source)
        prepareTransactionRequestAndShow(transactionRequest)
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
        updateSourceAmount(true)
    }

    fun onDestinationSelectionChanged(position: Int) {
        selectedDestinationCurrency = availableCurrencies[position]
        viewState.setSelectedDestinationCurrency(position)
        updateDestinationAmount(true)
    }

    fun onExchangeClick() {

    }

    fun onAmountChanged(fragmentIdentifier: CurrencyFragmentIdentifier, amount: Float) {
        val amountInCents = (amount * 100).toLong()
        when (fragmentIdentifier.type) {
            CurrencyFragmentType.SOURCE -> {
                sourceAmount = amountInCents
                updateDestinationAmount(false)
            }
            CurrencyFragmentType.DESTINATION -> {
                destinationAmount = amountInCents
                updateSourceAmount(false)
            }
        }
    }

    private fun updateSourceAmount(withRates: Boolean) {
        val transactionRequest = TransactionRequest(
            selectedSourceCurrency,
            selectedDestinationCurrency,
            destinationAmount,
            TransactionRequest.TransactionCalcFrom.Destination
        )
        val srcPresenter = getActiveSourceCurreucyPresenter()
        try {
            val transaction = bank!!.prepareTransactionWithActualRates(transactionRequest)
            sourceAmount = transaction.sourceAmount
            srcPresenter.setAmount(transaction.sourceAmount / 100f)
            if( withRates ) {
                srcPresenter.setRate(
                    1f,
                    transaction.sourceCurrency.symbol,
                    transaction.sourceToDestinationRate,
                    transaction.destinationCurrency.symbol
                )
            }
        } catch (e: Exception) {
            srcPresenter.setAmount(0f)
        }
    }

    private fun updateDestinationAmount(withRates: Boolean) {
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
            if( withRates ) {
                dstPresenter.setRate(
                    1f,
                    transaction.destinationCurrency.symbol,
                    1 / transaction.sourceToDestinationRate,
                    transaction.sourceCurrency.symbol
                )
            }
        } catch (e: Exception) {
            dstPresenter.setAmount(0f)
        }
    }

    private fun getActiveSourceCurreucyPresenter(): CurrencyFragmentPresenter {
        val sourceCurrencyIndex = availableCurrencies.indexOf(selectedSourceCurrency)
        val sourcePresenterIdentifier = CurrencyFragmentIdentifier(CurrencyFragmentType.SOURCE, sourceCurrencyIndex)
        return providePresenter(sourcePresenterIdentifier)
    }

    private fun getActiveDestinationCurreucyPresenter(): CurrencyFragmentPresenter {
        val sourceCurrencyIndex = availableCurrencies.indexOf(selectedDestinationCurrency)
        val sourcePresenterIdentifier = CurrencyFragmentIdentifier(CurrencyFragmentType.DESTINATION, sourceCurrencyIndex)
        return providePresenter(sourcePresenterIdentifier)
    }

    private fun prepareTransactionRequestAndShow(transactionRequest: TransactionRequest) {
        try {
            val preparedTransaction = bank!!.prepareTransactionWithActualRates(transactionRequest)
            showPreparedTransaction(preparedTransaction)
        } catch (e: Exception) {
            if( BuildConfig.DEBUG ) {
                e.printStackTrace()
            }
        }
    }

    private fun showPreparedTransaction(transaction: Transaction) {
        val srcPresenter = getActiveSourceCurreucyPresenter()
        srcPresenter.setAmount(transaction.sourceAmount / 100f)

        srcPresenter.setRate(
            1f,
            transaction.sourceCurrency.symbol,
            transaction.sourceToDestinationRate,
            transaction.destinationCurrency.symbol
        )

        val destCurrencyIndex = availableCurrencies.indexOf(transaction.destinationCurrency)
        val destPresenterIdentifier = CurrencyFragmentIdentifier(CurrencyFragmentType.DESTINATION, destCurrencyIndex)
        val destPresenter = providePresenter(destPresenterIdentifier)
        destPresenter.setAmount(transaction.destinationAmount / 100f)

        destPresenter.setRate(
            1f,
            transaction.destinationCurrency.symbol,
            1 / transaction.sourceToDestinationRate,
            transaction.sourceCurrency.symbol
        )
    }

    fun onReloadClick() {
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