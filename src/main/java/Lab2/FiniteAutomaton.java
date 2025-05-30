package Lab2;

import java.util.*;
import java.util.stream.Collectors;

public class FiniteAutomaton {
    private Set<Character> alphabet;
    private String startState;
    private Set<String> finalStates;
    private Set<String> states;
    private Map<String, Map<Character, Set<String>>> transitions;

    // member variable to store the mapping from DFA states to NFA state sets
    private Map<String, Set<String>> stateToNfaStatesMap;

    public FiniteAutomaton() {
        alphabet = new HashSet<>();
        states = new HashSet<>();
        finalStates = new HashSet<>();
        transitions = new HashMap<>();
        stateToNfaStatesMap = new HashMap<>();
    }

    public void addState(String state) {
        states.add(state);
    }

    public void addFinalState(String state) {
        finalStates.add(state);
    }

    public void addTransition(String fromState, char symbolTransition, String toState) {
        alphabet.add(symbolTransition);
        transitions.computeIfAbsent(fromState, k -> new HashMap<>())
                .computeIfAbsent(symbolTransition, k -> new HashSet<>())
                .add(toState);
    }

    public Grammar convertToRegularGrammar() {
        Grammar grammar = new Grammar();

        Set<String> nonTerminals = new HashSet<>(states);
        grammar.setV_n(nonTerminals);

        Set<String> terminals = alphabet.stream().map(String::valueOf).collect(Collectors.toSet());
        grammar.setV_t(terminals);

        Map<String, Set<String>> productions = new HashMap<>();
        for (String state : states) {
            Set<String> stateProductions = new HashSet<>();

            if (transitions.containsKey(state)) {
                for (Map.Entry<Character, Set<String>> entry : transitions.get(state).entrySet()) {
                    char symbol = entry.getKey();
                    String terminal = String.valueOf(symbol);

                    for (String nextState : entry.getValue()) {
                        stateProductions.add(terminal + nextState);
                    }
                }
            }

            // if the state is final, it should have an ε-production
            if (finalStates.contains(state)) {
                stateProductions.add("ε");
            }

            if (!stateProductions.isEmpty()) {
                productions.put(state, stateProductions);
            }
        }

        grammar.setP(productions);
        return grammar;
    }

