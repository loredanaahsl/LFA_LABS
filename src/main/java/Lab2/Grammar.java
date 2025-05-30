package Lab2;

import java.util.*;
import java.util.stream.Collectors;

public class Grammar {
    private Set<String> V_n;
    private Set<String> V_t;
    private Map<String, Set<String>> P;

    public Grammar() {
        this.V_n = new HashSet<>();
        this.V_t = new HashSet<>();
        this.P = new HashMap<>();
    }

    public String classifyGrammar() {
        boolean isRightRegular = isRightRegularGrammar();
        boolean isLeftRegular = isLeftRegularGrammar();

        if (isRightRegular || isLeftRegular) {
            return "Type 3: Regular Grammar";
        }else if (isContextFreeGrammar()) {
            return "Type 2: Context-Free Grammar";
        }
        else if (isContextSensitiveGrammar()) {
            return "Type 1: Context-Sensitive Grammar";
        }
        else {
            return "Type 0: Unrestricted Grammar";
        }
    }

    // Right-regular grammar checker
    private boolean isRightRegularGrammar() {
        for (Map.Entry<String, Set<String>> entry : P.entrySet()) {
            String leftSide = entry.getKey();
            Set<String> rightSides = entry.getValue();

            // left side must be a non-terminal
            if (!V_n.contains(leftSide)) {
                return false;
            }

            for (String rightSide : rightSides) {
                // empty string (ε) is allowed
                if (rightSide.equals("ε")) {
                    continue;
                }

                // check each right side for valid format
                boolean validRightSide = false;

                // right side contains only terminals
                boolean onlyTerminals = true;
                for (int i = 0; i < rightSide.length(); i++) {
                    String symbol = rightSide.substring(i, i + 1);
                    if (!V_t.contains(symbol)) {
                        onlyTerminals = false;
                        break;
                    }
                }

                if (onlyTerminals) {
                    validRightSide = true;
                } else {
                    // right side is a terminal followed by a non-terminal
                    for (String nonTerminal : V_n) {
                        if (rightSide.endsWith(nonTerminal)) {
                            String prefix = rightSide.substring(0, rightSide.length() - nonTerminal.length());
                            boolean validPrefix = true;
                            for (int i = 0; i < prefix.length(); i++) {
                                String symbol = prefix.substring(i, i + 1);
                                if (!V_t.contains(symbol)) {
                                    validPrefix = false;
                                    break;
                                }
                            }

                            if (validPrefix) {
                                validRightSide = true;
                                break;
                            }
                        }
                    }
                }

                if (!validRightSide) {
                    return false;
                }
            }
        }

        return true;
    }

    // left-regular grammar checker
    private boolean isLeftRegularGrammar() {
        for (Map.Entry<String, Set<String>> entry : P.entrySet()) {
            String leftSide = entry.getKey();
            Set<String> rightSides = entry.getValue();

            // left side must be a non-terminal
            if (!V_n.contains(leftSide)) {
                return false;
            }

            for (String rightSide : rightSides) {
                // empty string (ε) is allowed
                if (rightSide.equals("ε")) {
                    continue;
                }

                // check each right side for valid format
                boolean validRightSide = false;

                // right side contains only terminals
                boolean onlyTerminals = true;
                for (int i = 0; i < rightSide.length(); i++) {
                    String symbol = rightSide.substring(i, i + 1);
                    if (!V_t.contains(symbol)) {
                        onlyTerminals = false;
                        break;
                    }
                }

                if (onlyTerminals) {
                    validRightSide = true;
                } else {
                    // right side is a non-terminal followed by terminals
                    for (String nonTerminal : V_n) {
                        if (rightSide.startsWith(nonTerminal)) {

                            String suffix = rightSide.substring(nonTerminal.length());

                            boolean validSuffix = true;
                            for (int i = 0; i < suffix.length(); i++) {
                                String symbol = suffix.substring(i, i + 1);
                                if (!V_t.contains(symbol)) {
                                    validSuffix = false;
                                    break;
                                }
                            }

                            if (validSuffix) {
                                validRightSide = true;
                                break;
                            }
                        }
                    }
                }

                if (!validRightSide) {
                    return false;
                }
            }
        }

        return true;
    }

