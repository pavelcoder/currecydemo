package ru.pavelcoder.modulbankdemo.ui.view

import android.content.Context
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

/**
 * Cursor not positioning at right corner, when hint is specified.
 */
class PrefixedEditText(context: Context?, attrs: AttributeSet?) : AppCompatEditText (context, attrs) {
    var prefix: String = ""
        set(value) {
            field = value
            updatePrefixForText()
        }

    var onTextChanged: ((String) -> Unit)? = null
    private var textChangedCallbackAllowed = true

    init {
        //prevent from restore state and call text changed listener
        isSaveEnabled = false
    }

    private val textWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence,start: Int,before: Int,count: Int) {}
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,after: Int) {}

        override fun afterTextChanged(editable: Editable) {
            if( textChangedCallbackAllowed ) {
                updatePrefixForText()
                onTextChanged?.invoke(textWithoutPrefix())
            }
        }
    }

    init {
        addTextChangedListener(textWatcher)
    }

    fun setTextWithoutCallbacks(text: String) {
        if( textWithoutPrefix() == text ) return
        textChangedCallbackAllowed = false
        setText(text)
        updatePrefixForText()
        textChangedCallbackAllowed = true
    }

    private fun updatePrefixForText() {
        val textString = text.toString()
        when {
            prefix == textString || textString.isEmpty() -> {
                setTextWithoutCallbacks("")
            }
            textString.startsWith(prefix) == false -> {
                setTextWithoutCallbacks(prefix + textString)
                Selection.setSelection(text, text!!.length)
            }
        }
    }

    private fun textWithoutPrefix(): String {
        return if( text!!.startsWith(prefix) ) {
            text!!.substring(prefix.length)
        } else {
            text.toString()
        }
    }
}