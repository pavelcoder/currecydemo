package ru.pavelcoder.modulbankdemo.model.bank.exception

import java.lang.Exception

class NoSuchCurrencyException(message: String?) : Exception(message)