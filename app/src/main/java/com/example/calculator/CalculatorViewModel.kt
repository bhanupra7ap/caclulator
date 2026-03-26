package com.example.calculator

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10

data class HistoryItem(
    val expression: String,
    val result: String
)

class CalculatorViewModel : ViewModel() {
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val historyKey = "calculator_history"
    
    var display by mutableStateOf("0")
        private set
    
    var previousDisplay by mutableStateOf("")
        private set
    
    var currentOperation by mutableStateOf("")
        private set
    
    var secondOperand by mutableStateOf("")
        private set

    var history by mutableStateOf<List<HistoryItem>>(emptyList())
        private set
    
    var isShowingHistory by mutableStateOf(false)
        private set

    var shouldOpenVault by mutableStateOf(false)
        private set

    private var previousValue = 0.0
    private var currentOperationInternal: String? = null
    private var shouldResetDisplay = false
    private var securityManager: SecurityManager? = null

    fun initializeSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
        loadHistory()
        securityManager = SecurityManager(context)
    }

    private fun loadHistory() {
        val historyJson = sharedPreferences.getString(historyKey, null)
        history = if (historyJson != null) {
            try {
                val type = object : TypeToken<List<HistoryItem>>() {}.type
                gson.fromJson(historyJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun saveHistory() {
        val historyJson = gson.toJson(history)
        sharedPreferences.edit().putString(historyKey, historyJson).apply()
    }

    fun onNumberClick(number: String) {
        if (shouldResetDisplay) {
            display = number
            shouldResetDisplay = false
            // Clear history when starting a new calculation
            if (currentOperationInternal == null) {
                previousDisplay = ""
                currentOperation = ""
                secondOperand = ""
            }
        } else {
            val newDisplay = if (display == "0") number else display + number
            // Count digits only (exclude decimal point and minus sign)
            val digitCount = newDisplay.filter { it.isDigit() }.length
            if (digitCount <= 9) {
                display = newDisplay
            }
        }
    }

    fun onDecimalClick() {
        if (!display.contains(".")) {
            display += "."
            shouldResetDisplay = false
        }
    }

    fun onOperationClick(op: String) {
        if (currentOperationInternal != null && !shouldResetDisplay) {
            // Chain operations: calculate the result first
            onEqualsClick()
        }
        previousValue = display.toDoubleOrNull() ?: 0.0
        previousDisplay = display
        secondOperand = ""  // Clear the second operand when starting a new operation
        currentOperationInternal = op
        // Format the operation for display
        val displayOp = when (op) {
            "*" -> "×"
            "/" -> "÷"
            "-" -> "−"
            else -> op
        }
        currentOperation = displayOp
        shouldResetDisplay = true
    }

    fun onEqualsClick() {
        // Check if the display matches the password to open the vault
        if (securityManager?.isPasswordSet() == true && 
            currentOperationInternal == null && 
            display.isNotEmpty() && 
            display != "0") {
            if (securityManager?.verifyPassword(display) == true) {
                shouldOpenVault = true
                onClear()
                return
            }
        }

        if (currentOperationInternal != null) {
            val current = display.toDoubleOrNull() ?: 0.0
            secondOperand = display
            val result = when (currentOperationInternal) {
                "+" -> previousValue + current
                "-" -> previousValue - current
                "*" -> previousValue * current
                "/" -> if (current != 0.0) previousValue / current else 0.0
                else -> current
            }
            val resultStr = formatResult(result)
            
            // Add to history
            val displayOp = when (currentOperationInternal) {
                "*" -> "×"
                "/" -> "÷"
                "-" -> "−"
                else -> currentOperationInternal
            }
            val expression = "$previousValue $displayOp $current"
            val historyItem = HistoryItem(expression, resultStr)
            history = listOf(historyItem) + history
            saveHistory()
            
            display = resultStr
            // Keep previousDisplay, currentOperation and secondOperand visible
            currentOperationInternal = null
            shouldResetDisplay = true
        }
    }

    fun onClear() {
        display = "0"
        previousDisplay = ""
        secondOperand = ""
        previousValue = 0.0
        currentOperationInternal = null
        currentOperation = ""
        shouldResetDisplay = false
    }

    fun onDelete() {
        display = if (display.length > 1) {
            display.dropLast(1)
        } else {
            "0"
        }
    }

    fun onPlusMinus() {
        val value = display.toDoubleOrNull() ?: 0.0
        display = if (value > 0) {
            "-$display"
        } else if (display.startsWith("-")) {
            display.substring(1)
        } else if (display != "0") {
            "-$display"
        } else {
            display
        }
    }

    fun onPercentage() {
        val value = display.toDoubleOrNull() ?: 0.0
        if (previousValue != 0.0 && currentOperation != null) {
            val result = (value / 100) * previousValue
            display = formatResult(result)
        } else {
            val result = value / 100
            display = formatResult(result)
        }
    }

    private fun formatResult(value: Double): String {
        // Check if the absolute value has more than 9 digits
        val absValue = abs(value)
        val digitCount = if (absValue >= 1.0) {
            floor(log10(absValue)).toInt() + 1
        } else if (absValue == 0.0) {
            1
        } else {
            0
        }
        
        // If more than 9 digits, use scientific notation
        if (digitCount > 9) {
            return String.format("%.2E", value)
        }
        
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.10f", value)
                .trimEnd('0')
                .trimEnd('.')
        }
    }

    fun toggleHistory() {
        isShowingHistory = !isShowingHistory
    }

    fun closeHistory() {
        isShowingHistory = false
    }

    fun clearHistory() {
        history = emptyList()
        sharedPreferences.edit().remove(historyKey).apply()
    }

    fun closeVault() {
        shouldOpenVault = false
    }
}

