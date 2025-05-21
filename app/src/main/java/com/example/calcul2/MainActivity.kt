package com.example.calcul2

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.calcul2.R

class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private var expression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.display)

        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5,
            R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnPlus,
            R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide, R.id.btnDot
        )

        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                appendToExpression((it as Button).text.toString())
            }
        }

        findViewById<Button>(R.id.btnEqual).setOnClickListener {
            calculateResult()
        }

        findViewById<Button>(R.id.btnC).setOnClickListener {
            expression = ""
            display.text = "0"
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
                display.text = if (expression.isEmpty()) "0" else expression
            }
        }
    }

    private fun appendToExpression(value: String) {
        if (expression.isEmpty() && value in "+*/") return
        expression += value
        display.text = expression
    }

    private fun calculateResult() {
        try {
            val result = evaluate(expression)
            display.text = result.toString()
            expression = result.toString()
        } catch (e: Exception) {
            display.text = "Ошибка"
            expression = ""
        }
    }

    private fun evaluate(expr: String): Double {
        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<Char>()

        var currentNumber = ""
        for (char in expr) {
            if (char.isDigit() || char == '.') {
                currentNumber += char
            } else if (char in "+-*/") {
                numbers.add(currentNumber.toDouble())
                currentNumber = ""
                operators.add(char)
            }
        }
        numbers.add(currentNumber.toDouble())
        // обработка умн и дел
        var i = 0
        while (i < operators.size) {
            if (operators[i] == '*' || operators[i] == '/') {
                val num1 = numbers[i]
                val num2 = numbers[i + 1]
                val result = if (operators[i] == '*') num1 * num2 else num1 / num2
                numbers[i] = result
                numbers.removeAt(i + 1)
                operators.removeAt(i)
            } else {
                i++
            }
        }

        // обработка пл и мин
        var result = numbers[0]
        for (j in 0 until operators.size) {
            result = if (operators[j] == '+') result + numbers[j + 1] else result - numbers[j + 1]
        }

        return result
    }
}