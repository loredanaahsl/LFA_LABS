package Lab4;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String[] regexes = {
                "(a|b)(c|d)E+G?",
                "P(Q|R|S)T(UV|W|X)*Z+",
                "1(0|1)*2(3|4){5}36"
        };

        System.out.println("Starting regex generator...");

        for (String regex : regexes) {
            System.out.println("\nGenerating strings for regex: " + regex);
            generateStrings(regex);
        }

        // user input
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nEnter a regex pattern (or 'exit' to quit):");
            String userRegex = scanner.nextLine();

            if (userRegex.equalsIgnoreCase("exit")) {
                break;
            }

            generateStrings(userRegex);
        }
        scanner.close();
    }

    private static void generateStrings(String regex) {
        try {
            RegexParser parser = new RegexParser(regex);
            List<Token> tokens = parser.parse();

            System.out.println("Parsed tokens: " + tokens);

            RegexGenerator generator = new RegexGenerator(tokens);
            List<String> generatedStrings = generator.generate(7);

            if (generatedStrings.isEmpty()) {
                System.out.println("No strings were generated. There might be an issue with the regex pattern.");
                return;
            }

            System.out.println("Generated strings:");
            for (String str : generatedStrings) {
                System.out.println(str);
            }

            Map<String, List<String>> allSteps = generator.getAllProcessingSteps();

            System.out.println("\nProcessing steps for each generated string:");
            for (Map.Entry<String, List<String>> entry : allSteps.entrySet()) {
                System.out.println("\nString: " + entry.getKey());
                System.out.println("Steps:");
                for (String step : entry.getValue()) {
                    System.out.println("  - " + step);
                }
            }
        } catch (Exception e) {
            System.out.println("Error processing regex: " + e.getMessage());
            e.printStackTrace();
        }
    }
}