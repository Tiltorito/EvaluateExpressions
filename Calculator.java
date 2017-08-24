import java.util.EmptyStackException;

/**
 * Created by mpampis on 8/3/2017.
 */
public class Calculator {
    private Stack<Operator> operatorsStack;
    private Stack<Double> operandsStack;

    private enum Operator {
        ADDITION(1), SUBSTRACTION(1), MULTIPLICATION(2), DIVISION(2), PARENTHESIS_LEFT(3),
        PARENTHESIS_RIGHT(3);
        public int priority;

        private Operator(int priority) {
            this.priority = priority;
        }

    }

    /**
     * Calculates an expression.
     * @param expression the expression to be evaluated.
     * @return returns the result of the expression.
     */
    public double calculate(String expression) {
        String[] expressionElements = realignExpression(expression).split(" ");

        if(!checkParenthesis(expression)) {
            return 0f;
        }

        operatorsStack = new Stack<>();
        operandsStack = new Stack<>();

        for(String str : expressionElements) {
            if(str.isEmpty()) {
                continue;
            }
            if(isNumber(str)) {
                operandsStack.push(Double.parseDouble(str));
            }
            else {
                Operator operator = chToOperator(str.charAt(0)); // operators are single characters
                if(operator == null) {
                    return 0f;
                }

                if(operator == Operator.PARENTHESIS_RIGHT) {
                    evaluateParenthesis(operatorsStack, operandsStack); // αν θες να εισαι secure κοιταξε εαν επιστρεφει false.
                    // αν ειναι false την γαμησες..
                }
                else if(operatorsStack.isEmpty() || operator.priority > operatorsStack.peek().priority || operatorsStack.peek() == Operator.PARENTHESIS_LEFT) {
                    operatorsStack.push(operator);
                }
                else {
                    try {
                        double res = operateRevArg(operandsStack.pop(), operandsStack.pop(), operatorsStack.pop());
                        operandsStack.push(res);
                        operatorsStack.push(operator);
                    }
                    catch(EmptyStackException e) {
                        return 0f;
                    }
                }
            }
        }

        while(!operatorsStack.isEmpty()) {
            double res = operateRevArg(operandsStack.pop(), operandsStack.pop(), operatorsStack.pop());
            operandsStack.push(res);
        }

        double finalRes = operandsStack.pop();
        operandsStack = null;
        operatorsStack = null;
        return finalRes;
    }

    /**
     * Pops and perform every operation until it finds a PARENTHESIS_LEFT '('. Mostly you should call this function when
     * you find PARENTHESIS_RIGHT ')'
     * @param operatorsStack The stack with the operators.
     * @param operandsStack  The stack with the operators.
     * @return true if everything went smooth. If returned false the stacks may be corrupted.
     */
    private boolean evaluateParenthesis(Stack<Operator> operatorsStack, Stack<Double> operandsStack) {
        Operator operator;
        operator = operatorsStack.pop();


        try {
            while (operator != Operator.PARENTHESIS_LEFT) {
                double res = operateRevArg(operandsStack.pop(), operandsStack.pop(), operator);
                operandsStack.push(res);
                operator = operatorsStack.pop();
            }
        }catch(EmptyStackException e) {
            return false;
        }

        return true;
    }

    /**
     * Distinguish every number and operator
     * @param expression The expression in whatever form.
     * @return The above expression but with space among all the different elements.
     */
    public String realignExpression(String expression) {
        StringBuilder str = new StringBuilder(16);
        expression = expression.trim(); // βγαζουμε κενα απο την αρχη και το τελος.

        for(int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if(chToOperator(ch) == null && !Character.isDigit(ch)) {
                continue;
            }
            int indexOfNextNumber = findNextNumber(expression, i);
            int indexOfNextOperator = findNextOperator(expression, i);

            if(indexOfNextNumber < indexOfNextOperator && indexOfNextNumber != -1 || indexOfNextOperator == -1 && indexOfNextNumber != -1) {
                int length = lengthOfNumber(expression, indexOfNextNumber);
                str.append(expression.substring(indexOfNextNumber, indexOfNextNumber + length));
                i += length -1; // step over the number.
            }
            else if(indexOfNextOperator != -1) {
                str.append(expression.charAt(indexOfNextOperator));
            }

            str.append(' ');
        }


        return str.toString().trim();
    }