    // context-free grammar check
    private boolean isContextFreeGrammar() {
        for (Map.Entry<String, Set<String>> entry : P.entrySet()) {
            String leftSide = entry.getKey();

            // left side must be a single non-terminal
            if (!V_n.contains(leftSide)) {
                return false;
            }
        }
        return true;
    }

    // context-sensitive grammar check
    private boolean isContextSensitiveGrammar() {
        for (Map.Entry<String, Set<String>> entry : P.entrySet()) {
            String leftSide = entry.getKey();
            Set<String> rightSides = entry.getValue();

            for (String rightSide : rightSides) {
                // exception: S → ε is allowed in some cases
                if (leftSide.equals("S") && rightSide.equals("ε")) {
                    continue;
                }

                // |α| ≤ |β|
                if (leftSide.length() > rightSide.length() && !rightSide.equals("ε")) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Non-terminals: ").append(this.V_n.isEmpty() ? "{}" : this.V_n).append("\n");
        sb.append("Terminals: ").append(this.V_t.isEmpty() ? "{}" : this.V_t).append("\n");
        sb.append("Productions: \n");
        for(Map.Entry<String, Set<String>> entry : this.P.entrySet()){
            String nonTerminal = entry.getKey();
            sb.append(nonTerminal).append(" : ");
            Set<String> productions = entry.getValue();
            if(!productions.isEmpty()){
                Iterator<String> iterator = productions.iterator();
                sb.append(iterator.next());
                while(iterator.hasNext()){
                    sb.append(" | ").append(iterator.next());
                }
            }else{
                sb.append("{}");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    //getters and setters
    public Set<String> getV_n() {
        return V_n;
    }

    public Set<String> getV_t() {
        return V_t;
    }

    public Map<String, Set<String>> getP() {
        return P;
    }

    public void setV_n(Set<String> v_n) {
        V_n = v_n;
    }

    public void setV_t(Set<String> v_t) {
        V_t = v_t;
    }

    public void setP(Map<String, Set<String>> p) {
        P = p;
    }

    //1st lab methods
    public List<String> generateStrings(String startSymbol, int numStrings){
        Set<String> generatedStrings = new HashSet<>();
        while(generatedStrings.size() < numStrings){
            StringBuilder sb = new StringBuilder();
            generateString(startSymbol, sb);
            generatedStrings.add(sb.toString());
        }
        return new ArrayList<>(generatedStrings);
    }

    private void generateString(String symbol, StringBuilder sb) {
        if (!this.P.containsKey(symbol)) {
            sb.append(symbol);
            return;
        }
        List<String> choices = new ArrayList<>(this.P.get(symbol));
        if (choices.isEmpty()) return;
        String choice = choices.get(new Random().nextInt(choices.size()));
        for (char c : choice.toCharArray()) {
            if (Character.isUpperCase(c)) {
                generateString(String.valueOf(c), sb);
            } else {
                sb.append(c);
            }
        }
    }

    public FiniteAutomaton toFiniteAutomaton() {
        Set<String> states = new HashSet<>(V_n);
        Set<Character> alphabet = new HashSet<>(V_t.stream().map(s -> s.charAt(0)).collect(Collectors.toSet()));
        Map<String, Map<Character, Set<String>>> transitions = new HashMap<>();

        transitions.put("S", new HashMap<>() {{
            put('a', new HashSet<>(Collections.singletonList("B")));
        }});

        transitions.put("B", new HashMap<>() {{
            put('b', new HashSet<>(Arrays.asList("S", "B")));  // multiple transitions for "b"
            put('a', new HashSet<>(Collections.singletonList("C")));
        }});

        transitions.put("C", new HashMap<>() {{
            put('b', new HashSet<>(Collections.singletonList("D")));
        }});

        transitions.put("D", new HashMap<>() {{
            put('a', new HashSet<>(Collections.singletonList("D")));
            put('b', new HashSet<>(Collections.singletonList("C")));
            put('c', new HashSet<>(Collections.singletonList("S")));
        }});

        // final states
        Set<String> finalStates = new HashSet<>();
        finalStates.add("B");
        finalStates.add("D");

        FiniteAutomaton fa = new FiniteAutomaton();
        fa.setStartState("S");
        fa.setAlphabet(alphabet);
        fa.setTransitions(transitions);
        fa.setFinalStates(finalStates);
        fa.setStates(states);
        return fa;
    }
}