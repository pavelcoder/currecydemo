package ru.pavelcoder.modulbankdemo.model.db

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class MoneyRecord : RealmObject() {
    @Required
    @PrimaryKey
    var currency: String? = null
    @Required
    var amount: Double? = null
}