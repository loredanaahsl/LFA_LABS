import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Define the grammar components
        Set<Character> VN = new HashSet<>(Arrays.asList('S', 'A', 'B', 'C'));
        Set<Character> VT = new HashSet<>(Arrays.asList('a', 'b'));
        Map<Character, List<String>> P = new HashMap<>();
        P.put('S', Arrays.asList("aA"));
        P.put('A', Arrays.asList("bS","aB"));
        P.put('B', Arrays.asList("bC", "aB"));
        P.put('C', Arrays.asList("aA","b"));
        Character S = 'S';

        // Create the grammar
        System.out.println("=== Creating Grammar ===");
        Grammar grammar = new Grammar(VN, VT, P, S);

        // Generate 5 valid strings
        System.out.println("\n=== Generating 5 Valid Strings ===");
        for (int i = 0; i < 5; i++) {
            System.out.println("String " + (i + 1) + ":");
            grammar.generateString();
        }

        // Convert the grammar to a finite automaton
        System.out.println("\n=== Converting Grammar To Finite Automaton ===");
        FiniteAutomaton automaton = grammar.toFiniteAutomaton();

        // Check if some strings belong to the language
        String[] testStrings = {"ab", "abc", "aabb", "baba"};
        System.out.println("\n=== Checking Strings ===");
        for (String testString : testStrings) {
            System.out.println("Testing String: " + testString);
            boolean belongs = automaton.stringBelongToLanguage(testString);
            System.out.println("Result: " + (belongs ? "Belongs" : "Does Not Belong"));
        }
    }
}