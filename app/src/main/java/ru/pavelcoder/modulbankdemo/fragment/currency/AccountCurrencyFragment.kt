package ru.pavelcoder.modulbankdemo.fragment.currency

import android.os.Bundle
import android.view.View
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import ru.pavelcoder.modulbankdemo.R


class AccountCurrencyFragment : MvpAppCompatFragment(R.layout.fragment_currency), AccountCurrencyView {
    @InjectPresenter
    internal lateinit var presenter: AccountCurrencyPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}