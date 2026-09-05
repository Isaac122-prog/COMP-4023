import static org.junit.Assert.*;
import org.junit.Test;

public class RegexEngine_Test {

    @Test
    public void testLiteralExpression() {
        RegexEngine.nextStateId = 0;
        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("a");

        assertTrue(RegexEngine.matches(nfa, "a"));
        assertFalse(RegexEngine.matches(nfa, "b"));
        assertFalse(RegexEngine.matches(nfa, ""));
        assertFalse(RegexEngine.matches(nfa, "aa"));
    }

    @Test
    public void testConcatenation() {
        RegexEngine.nextStateId = 0;
        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("ab");

        assertTrue(RegexEngine.matches(nfa, "ab"));
        assertFalse(RegexEngine.matches(nfa, "a"));
        assertFalse(RegexEngine.matches(nfa, "b"));
        assertFalse(RegexEngine.matches(nfa, ""));
        assertFalse(RegexEngine.matches(nfa, "abc"));
    }

    @Test
    public void testAlternation() {
        RegexEngine.nextStateId = 0;
        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("a|b");

        assertTrue(RegexEngine.matches(nfa, "a"));
        assertTrue(RegexEngine.matches(nfa, "b"));
        assertFalse(RegexEngine.matches(nfa, "c"));
        assertFalse(RegexEngine.matches(nfa, "ab"));
        assertFalse(RegexEngine.matches(nfa, ""));
    }

    @Test
    public void testKleeneStar() {
        RegexEngine.nextStateId = 0;
        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("a*");

        assertTrue(RegexEngine.matches(nfa, ""));
        assertTrue(RegexEngine.matches(nfa, "a"));
        assertTrue(RegexEngine.matches(nfa, "aa"));
        assertTrue(RegexEngine.matches(nfa, "aaa"));
        assertFalse(RegexEngine.matches(nfa, "b"));
        assertFalse(RegexEngine.matches(nfa, "ab"));
    }

    @Test
    public void testKleenePlus() {
        RegexEngine.nextStateId = 0;
        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("a+");

        assertFalse(RegexEngine.matches(nfa, ""));
        assertTrue(RegexEngine.matches(nfa, "a"));
        assertTrue(RegexEngine.matches(nfa, "aa"));
        assertTrue(RegexEngine.matches(nfa, "aaa"));
        assertFalse(RegexEngine.matches(nfa, "b"));
        assertFalse(RegexEngine.matches(nfa, "ab"));
    }
}