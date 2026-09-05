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

    @Test
    public void testParentheses() {
        RegexEngine.nextStateId = 0;

        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("(ab)");

        assertTrue(RegexEngine.matches(nfa, "ab"));
        assertFalse(RegexEngine.matches(nfa, "a"));
        assertFalse(RegexEngine.matches(nfa, "b"));
        assertFalse(RegexEngine.matches(nfa, "abc"));
    }

    @Test
    public void testParenthesesWithStar() {
        RegexEngine.nextStateId = 0;

        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("(ab)*");

        assertTrue(RegexEngine.matches(nfa, ""));
        assertTrue(RegexEngine.matches(nfa, "ab"));
        assertTrue(RegexEngine.matches(nfa, "abab"));
        assertTrue(RegexEngine.matches(nfa, "ababab"));

        assertFalse(RegexEngine.matches(nfa, "a"));
        assertFalse(RegexEngine.matches(nfa, "b"));
        assertFalse(RegexEngine.matches(nfa, "aba"));
    }

    @Test
    public void testParenthesesWithPlus() {
        RegexEngine.nextStateId = 0;

        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("(ab)+");

        assertFalse(RegexEngine.matches(nfa, ""));
        assertTrue(RegexEngine.matches(nfa, "ab"));
        assertTrue(RegexEngine.matches(nfa, "abab"));
        assertTrue(RegexEngine.matches(nfa, "ababab"));

        assertFalse(RegexEngine.matches(nfa, "a"));
        assertFalse(RegexEngine.matches(nfa, "aba"));
    }

    @Test
    public void testAlternationWithConcatenation() {
        RegexEngine.nextStateId = 0;

        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("a|bc");

        assertTrue(RegexEngine.matches(nfa, "a"));
        assertTrue(RegexEngine.matches(nfa, "bc"));

        assertFalse(RegexEngine.matches(nfa, "b"));
        assertFalse(RegexEngine.matches(nfa, "abc"));
        assertFalse(RegexEngine.matches(nfa, "ac"));
    }

    @Test
    public void testConcatenationWithAlternation() {
        RegexEngine.nextStateId = 0;

        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("(a|b)c");

        assertTrue(RegexEngine.matches(nfa, "ac"));
        assertTrue(RegexEngine.matches(nfa, "bc"));

        assertFalse(RegexEngine.matches(nfa, "a"));
        assertFalse(RegexEngine.matches(nfa, "b"));
        assertFalse(RegexEngine.matches(nfa, "abc"));
    }

    @Test
    public void testAssignmentExample() {
        RegexEngine.nextStateId = 0;

        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("(ab)*|c+");

        assertTrue(RegexEngine.matches(nfa, ""));
        assertTrue(RegexEngine.matches(nfa, "ab"));
        assertTrue(RegexEngine.matches(nfa, "abab"));
        assertTrue(RegexEngine.matches(nfa, "c"));
        assertTrue(RegexEngine.matches(nfa, "cc"));
        assertTrue(RegexEngine.matches(nfa, "ccc"));

        assertFalse(RegexEngine.matches(nfa, "a"));
        assertFalse(RegexEngine.matches(nfa, "b"));
        assertFalse(RegexEngine.matches(nfa, "abc"));
        assertFalse(RegexEngine.matches(nfa, "ac"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyRegex() {
        RegexEngine.buildBasicNFA("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRegex() {
        RegexEngine.buildBasicNFA(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnexpectedClosingParenthesis() {
        RegexEngine.buildBasicNFA("a)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingClosingParenthesis() {
        RegexEngine.buildBasicNFA("(ab");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnexpectedStar() {
        RegexEngine.buildBasicNFA("*a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnexpectedPlus() {
        RegexEngine.buildBasicNFA("+a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAlternationAtStart() {
        RegexEngine.buildBasicNFA("|a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAlternationAtEnd() {
        RegexEngine.buildBasicNFA("a|");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyParentheses() {
        RegexEngine.buildBasicNFA("()");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperatorAfterOpeningParenthesis() {
        RegexEngine.buildBasicNFA("(*a)");
    }

    
    @Test
    public void testUppercaseLetters() {
        RegexEngine.nextStateId = 0;
    
        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("ABC");
        assertTrue(RegexEngine.matches(nfa, "ABC"));
        assertFalse(RegexEngine.matches(nfa, "abc"));
    }

    @Test
    public void testNumbers() {
        RegexEngine.nextStateId = 0;
        
        RegexEngine.NFA nfa = RegexEngine.buildBasicNFA("123");
        assertTrue(RegexEngine.matches(nfa, "123"));
        assertFalse(RegexEngine.matches(nfa, "12"));
        assertFalse(RegexEngine.matches(nfa, "1234"));
    }
}