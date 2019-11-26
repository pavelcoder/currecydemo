package ru.pavelcoder.modulbankdemo.model.bank.exception

import java.lang.Exception

class NotEnoughFundsException(message: String?) : Exception(message)