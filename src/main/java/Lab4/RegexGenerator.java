package Lab4;

import java.util.*;

public class RegexGenerator {
    private List<Token> tokens;
    private Random random;
    private Map<String, List<String>> allProcessingSteps;
    private List<String> currentSteps;
    private int maxIterations = 5; //Limit for * and + operators

    public RegexGenerator(List<Token> tokens) {
        this.tokens = tokens;
        this.random = new Random();
        this.allProcessingSteps = new LinkedHashMap<>();
        this.currentSteps = new ArrayList<>();
    }

    public List<String> generate(int count) {
        List<String> generatedStrings = new ArrayList<>();
        allProcessingSteps.clear();

        for (int i = 0; i < count * 2 && generatedStrings.size() < count; i++) {
            currentSteps = new ArrayList<>();
            String generated = generateFromTokens(0, tokens.size());

            if (generated != null && !generated.isEmpty() && !generatedStrings.contains(generated)) {
                generatedStrings.add(generated);
                allProcessingSteps.put(generated, new ArrayList<>(currentSteps));
            }
        }

        return generatedStrings;
    }

    public Map<String, List<String>> getAllProcessingSteps() {
        return allProcessingSteps;
    }

    private String generateFromTokens(int startIndex, int endIndex) {
        StringBuilder result = new StringBuilder();

        for (int i = startIndex; i < endIndex && i < tokens.size(); i++) {
            Token token = tokens.get(i);

            switch (token.getType()) {
                case LITERAL:
                    result.append(token.getValue());
                    addStep("Added literal: " + token.getValue());
                    break;

                case OPEN_PAREN:
                    // find matching closing parenthesis
                    int closeIndex = findMatchingCloseParen(i);
                    if (closeIndex == -1) {
                        addStep("Error: Missing closing parenthesis");
                        return null;
                    }

                    // check if followed by repetition
                    if (closeIndex + 1 < tokens.size()) {
                        Token nextToken = tokens.get(closeIndex + 1);
                        if (nextToken.getType() == TokenType.PLUS) {
                            // (expr)+ : one or more repetitions
                            int repetitions = random.nextInt(maxIterations) + 1;
                            addStep("Repeating group " + repetitions + " times (+ operator)");
                            for (int j = 0; j < repetitions; j++) {
                                addStep("Repetition " + (j+1) + " of group:");
                                result.append(handleOrGroup(i + 1, closeIndex));
                            }
                            i = closeIndex + 1; // skip the + operator
                        } else if (nextToken.getType() == TokenType.STAR) {
                            // (expr)* : zero or more repetitions
                            int repetitions = random.nextInt(maxIterations + 1);
                            addStep("Repeating group " + repetitions + " times (* operator)");
                            for (int j = 0; j < repetitions; j++) {
                                addStep("Repetition " + (j+1) + " of group:");
                                result.append(handleOrGroup(i + 1, closeIndex));
                            }
                            i = closeIndex + 1; // skip the * operator
                        } else if (nextToken.getType() == TokenType.QUESTION) {
                            // (expr)? : zero or one repetition
                            boolean include = random.nextBoolean();
                            addStep("Question operator: " + (include ? "including" : "excluding") + " group");
                            if (include) {
                                result.append(handleOrGroup(i + 1, closeIndex));
                            }
                            i = closeIndex + 1; // skip the ? operator
                        } else if (nextToken.getType() == TokenType.REPEAT_START) {
                            // handle {n} and {n,m} repetition
                            i = handleRepetition(i, closeIndex, result);
                        } else {
                            // regular group without repetition
                            addStep("Processing group without repetition:");
                            result.append(handleOrGroup(i + 1, closeIndex));
                            i = closeIndex;
                        }
                    } else {
                        addStep("Processing group without repetition:");
                        result.append(handleOrGroup(i + 1, closeIndex));
                        i = closeIndex;
                    }
                    break;

                case PLUS:
                    if (i > 0 && tokens.get(i-1).getType() == TokenType.LITERAL) {
                        String literal = tokens.get(i-1).getValue();
                        int repetitions = random.nextInt(maxIterations) + 1;
                        addStep("Repeating literal '" + literal + "' " + repetitions + " more times (+ operator)");
                        for (int j = 0; j < repetitions; j++) {
                            result.append(literal);
                        }
                    }
                    break;

                case STAR:
                    // handle literals followed by * (zero or more)
                    if (i > 0 && tokens.get(i-1).getType() == TokenType.LITERAL) {
                        String literal = tokens.get(i-1).getValue();
                        int repetitions = random.nextInt(maxIterations);
                        addStep("Repeating literal '" + literal + "' " + repetitions + " more times (* operator)");
                        for (int j = 0; j < repetitions; j++) {
                            result.append(literal);
                        }
                    }
                    break;

                case QUESTION:
                    // handle literals followed by ? (zero or one)
                    if (i > 0 && tokens.get(i-1).getType() == TokenType.LITERAL) {
                        boolean remove = random.nextBoolean();
                        addStep("Question operator for preceding literal: " + (remove ? "removing" : "keeping"));
                        if (remove) {
                            result.delete(result.length() - tokens.get(i-1).getValue().length(), result.length());
                        }
                    }
                    break;

                case REPEAT_START:
                    // handle {n} and {n,m} repetition for literals
                    if (i > 0 && tokens.get(i-1).getType() == TokenType.LITERAL) {
                        String literal = tokens.get(i-1).getValue();
                        int j = i + 1;
                        if (j < tokens.size() && tokens.get(j).getType() == TokenType.NUMBER) {
                            int n = Integer.parseInt(tokens.get(j).getValue());
                            int m = n;
                            j++;

                            // check for {n,m} format
                            if (j < tokens.size() && tokens.get(j).getType() == TokenType.COMMA) {
                                j++;
                                if (j < tokens.size() && tokens.get(j).getType() == TokenType.NUMBER) {
                                    m = Integer.parseInt(tokens.get(j).getValue());
                                    j++;
                                }
                            }

                            // find the closing }
                            while (j < tokens.size() && tokens.get(j).getType() != TokenType.REPEAT_END) {
                                j++;
                            }

                            result.delete(result.length() - literal.length(), result.length());

                            int repetitions = (m > n) ? n + random.nextInt(m - n + 1) : n;
                            addStep("Repeating literal '" + literal + "' " + repetitions + " times ({n} operator)");
                            for (int k = 0; k < repetitions; k++) {
                                result.append(literal);
                                addStep("Added repetition " + (k+1) + " of literal '" + literal + "'");
                            }

                            // skip to after the closing }
                            i = j;
                        }
                    }
                    break;
            }
        }

        return result.toString();
    }

