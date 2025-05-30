package Lab1;

import java.util.*;


public class Grammar
{
    private final Set<String> V_n;
    private final Set<String> V_t;
    private final Map<String, List<String>> P;

    public Grammar(Set<String> V_n, Set<String> V_t, Map<String, List<String>> P) {
        this.V_n = V_n;
        this.V_t = V_t;
        this.P = P;
    }

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
        List<String> choices = this.P.get(symbol);
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
        Set<String> alphabet = new HashSet<>(V_t);
        Map<String, Map<String, Set<String>>> transitions = new HashMap<>();

        transitions.put("S", new HashMap<>() {{
            put("a", new HashSet<>(Collections.singletonList("B")));
        }});

        transitions.put("B", new HashMap<>() {{
            put("b", new HashSet<>(Arrays.asList("S", "B")));  // multiple transitions for "b"
            put("a", new HashSet<>(Collections.singletonList("C")));
        }});

        transitions.put("C", new HashMap<>() {{
            put("b", new HashSet<>(Collections.singletonList("D")));
        }});

        transitions.put("D", new HashMap<>() {{
            put("a", new HashSet<>(Collections.singletonList("D")));
            put("b", new HashSet<>(Collections.singletonList("C")));
            put("c", new HashSet<>(Collections.singletonList("S")));
        }});

        // final states
        Set<String> finalStates = new HashSet<>();
        finalStates.add("B");
        finalStates.add("D");

        return new FiniteAutomaton(alphabet, states, "S", finalStates, transitions);
    }
}