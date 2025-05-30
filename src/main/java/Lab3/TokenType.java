package Lab3;

public enum TokenType {
    NUMBER,         // integers and floating point numbers
    PLUS,           // +
    MINUS,          // -
    MULTIPLY,       // *
    DIVIDE,         // /
    LPAREN,         // (
    RPAREN,         // )
    SIN,            // sin function
    COS,            // cos function
    TAN,            // tan function
    POWER,          // ^ for exponentiation
    EOL,            // End of line
    WHITESPACE,     // spaces and tabs
    INVALID         // invalid token
}