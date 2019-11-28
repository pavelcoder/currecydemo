package ru.pavelcoder.modulbankdemo.activity.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_main.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import ru.pavelcoder.modulbankdemo.R
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentIdentifier
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenter
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenterProvider

class MainActivity : MvpAppCompatActivity(), MainActivityView, CurrencyFragmentPresenterProvider {

    private var hasMenu = false

    @InjectPresenter
    internal lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        amSourcePager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int,positionOffset: Float,positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                presenter.onSourceSelectionChanged(position)
            }
        })
        amDestinationPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int,positionOffset: Float,positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                presenter.onDestinationSelectionChanged(position)
            }
        })
        val pageMargin = resources.getDimension(R.dimen.viewpager_page_margin).toInt()
        amSourcePager.pageMargin = pageMargin
        amDestinationPager.pageMargin = pageMargin
        amReload.setOnClickListener {
            presenter.onReloadClick()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if( hasMenu ) {
            val inflater = menuInflater
            inflater.inflate(R.menu.exchange_menu, menu)
            true
        } else super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.exchange -> {
                presenter.onExchangeClick()
                true
            }
            R.id.clear -> {
                presenter.onClearClick()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun setState(state: MainViewState) {
        amProgress.visibility = if(state == MainViewState.LOADING) View.VISIBLE else View.GONE
        amContent.visibility = if(state == MainViewState.CURRENCY_EXCHANGE) View.VISIBLE else View.GONE
        amError.visibility = if(state == MainViewState.ERROR) View.VISIBLE else View.GONE
    }

    override fun showSourceCurrencies(sourceIdentifiers: List<CurrencyFragmentIdentifier>) {
        amSourcePager.adapter = CurrencyFragmentPagerAdapter( sourceIdentifiers, supportFragmentManager )
    }

    override fun showDestinationCurrencies(destinationIdentifiers: List<CurrencyFragmentIdentifier>) {
        amDestinationPager.adapter = CurrencyFragmentPagerAdapter( destinationIdentifiers, supportFragmentManager )
    }

    override fun providePresenter(identifier: CurrencyFragmentIdentifier): CurrencyFragmentPresenter {
        return presenter.providePresenter(identifier)
    }

    override fun setToolbarTitle(title: String) {
        super.setTitle(title)
    }

    override fun setSelectedSourceCurrency(position: Int) {
        amSourcePager.setCurrentItem(position, false)
    }

    override fun setSelectedDestinationCurrency(position: Int) {
        amDestinationPager.setCurrentItem(position, false)
    }

    override fun showAlert(text: String) {
        AlertDialog.Builder(this)
            .setMessage(text)
            .setPositiveButton(R.string.Close) { _, _ ->}
            .show()
    }

    override fun setExchangeButtonVisible(visible: Boolean) {
        hasMenu = visible
        invalidateOptionsMenu()
    }
}
