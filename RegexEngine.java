import java.util.ArrayList;
import java.util.List;

public class RegexEngine {
    static int nextStateId = 0;
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
            states.add(state);
        }

        void addTransition(State from, State to, Character symbol) {
            transitions.add(new Transition(from, to, symbol));
        }
    }

    static NFA createLiteralNFA(char symbol) {
        State start = new State(0, false);
        State accept = new State(1, true);

        NFA nfa = new NFA(start, accept);

        nfa.addTransition(start, accept, symbol);

        return nfa;
    }

    static NFA concatenate(NFA first, NFA second) {
        first.accept.accepting = false;
        NFA result = new NFA(first.start, second.accept);

        result.states.clear();
        result.states.addAll(first.states);
        result.states.addAll(second.states);
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
    
        int alternationIndex = regex.indexOf('|');

        if (alternationIndex != -1) {
            String left = regex.substring(0, alternationIndex);
            String right = regex.substring(alternationIndex + 1);
    
            if (left.length() == 0 || right.length() == 0) {
                throw new IllegalArgumentException(
                        "Alternation must have an expression on both sides.");
            }
    
            NFA leftNFA = buildBasicNFA(left);
            NFA rightNFA = buildBasicNFA(right);
            return alternate(leftNFA, rightNFA);
        }
    
        NFA result = null;
        int i = 0;

        while (i < regex.length()) {
            char symbol = regex.charAt(i);
            NFA current = createLiteralNFA(symbol);

        if (i + 1 < regex.length()
            && regex.charAt(i + 1) == '*') {
            current = star(current);
            i += 2;
        
        } else if (i + 1 < regex.length()
                && regex.charAt(i + 1) == '+') {
        
            current = plus(current);
            i += 2;
        
        } else {
            i++;
        }
    
            if (result == null) {
                result = current;
            } else {
                result = concatenate(result, current);
            }
        }
        return result;
    }
    
    static NFA alternate(NFA first, NFA second) {
        State start = new State(0, false);
        State accept = new State(1, true);
    
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

    public static void main(String[] args) {
        NFA nfa = buildBasicNFA("a+");
    
        System.out.println("Start: " + nfa.start);
        System.out.println("Accept: " + nfa.accept);
    
        System.out.println("States:");
    
        for (State state : nfa.states) {
            System.out.println("  " + state);
        }
    
        System.out.println("Transitions:");
    
        for (Transition transition : nfa.transitions) {
            System.out.println("  " + transition);
        }
    }
}