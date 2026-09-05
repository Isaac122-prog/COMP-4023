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
}