    /**
     * Finds the next Operator inside a string, starting from the startIndex.
     * @param expression The string which the operator will be searched.
     * @param startIndex The startIndex inside the string.
     * @return returns the index of the next operator or -1 if it could not be found.
     */
    private int findNextOperator(String expression, int startIndex) {
        for(int i = startIndex; i < expression.length(); i++) {
            if(chToOperator(expression.charAt(i)) != null) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the next number inside a string, starting from the startIndex.
     * @param expression The string which the number will be searched.
     * @param startIndex The startIndex inside the string.
     * @return returns the index at the beginning of the number or -1 if it could not be found.
     */
    private int findNextNumber(String expression, int startIndex) {
        for(int i = startIndex; i < expression.length(); i++) {
            if(Character.isDigit(expression.charAt(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Count the length of a number
     * @param expression The string which the number will be searched.
     * @param startIndex The startIndex inside the string.
     * @return the length of the number at startIndex.
     */
    private int lengthOfNumber(String expression, int startIndex) {
        int ctr = 0;
        for(int i = startIndex + 1; i <= expression.length();i++) {
            try {
                Double.parseDouble(expression.substring(startIndex, i));
            }catch(NumberFormatException e) {
                return ctr;
            }
            ctr++;
        }

        return ctr;
    }

    /**
     * Convert a ch to the right Operator.
     * @param ch The operator in char form.
     * @return The Operator which correspond to @ch if the conversion was successful or null if it failed.
     */
    private Operator chToOperator(char ch) {
        Operator op = null;
        switch(ch) {
            case '+' :
                op = Operator.ADDITION;
                break;
            case '-' :
                op = Operator.SUBSTRACTION;
                break;
            case '*' :
                op = Operator.MULTIPLICATION;
                break;
            case '/' :
                op = Operator.DIVISION;
                break;
            case '(' :
                op = Operator.PARENTHESIS_LEFT;
                break;
            case ')' :
                op = Operator.PARENTHESIS_RIGHT;
        }

        return op;
    }

    /**
     * Calling the operate method after reversing the arguments, see the code. More info at operate doc
     * @param b .
     * @param a .
     * @param operator .
     * @return .
     */
    private double operateRevArg(double b, double a, Operator operator) {
        return operate(a, b, operator);
    }

    /**
     * Execute the corresponding operation
     * @param a the first number
     * @param b the second number
     * @param operator the operator
     * @return the result of the operation which performed among this 2 numbers.
     */
    private double operate(double a, double b, Operator operator) {
        double res = 0;

        if(operator == Operator.ADDITION) {
            res = a + b;
        }
        else if(operator == Operator.SUBSTRACTION) {
            res = a - b;
        }
        else if(operator == Operator.MULTIPLICATION) {
            res = a * b;
        }
        else if(operator == Operator.DIVISION) {
            res = a / b;
        }

        return res;
    }

    /**
     *   TO - DOOO
     *   Φενεται δυσκολο και βρμ να κατσω να το κανω να κοιταει εαν ολα ειναι καλα πριν ξεκινησω να κανω της πραξεις
     *   θα βολευει εαν ηταν σε RPN
     * @param expression .
     * @return .
     */
    private boolean checkExpression(char[] expression) {
        return true;
        // TO - DOO
    }

    /**
     * Check if a str is number
     * @param str the str to be checked.
     * @return true if it is number. False otherwise.
     */
    private boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
        }
        catch(NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Check for unclosed parenthesis or whatever is invalid regarding the parenthesis.
     * @param expression The expression
     * @return returns true if it is valid expression otherwise false.
     */
    private boolean checkParenthesis(String expression) {
        Stack<Character> stack = new Stack<>();

        for(char ch : expression.toCharArray()) {
            if(ch == '(') {
                stack.push(ch);
            }
            else if(ch == ')') {
                try {
                    char chAtStack = stack.pop();
                    if(chAtStack != '(') {
                        return false;
                    }
                }
                catch(EmptyStackException e) {
                    return false;
                }
            }
        }

        return stack.isEmpty();
    }
}
