package Lab3;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] testInputs = {
                "3.04 + 42",
                "sin(0.5) * 2",
                "cos(3.14) / 2.5",
                "2^4 + (4 * 5.2)",
                "tan(0.85) - 1.5",
                "strawberry"
        };

        for (String input : testInputs) {
            System.out.println("\nInput: " + input);
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();

            System.out.println("Tokens:");
            for (Token token : tokens) {
                System.out.println("  " + token);
            }
        }
    }
}
