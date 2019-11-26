package ru.pavelcoder.modulbankdemo.activity.main

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_main.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import ru.pavelcoder.modulbankdemo.R
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentIdentifier
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenter
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentPresenterProvider
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentType

class MainActivity : MvpAppCompatActivity(), MainActivityView, CurrencyFragmentPresenterProvider {

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
        amSourcePager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int,positionOffset: Float,positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                presenter.onDestinationSelectionChanged(position)
            }
        })
        val pageMargin = resources.getDimension(R.dimen.viewpager_page_margin).toInt()
        amSourcePager.pageMargin = pageMargin
        amDestinationPager.pageMargin = pageMargin
    }

    override fun setLoading(loading: Boolean) {
        amProgress.visibility = if( loading ) View.VISIBLE else View.GONE
        amContent.visibility = if( loading ) View.GONE else View.VISIBLE
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
}
