package Lab1;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FiniteAutomaton
{
    private final Set<String> alphabet;
    private final String startState;
    private final Set<String> finalStates;
    private final Set<String> states;
    private final Map<String, Map<String, Set<String>>> transitions;;


    public FiniteAutomaton(Set<String> alphabet, Set<String> states, String startState, Set<String> finalStates, Map<String, Map<String, Set<String>>> transitions) {
        this.alphabet = alphabet;
        this.states = states;
        this.startState = startState;
        this.finalStates = finalStates;
        this.transitions = transitions;
    }

    public boolean stringBelongToLanguage(String inputString) {
        Set<String> currentStates = new HashSet<>();
        currentStates.add(startState);

        for (char symbol : inputString.toCharArray()) {
            String symbolString = String.valueOf(symbol);

            if (!alphabet.contains(symbolString)) {
                System.out.println("ERROR: Symbol '" + symbolString + "' not in alphabet!");
                return false;
            }

            Set<String> nextStates = new HashSet<>();
            for (String state : currentStates) {
                Map<String, Set<String>> stateTransitions = transitions.get(state);
                if (stateTransitions != null && stateTransitions.containsKey(symbolString)) {
                    nextStates.addAll(stateTransitions.get(symbolString));
                }
            }

            if (nextStates.isEmpty()) {
                System.out.println("No transition found for any state with symbol '" + symbolString + "'.");
                return false;
            }

            currentStates = nextStates;
        }

        for (String state : currentStates) {
            if (finalStates.contains(state)) {
                return true;
            }
        }

        return false;
    }

}
