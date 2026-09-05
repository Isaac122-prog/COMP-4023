import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RegexEngine {
    static int nextStateId = 0;

    static List<State> epsilonClosure(List<State> states, NFA nfa) {
        List<State> closure = new ArrayList<>();
        List<State> stack = new ArrayList<>();
    
        for (State state : states) {
            if (!closure.contains(state)) {
                closure.add(state);
                stack.add(state);
            }
        }
    
        while (!stack.isEmpty()) {
            State current = stack.remove(stack.size() - 1);
            for (Transition transition : nfa.transitions) {
                if (transition.from == current
                        && transition.symbol == null) {
                    State next = transition.to;

                    if (!closure.contains(next)) {
                        closure.add(next);
                        stack.add(next);
                    }
                }
            }
        }
        return closure;
    }

    static List<State> move(List<State> states, char symbol, NFA nfa) {
        List<State> result = new ArrayList<>();
        for (State state : states) {
            for (Transition transition : nfa.transitions) {
                if (transition.from == state && transition.symbol != null && transition.symbol == symbol) {
                    if (!result.contains(transition.to)) {
                        result.add(transition.to);
                    }
                }
            }
        }
        return result;
    }

    static boolean matches(NFA nfa, String input) {
        List<State> currentStates = new ArrayList<>();
        currentStates.add(nfa.start);
        currentStates = epsilonClosure(currentStates, nfa);
        
        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            currentStates = move(currentStates, symbol, nfa);
            currentStates = epsilonClosure(currentStates, nfa);
            
            if (currentStates.isEmpty()) {
                return false;
            }
        }
        
        for (State state : currentStates) {
            if (state == nfa.accept) {
                return true;
            }
        }        
        return false;
    }

    static class State {
        int id;
        boolean accepting;

        State(int id, boolean accepting) {
            this.id = id;
            this.accepting = accepting;
        }

        @Override
        public String toString() {
            if (accepting) {
                return "*q" + id;
            }

            return "q" + id;
        }
    }

    static class Transition {
        State from;
        State to;
        Character symbol;

        Transition(State from, State to, Character symbol) {
            this.from = from;
            this.to = to;
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            String symbolText;

            if (symbol == null) {
                symbolText = "ε";
            } else {
                symbolText = symbol.toString();
            }

            return from + " --" + symbolText + "--> " + to;
        }
    }

    static class NFA {
        State start;
        State accept;
        List<State> states;
        List<Transition> transitions;

        NFA(State start, State accept) {
            this.start = start;
            this.accept = accept;

            this.states = new ArrayList<>();
            this.transitions = new ArrayList<>();

            states.add(start);
            states.add(accept);
        }

        void addState(State state) {
            if (!states.contains(state)) {
                states.add(state);
            }
        }

        void addTransition(State from, State to, Character symbol) {
            transitions.add(new Transition(from, to, symbol));
        }
    }

    static NFA createLiteralNFA(char symbol) {
        State start = new State(nextStateId++, false);
        State accept = new State(nextStateId++, true);
    
        NFA nfa = new NFA(start, accept);
    
        nfa.addTransition(start, accept, symbol);
    
        return nfa;
    }

    static NFA concatenate(NFA first, NFA second) {
        first.accept.accepting = false;
        NFA result = new NFA(first.start, second.accept);

        result.states.clear();

        for (State state : first.states) {
            result.addState(state);
        }
        
        for (State state : second.states) {
            result.addState(state);
        }

        result.transitions.addAll(first.transitions);
        result.addTransition(first.accept, second.start, null);
        result.transitions.addAll(second.transitions);

        return result;
    }

    static NFA buildBasicNFA(String regex) {
        if (regex.length() == 0) {
            throw new IllegalArgumentException(
                    "Regex cannot be empty.");
        }
    
        Parser parser = new Parser(regex);
        return parser.parse();
    }
    
    static NFA alternate(NFA first, NFA second) {
        State start = new State(nextStateId++, false);
        State accept = new State(nextStateId++, true);
    
        NFA result = new NFA(start, accept);
        first.accept.accepting = false;
        second.accept.accepting = false;
    
        result.states.clear();
        result.states.add(start);
        result.states.addAll(first.states);
        result.states.addAll(second.states);
        result.states.add(accept);
        result.transitions.addAll(first.transitions);
        result.transitions.addAll(second.transitions);
        result.addTransition(start, first.start, null);
        result.addTransition(start, second.start, null);
        result.addTransition(first.accept, accept, null);
        result.addTransition(second.accept, accept, null);
    
        return result;
    }

    static NFA star(NFA original) {
        State start = new State(nextStateId++, false);
        State accept = new State(nextStateId++, true);
    
        original.accept.accepting = false;
        NFA result = new NFA(start, accept);
    
        result.states.clear();
        result.states.add(start);
        result.states.addAll(original.states);
        result.states.add(accept);
    
        result.transitions.addAll(original.transitions);
    
        result.addTransition(start, accept, null);
        result.addTransition(start, original.start, null);
        result.addTransition(original.accept, accept, null);
        result.addTransition(original.accept, original.start, null);
    
        return result;
    }

    static NFA plus(NFA original) {
        State start = new State(nextStateId++, false);
        State accept = new State(nextStateId++, true);
    
        original.accept.accepting = false;
        NFA result = new NFA(start, accept);
    
        result.states.clear();
        result.states.add(start);
        result.states.addAll(original.states);
        result.states.add(accept);
        result.transitions.addAll(original.transitions);
        result.addTransition(start, original.start, null);
        result.addTransition(original.accept, accept, null);
        result.addTransition(original.accept, original.start, null);
    
        return result;
    }

    static class Parser {
        String regex;
        int position;
    
        Parser(String regex) {
            this.regex = regex;
            this.position = 0;
        }
    
        NFA parse() {
            NFA result = parseAlternation();

            if (position != regex.length()) {
                throw new IllegalArgumentException(
                        "Unexpected character: " + regex.charAt(position));
            }
            return result;
        }
    
        NFA parseAlternation() {
            NFA result = parseConcatenation();
    
            while (position < regex.length() && regex.charAt(position) == '|') {
                position++;
                NFA right = parseConcatenation();
                result = alternate(result, right);
            }
            return result;
        }
    
        NFA parseConcatenation() {
            NFA result = null;

            while (position < regex.length()) {
                char current = regex.charAt(position);
    
                if (current == '|' || current == ')') {
                    break;
                }
    
                NFA next = parseRepetition();
    
                if (result == null) {
                    result = next;
                } else {
                    result = concatenate(result, next);
                }
            }
    
            if (result == null) {
                throw new IllegalArgumentException(
                        "Expected an expression.");
            }
            return result;
        }
    
        NFA parseRepetition() {
            NFA result = parseCharacter();
    
            while (position < regex.length()) {
                char current = regex.charAt(position);
    
                if (current == '*') {
                    result = star(result);
                    position++;
    
                } else if (current == '+') {
                    result = plus(result);
                    position++;
    
                } else {
                    break;
                }
            }
            return result;
        }
    
        NFA parseCharacter() {
            if (position >= regex.length()) {
                throw new IllegalArgumentException(
                        "Expected an expression.");
            }
        
            char current = regex.charAt(position);
        
            if (current == '(') {
                position++;
                NFA result = parseAlternation();
        
                if (position >= regex.length()
                        || regex.charAt(position) != ')') {
                    throw new IllegalArgumentException(
                            "Missing closing parenthesis.");
                }
                position++;
                return result;
            }
        
            if (current == ')' || current == '|' ||
                    current == '*' || current == '+') {
                throw new IllegalArgumentException(
                        "Unexpected operator: " + current);
            }
            position++;
            return createLiteralNFA(current);
        }
    }

    static boolean matchesVerbose(NFA nfa, String input) {
        List<State> currentStates = new ArrayList<>();
        currentStates.add(nfa.start);
        currentStates = epsilonClosure(currentStates, nfa);
        System.out.println("Initial states: " + currentStates);
    
        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            currentStates = move(currentStates, symbol, nfa);
            currentStates = epsilonClosure(currentStates, nfa);
            System.out.println(
                    "After '" + symbol + "': " + currentStates);
    
            if (currentStates.isEmpty()) {
                System.out.println("No states remain.");
                return false;
            }
        }
    
        for (State state : currentStates) {
            if (state == nfa.accept) {
                return true;
            }
        }
        return false;
    }

    static void printNFA(NFA nfa) {
        System.out.println("ε-NFA");
        for (State state : nfa.states) {
            System.out.println(state);
        }

        System.out.println("Transitions");
        for (Transition transition : nfa.transitions) {
            System.out.println(transition);
        }
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean verbose = false;
    
        if (args.length > 0 && args[0].equals("-v")) {
            verbose = true;
        }
    
        String regex = reader.readLine();
    
        if (regex == null || regex.length() == 0) {
            throw new IllegalArgumentException(
                    "Regex cannot be empty.");
        }
    
        NFA nfa = buildBasicNFA(regex);
    
        if (verbose) {
            printNFA(nfa);
        }
    
        System.out.println("ready");
        String input;
    
        while ((input = reader.readLine()) != null) {
            boolean result;
    
            if (verbose) {
                result = matchesVerbose(nfa, input);
            } else {
                result = matches(nfa, input);
            }
            System.out.println(result);
        }
    }
}