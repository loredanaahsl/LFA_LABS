package Lab4;
import java.util.ArrayList;
import java.util.List;

class RegexParser {
    private String regex;
    private int position;
    private List<Token> tokens;

    public RegexParser(String regex) {
        this.regex = regex.replace("^", "{"); // Replace ^ with { for parsing
        this.position = 0;
        this.tokens = new ArrayList<>();
    }

    public List<Token> parse() {
        while (position < regex.length()) {
            char c = regex.charAt(position);

            if (c == '(') {
                tokens.add(new Token(TokenType.OPEN_PAREN, "("));
                position++;
            } else if (c == ')') {
                tokens.add(new Token(TokenType.CLOSE_PAREN, ")"));
                position++;
            } else if (c == '|') {
                tokens.add(new Token(TokenType.OR, "|"));
                position++;
            } else if (c == '+') {
                tokens.add(new Token(TokenType.PLUS, "+"));
                position++;
            } else if (c == '*') {
                tokens.add(new Token(TokenType.STAR, "*"));
                position++;
            } else if (c == '?') {
                tokens.add(new Token(TokenType.QUESTION, "?"));
                position++;
            } else if (c == '{') {
                tokens.add(new Token(TokenType.REPEAT_START, "{"));
                position++;
                parseNumber();
                if (position < regex.length() && regex.charAt(position) == ',') {
                    tokens.add(new Token(TokenType.COMMA, ","));
                    position++;
                    parseNumber();
                }
                if (position < regex.length() && regex.charAt(position) == '}') {
                    tokens.add(new Token(TokenType.REPEAT_END, "}"));
                    position++;
                }
            } else if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                // Skip whitespace
                position++;
            } else {
                // Literal character or a sequence of literals
                StringBuilder literal = new StringBuilder();
                while (position < regex.length() &&
                        "(){}|+*? \t\n\r".indexOf(regex.charAt(position)) == -1) {
                    literal.append(regex.charAt(position));
                    position++;
                }
                if (literal.length() > 0) {
                    tokens.add(new Token(TokenType.LITERAL, literal.toString()));
                }
            }
        }
        return tokens;
    }

    private void parseNumber() {
        StringBuilder number = new StringBuilder();
        while (position < regex.length() && Character.isDigit(regex.charAt(position))) {
            number.append(regex.charAt(position));
            position++;
        }
        if (number.length() > 0) {
            tokens.add(new Token(TokenType.NUMBER, number.toString()));
        }
    }
}