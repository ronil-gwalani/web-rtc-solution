package com.renxo.user.utils.calculator


class ExpressionWriter {

    var expression = ""

    fun processAction(action: CalculatorAction) {
        when (action) {
            CalculatorAction.Calculate -> {
                val parser = ExpressionParser(prepareForCalculation())
                val evaluator = ExpressionEvaluator(parser.parse())
                expression = evaluator.evaluate().toString()
            }

            CalculatorAction.Clear -> {
                expression = ""
            }

            CalculatorAction.Decimal -> {
                if (canEnterDecimal()) {
                    expression += "."
                }
            }

            CalculatorAction.Delete -> {
                expression = expression.dropLast(1)
            }

            is CalculatorAction.Number -> {
                expression += action.number
            }

            is CalculatorAction.Op -> {
                if (canEnterOperation(action.operation)) {
                    expression += action.operation.symbol
                }
            }

            CalculatorAction.Parentheses -> {
                processParentheses()
            }
        }
    }

    private fun prepareForCalculation(): String {
        val newExpression = expression.dropLastWhile {
            it in "$operationSymbols(."
        }
        if (newExpression.isEmpty()) {
            return "0"
        }
        return newExpression
    }

    private fun processParentheses() {
        val openingCount = expression.count { it == '(' }
        val closingCount = expression.count { it == ')' }
        expression += when {
            expression.isEmpty() ||
                    expression.last() in "$operationSymbols(" -> "("

            expression.last() in "0123456789)" &&
                    openingCount == closingCount -> return

            else -> ")"
        }
    }

    private fun canEnterDecimal(): Boolean {
        if (expression.isEmpty() || expression.last() in "$operationSymbols.()") {
            return false
        }
        return !expression.takeLastWhile {
            it in "0123456789."
        }.contains(".")
    }

    private fun canEnterOperation(operation: Operation): Boolean {
        if (expression.isEmpty()) {
            // Allow only add/subtract if you want to support negative numbers at the start.
            return operation == Operation.ADD || operation == Operation.SUBTRACT
        }
        return when (operation) {
            Operation.ADD, Operation.SUBTRACT ->
                expression.last() in "$operationSymbols()0123456789"

            else -> expression.last() in "0123456789)"
        }
    }
}
