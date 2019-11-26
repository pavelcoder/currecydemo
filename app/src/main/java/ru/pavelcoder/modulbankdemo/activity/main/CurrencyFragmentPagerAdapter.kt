package ru.pavelcoder.modulbankdemo.activity.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragment
import ru.pavelcoder.modulbankdemo.fragment.currency.CurrencyFragmentIdentifier

class CurrencyFragmentPagerAdapter(
    private val identifiers: List<CurrencyFragmentIdentifier>,
    fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        val identifier = identifiers[position]
        return CurrencyFragment.instance(identifier)
    }

    override fun getCount() = identifiers.size
}