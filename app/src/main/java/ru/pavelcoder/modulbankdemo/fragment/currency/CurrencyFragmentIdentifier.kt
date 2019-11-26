package ru.pavelcoder.modulbankdemo.fragment.currency

import java.io.Serializable

data class CurrencyFragmentIdentifier(
    val type: CurrencyFragmentType,
    val position: Int
): Serializable