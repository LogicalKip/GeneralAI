package grammar.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import output.FrenchTranslator;

import java.util.function.Predicate;

class TokenizerTest {
    private Tokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new Tokenizer(new FrenchTranslator().getXMLLexicon());
    }


    private void f(Integer x) {
        System.out.println("Integer");
    }

    private void f(int... x) {
        System.out.println("int...");
    }

    private void f(double x) {
        System.out.println("double");
    }

    @Test
    void apostrophe() {
        String[][] s = new String[2][1];
        String s2[][] = new String[2][1];
        try {
            System.out.println("try");
        } catch (Exception e) {
            System.out.println("catch");
        } finally {
            System.out.println("finally");
        }
        Predicate<Integer> predicate = (x) -> x > 0;
//        List<Token> expected = Arrays.asList(
//                new DeterminerToken("l'"),
//                new NounToken("homme"),
//                new VerbToken("regarde"),
//                new DeterminerToken("l'"),
//                new NounToken("oiseau"),
//                new EndOfInputToken("EOI"));
//        assertEquals(expected, tokenizer.tokenize("l'homme regarde l'oiseau  "));
//        assertEquals(expected, tokenizer.tokenize("   l '  homme regarde  l  'oiseau  "));
//        assertEquals(expected, tokenizer.tokenize("l'  homme regarde  l  '  oiseau"));
//
//        expected = Arrays.asList(
//                new MiscToken("le"), // Misc because it can be determiner or pronoun : "le chat" and "il le regarde"
//                new NounToken("chat"),
//                new NeToken("n'"),
//                new VerbToken("est"),
//                new MiscToken("pas"),
//                new PronounToken("quoi"),
//                new QuestionMarkToken("?!?"),
//                new EndOfInputToken("EOI"));
//        assertEquals(expected, tokenizer.tokenize("le chat n'est pas quoi ?!?"));
//        assertEquals(expected, tokenizer.tokenize("le chat n 'est pas quoi ?!?"));
//        assertEquals(expected, tokenizer.tokenize("le chat n'  est pas quoi ?!?"));
    }

}