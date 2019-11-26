package ru.pavelcoder.modulbankdemo.activity.main

import moxy.InjectViewState
import moxy.MvpPresenter
import ru.pavelcoder.modulbankdemo.dagger.DaggerHolder
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentIdentifier
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenter
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenterProvider
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentType
import ru.pavelcoder.modulbankdemo.model.bank.Bank
import ru.pavelcoder.modulbankdemo.model.bank.Currency
import ru.pavelcoder.modulbankdemo.model.bank.LocalBankFactory
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesFetcher
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesListener
import javax.inject.Inject

@InjectViewState
class MainPresenter : MvpPresenter<MainActivityView>(), CurrencyRatesListener,
    CurrencyFragmentPresenterProvider {

    companion object {
        private const val sourcePrefix = "-"
        private const val destinationPrefix = "+"
    }

    @Inject
    internal lateinit var currencyRates: CurrencyRatesFetcher

    private var bank: Bank? = null
    private var availableCurrencies: List<Currency>? = null
    private var selectedSourceCurrency: Currency? = null
    private var selectedDestinationCurrency: Currency? = null

    init {
        DaggerHolder.getDagger().inject(this)
        viewState.setLoading(true)
        currencyRates.addListener(this)
    }

    override fun onNewCurrencyRates() {
        if( bank == null ) {
            bank = LocalBankFactory.createBank(currencyRates)
            showExchangeView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currencyRates.removeListener(this)
    }

    private fun showExchangeView() {
        viewState.setLoading(false)
        availableCurrencies = bank!!.getAvailableCurrencies().sortedBy { it.code }
        val currencyCount = availableCurrencies!!.size

        val sourceIdentifiers = (0 until currencyCount).map { CurrencyFragmentIdentifier(CurrencyFragmentType.SOURCE, it) }
        viewState.showSourceCurrencies(sourceIdentifiers)

        val destinationIdentifiers = (0 until currencyCount).map {
            CurrencyFragmentIdentifier(CurrencyFragmentType.DESTINATION, it)
        }
        viewState.showDestinationCurrencies(destinationIdentifiers)

        for( identifier in sourceIdentifiers ) {
            val presenter = providePresenter(identifier)
            val currency = availableCurrencies!![identifier.position]
            presenter.setCurrency(currency.code)
            presenter.setAmount(0f)
            presenter.setAmountPrefix(sourcePrefix)
            presenter.setRate(1f, currency.symbol, 1f, currency.symbol)

            val available = bank!!.getAvailableFunds(currency)
            presenter.setAvailableAmount(available / 100f, currency.symbol)
        }

        for( identifier in destinationIdentifiers ) {
            val presenter = providePresenter(identifier)
            val currency = availableCurrencies!![identifier.position]
            presenter.setCurrency(currency.code)
            presenter.setAmount(0f)
            presenter.setAmountPrefix(destinationPrefix)
            presenter.setRate(1f, currency.symbol, 1f, currency.symbol)

            val available = bank!!.getAvailableFunds(currency)
            presenter.setAvailableAmount(available / 100f, currency.symbol)
        }
    }

    fun onSourceSelectionChanged(position: Int) {
        selectedSourceCurrency = availableCurrencies?.getOrNull(position)
    }

    fun onDestinationSelectionChanged(position: Int) {
        selectedDestinationCurrency = availableCurrencies?.getOrNull(position)
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