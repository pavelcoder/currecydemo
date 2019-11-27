package ru.pavelcoder.modulbankdemo.ui.view

import android.content.Context
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText

class PrefixedEditText(context: Context?, attrs: AttributeSet?) : EditText (context, attrs) {
    var prefix: String = ""
        set(value) {
            field = value
            updatePrefixForText()
        }

    var onTextChanged: ((String) -> Unit)? = null

    private val textWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence,start: Int,before: Int,count: Int) {}
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,after: Int) {}

        override fun afterTextChanged(editable: Editable) {
            updatePrefixForText()
            onTextChanged?.invoke( textWithoutPrefix() )
        }
    }

    init {
        addTextChangedListener(textWatcher)
    }

    fun setTextWithoutCallbacks(text: String) {
        removeTextChangedListener(textWatcher)
        setText(text)
        addTextChangedListener(textWatcher)
    }

    private fun updatePrefixForText() {
        val textString = text.toString()
        when {
            prefix == textString || textString.isEmpty() -> {
                setTextWithoutCallbacks("")
            }
            textString.startsWith(prefix) == false -> {
                setTextWithoutCallbacks(prefix + textString)
                Selection.setSelection(text, text.length)
            }
        }
    }

    private fun textWithoutPrefix(): String {
        return if( text.startsWith(prefix) ) {
            text.substring(prefix.length)
        } else {
            text.toString()
        }

    }
}