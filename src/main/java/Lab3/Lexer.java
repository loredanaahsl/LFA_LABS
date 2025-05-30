package Lab3;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int position;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
    }

    // method to get the current character
    private char getCurrentChar() {
        return (position < input.length()) ? input.charAt(position) : '\0';
    }

    // method to advance the position
    private void advance() {
        position++;
    }

    // method to advance multiple positions
    private void advance(int steps) {
        position += steps;
    }

    // method to match numbers (integers and floats)
    private String matchNumber() {
        if (position >= input.length() || !Character.isDigit(getCurrentChar())) {
            return null;
        }

        int start = position;
        boolean hasDecimal = false;

        while (position < input.length() &&
                (Character.isDigit(getCurrentChar()) || getCurrentChar() == '.')) {
            if (getCurrentChar() == '.') {
                if (hasDecimal) {
                    break; // second decimal point is not part of this number
                }
                hasDecimal = true;
            }
            advance();
        }

        return input.substring(start, position);
    }

    // custom matcher for keywords (sin, cos, tan)
    private String matchKeyword(String keyword) {
        if (position + keyword.length() <= input.length() &&
                input.substring(position, position + keyword.length()).equals(keyword)) {
            // make sure the keyword is not part of a larger identifier
            int endPos = position + keyword.length();
            if (endPos >= input.length() || !Character.isLetter(input.charAt(endPos))) {
                return keyword;
            }
        }
        return null;
    }

    // method to skip whitespace
    private void skipWhitespace() {
        while (position < input.length() && Character.isWhitespace(getCurrentChar())) {
            advance();
        }
    }

    // get the next token
    public Token getNextToken() {
        // skip whitespace before finding the next token
        skipWhitespace();

        // check if we've reached the end of input
        if (position >= input.length()) {
            return new Token(TokenType.EOL, "");
        }

        // check for numbers (integers and floats)
        String number = matchNumber();
        if (number != null) {
            return new Token(TokenType.NUMBER, number);
        }

        // check for trigonometric functions
        String keyword;
        if ((keyword = matchKeyword("sin")) != null) {
            advance(3);
            return new Token(TokenType.SIN, keyword);
        }

        if ((keyword = matchKeyword("cos")) != null) {
            advance(3);
            return new Token(TokenType.COS, keyword);
        }

        if ((keyword = matchKeyword("tan")) != null) {
            advance(3);
            return new Token(TokenType.TAN, keyword);
        }

        // check for single character tokens
        char currentChar = getCurrentChar();
        advance(); // advance for all single-character tokens

        switch (currentChar) {
            case '+': return new Token(TokenType.PLUS, "+");
            case '-': return new Token(TokenType.MINUS, "-");
            case '*': return new Token(TokenType.MULTIPLY, "*");
            case '/': return new Token(TokenType.DIVIDE, "/");
            case '^': return new Token(TokenType.POWER, "^");
            case '(': return new Token(TokenType.LPAREN, "(");
            case ')': return new Token(TokenType.RPAREN, ")");
            default:  return new Token(TokenType.INVALID, String.valueOf(currentChar));
        }
    }

    // process the entire input and return a list of tokens
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        Token token;

        // reset position to ensure we're starting from the beginning
        position = 0;

        while ((token = getNextToken()).getType() != TokenType.EOL) {
            tokens.add(token);
        }

        return tokens;
    }
}