    private int handleRepetition(int openParenIndex, int closeParenIndex, StringBuilder result) {
        int currentIndex = closeParenIndex + 2; // Start after the {

        // get the minimum repetition count
        if (currentIndex < tokens.size() && tokens.get(currentIndex).getType() == TokenType.NUMBER) {
            int minRepeat = Integer.parseInt(tokens.get(currentIndex).getValue());
            int maxRepeat = minRepeat;
            currentIndex++;

            // check for range {n,m}
            if (currentIndex < tokens.size() && tokens.get(currentIndex).getType() == TokenType.COMMA) {
                currentIndex++;
                if (currentIndex < tokens.size() && tokens.get(currentIndex).getType() == TokenType.NUMBER) {
                    maxRepeat = Integer.parseInt(tokens.get(currentIndex).getValue());
                    currentIndex++;
                }
            }

            // find the closing }
            while (currentIndex < tokens.size() && tokens.get(currentIndex).getType() != TokenType.REPEAT_END) {
                currentIndex++;
            }

            // generate the required repetitions
            int repetitions = (maxRepeat > minRepeat) ?
                    minRepeat + random.nextInt(maxRepeat - minRepeat + 1) : minRepeat;

            addStep("Repeating group exactly " + repetitions + " times ({n} operator)");
            for (int i = 0; i < repetitions; i++) {
                addStep("Processing repetition " + (i+1) + " of group:");
                String groupContent = handleOrGroup(openParenIndex + 1, closeParenIndex);
                addStep("Generated group content for repetition " + (i+1) + ": " + groupContent);
                result.append(groupContent);
            }

            // skip to after the closing }
            return currentIndex;
        }

        return closeParenIndex;
    }

    private String handleOrGroup(int startIndex, int endIndex) {
        List<Integer> orPositions = new ArrayList<>();
        orPositions.add(startIndex - 1); // start position

        for (int i = startIndex; i < endIndex; i++) {
            if (tokens.get(i).getType() == TokenType.OR) {
                orPositions.add(i);
            }
        }
        orPositions.add(endIndex); // end position

        // randomly select one of the alternatives
        int randomChoice = random.nextInt(orPositions.size() - 1);
        int choiceStart = orPositions.get(randomChoice) + 1;
        int choiceEnd = orPositions.get(randomChoice + 1);

        addStep("Selected alternative " + (randomChoice + 1) + " from OR group");

        // generate the selected alternative
        return generateFromTokens(choiceStart, choiceEnd);
    }

    private int findMatchingCloseParen(int openIndex) {
        int level = 1;
        for (int i = openIndex + 1; i < tokens.size(); i++) {
            if (tokens.get(i).getType() == TokenType.OPEN_PAREN) {
                level++;
            } else if (tokens.get(i).getType() == TokenType.CLOSE_PAREN) {
                level--;
                if (level == 0) {
                    return i;
                }
            }
        }
        return -1; // no matching parenthesis found
    }

    private void addStep(String step) {
        currentSteps.add(step);
    }
}