    public boolean isDeterministic() {
        for(String state : states) {
            if(transitions.containsKey(state)) {
                for(char symbol : alphabet) {
                    if(transitions.get(state).containsKey(symbol) && transitions.get(state).get(symbol).size() > 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public FiniteAutomaton convertToDFA() {
        FiniteAutomaton dfa = new FiniteAutomaton();
        dfa.setAlphabet(new HashSet<>(alphabet));

        // create a map to store new state names (DFA state name -> Set of NFA states)
        Map<Set<String>, String> stateNameMap = new HashMap<>();
        Map<String, Set<String>> reverseStateNameMap = new HashMap<>();

        // create the initial DFA state corresponding to the epsilon closure of NFA start state
        Set<String> initialStateSet = epsilonClosure(Collections.singleton(startState));
        String initialStateName = "q0";
        stateNameMap.put(initialStateSet, initialStateName);
        reverseStateNameMap.put(initialStateName, initialStateSet);

        dfa.addState(initialStateName);
        dfa.setStartState(initialStateName);

        // check if initial state contains any final states
        boolean isFinal = false;
        for (String state : initialStateSet) {
            if (finalStates.contains(state)) {
                isFinal = true;
                break;
            }
        }
        if (isFinal) {
            dfa.addFinalState(initialStateName);
        }

        Queue<Set<String>> stateQueue = new LinkedList<>();
        stateQueue.add(initialStateSet);

        Set<Set<String>> processedStates = new HashSet<>();

        int stateCounter = 1;

        while (!stateQueue.isEmpty()) {
            Set<String> currentStateSet = stateQueue.poll();
            if (processedStates.contains(currentStateSet)) {
                continue;
            }
            processedStates.add(currentStateSet);

            for (char symbol : alphabet) {
                Set<String> nextStateSet = move(currentStateSet, symbol);
                if (!nextStateSet.isEmpty()) {
                    String nextStateName;
                    if (!stateNameMap.containsKey(nextStateSet)) {
                        nextStateName = "q" + stateCounter++;
                        stateNameMap.put(nextStateSet, nextStateName);
                        reverseStateNameMap.put(nextStateName, nextStateSet);
                        dfa.addState(nextStateName);
                        boolean containsFinal = false;
                        for (String state : nextStateSet) {
                            if (finalStates.contains(state)) {
                                containsFinal = true;
                                break;
                            }
                        }
                        if (containsFinal) {
                            dfa.addFinalState(nextStateName);
                        }
                        stateQueue.add(nextStateSet);
                    } else {
                        nextStateName = stateNameMap.get(nextStateSet);
                    }

                    // add transition to the DFA
                    dfa.addTransition(stateNameMap.get(currentStateSet), symbol, nextStateName);
                }
            }
        }

        // store the mapping in the DFA object
        dfa.setStateToNfaStatesMap(reverseStateNameMap);

        return dfa;
    }

    // helper method to get all states reachable from a set of states on a given symbol
    private Set<String> move(Set<String> states, char symbol) {
        Set<String> result = new HashSet<>();

        for (String state : states) {
            if (transitions.containsKey(state) &&
                    transitions.get(state).containsKey(symbol)) {
                result.addAll(transitions.get(state).get(symbol));
            }
        }

        return result;
    }

    // Helper method for epsilon closure
    private Set<String> epsilonClosure(Set<String> states) {
        return new HashSet<>(states);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("States: ").append(states.isEmpty() ? "{}" : states).append("\n");
        sb.append("Alphabet: ").append(alphabet).append("\n");
        sb.append("Transitions: \n");

        for (Map.Entry<String, Map<Character, Set<String>>> entry : transitions.entrySet()) {
            String state = entry.getKey();
            Map<Character, Set<String>> transitionMap = entry.getValue();

            String displayState = state;
            if (stateToNfaStatesMap != null && stateToNfaStatesMap.containsKey(state)) {
                Set<String> originalStates = stateToNfaStatesMap.get(state);
                displayState = originalStates.stream().sorted().collect(Collectors.joining(","));
            }

            for (Map.Entry<Character, Set<String>> trans : transitionMap.entrySet()) {
                char symbol = trans.getKey();
                Set<String> nextStates = trans.getValue();

                sb.append("{").append(displayState).append("}--")
                        .append(symbol).append("-->{");


                String nextStateStr;
                if (nextStates.size() == 1) {
                    String nextState = nextStates.iterator().next();
                    if (stateToNfaStatesMap != null && stateToNfaStatesMap.containsKey(nextState)) {
                        Set<String> originalNextStates = stateToNfaStatesMap.get(nextState);
                        nextStateStr = originalNextStates.stream().sorted().collect(Collectors.joining(","));
                    } else {
                        nextStateStr = nextState;
                    }
                } else {
                    nextStateStr = nextStates.stream().collect(Collectors.joining(", "));
                }

                sb.append(nextStateStr).append("}\n");
            }
        }

        return sb.toString();
    }

    //getters and setters
    public void setAlphabet(Set<Character> alphabet) {
        this.alphabet = alphabet;
    }

    public void setStartState(String startState) {
        this.startState = startState;
    }

    public void setFinalStates(Set<String> finalStates) {
        this.finalStates = finalStates;
    }

    public void setStates(Set<String> states) {
        this.states = states;
    }

    public void setTransitions(Map<String, Map<Character, Set<String>>> transitions) {
        this.transitions = transitions;
    }

    public void setStateToNfaStatesMap(Map<String, Set<String>> stateToNfaStatesMap) {
        this.stateToNfaStatesMap = stateToNfaStatesMap;
    }

    public Set<Character> getAlphabet() {
        return alphabet;
    }

    public String getStartState() {
        return startState;
    }

    public Set<String> getFinalStates() {
        return finalStates;
    }

    public Set<String> getStates() {
        return states;
    }

    public Map<String, Map<Character, Set<String>>> getTransitions() {
        return transitions;
    }

    public Map<String, Set<String>> getStateToNfaStatesMap() {
        return stateToNfaStatesMap;
    }

    //lab1 methods
    public boolean stringBelongToLanguage(String inputString) {
        Set<String> currentStates = new HashSet<>();
        currentStates.add(startState);

        for (char symbol : inputString.toCharArray()) {
            if (!alphabet.contains(symbol)) {
                System.out.println("ERROR: Symbol '" + symbol+ "' not in alphabet!");
                return false;
            }

            Set<String> nextStates = new HashSet<>();
            for (String state : currentStates) {
                Map<Character, Set<String>> stateTransitions = transitions.get(state);
                if (stateTransitions != null && stateTransitions.containsKey(symbol)) {
                    nextStates.addAll(stateTransitions.get(symbol));
                }
            }

            if (nextStates.isEmpty()) {
                System.out.println("No transition found for any state with symbol '" + symbol + "'.");
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