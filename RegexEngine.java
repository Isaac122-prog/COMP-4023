import java.util.ArrayList;
import java.util.List;

public class RegexEngine {
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

    static NFA buildBasicNFA(String regex) {
        if (regex.length() != 1) {
            throw new IllegalArgumentException(
                    "Basic regex must contain exactly one character.");
        }

        return createLiteralNFA(regex.charAt(0));
    }

    public static void main(String[] args) {

        NFA nfa = buildBasicNFA("a");

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