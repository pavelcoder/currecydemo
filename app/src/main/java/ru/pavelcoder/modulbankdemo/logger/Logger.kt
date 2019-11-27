package ru.pavelcoder.modulbankdemo.logger

interface Logger {
    fun log(str: String)
    fun log(e: Exception)
}