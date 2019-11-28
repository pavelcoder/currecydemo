package ru.pavelcoder.modulbankdemo.model.db

import io.realm.Realm
import ru.pavelcoder.modulbankdemo.model.bank.Currency

class MoneyRealmRepository(
    private val realm: Realm
) : MoneyRepository {
    override fun saveAmount(currency: Currency, amount: Double) {
        realm.executeTransactionAsync {
            val record = MoneyRecord()
            record.amount = amount
            record.currency = currency.code
            it.copyToRealmOrUpdate(record)
        }
    }

    override fun getAmount(currency: Currency): Double {
        val currencyRecord = realm
            .where(MoneyRecord::class.java)
            .equalTo("currency", currency.code)
            .findFirst()
        return currencyRecord?.amount ?: throw RateNotFoundException("Record for $currency not found")
    }
}