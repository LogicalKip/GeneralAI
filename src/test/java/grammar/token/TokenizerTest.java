package grammar.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import output.FrenchTranslator;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {
    private Tokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new Tokenizer(new FrenchTranslator().getXMLLexicon());
    }

    @Test
    void apostrophe() {
        List<Token> expected = Arrays.asList(
                new DeterminerToken("l'"),
                new NounToken("homme"),
                new VerbToken("regarde"),
                new DeterminerToken("l'"),
                new NounToken("oiseau"));
        assertEquals(expected, tokenizer.tokenize("l'homme regarde l'oiseau  "));
        assertEquals(expected, tokenizer.tokenize("   l '  homme regarde  l  'oiseau  "));
        assertEquals(expected, tokenizer.tokenize("l'  homme regarde  l  '  oiseau"));

        expected = Arrays.asList(
                new MiscToken("le"), // Misc because it can be determiner or pronoun : "le chat" and "il le regarde"
                new NounToken("chat"),
                new NeToken("n'"),
                new VerbToken("est"),
                new MiscToken("pas"),
                new PronounToken("quoi"),
                new QuestionMarkToken("?!?"));
        assertEquals(expected, tokenizer.tokenize("le chat n'est pas quoi ?!?"));
        assertEquals(expected, tokenizer.tokenize("le chat n 'est pas quoi ?!?"));
        assertEquals(expected, tokenizer.tokenize("le chat n'  est pas quoi ?!?"));
    }
}