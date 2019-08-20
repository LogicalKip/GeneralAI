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
    void apostrophe() {//FIXME handle "l ' oiseau" ?? and "n ' est"
        List<Token> expected = Arrays.asList(
                new DeterminerToken("l'"),
                new NounToken("homme"),
                new VerbToken("regarde"),
                new DeterminerToken("l'"),
                new NounToken("oiseau"));
        List<Token> actual = tokenizer.tokenize("   l'  homme regarde  l'oiseau  ");
        assertEquals(expected, actual);

        actual = tokenizer.tokenize("le chat n'est pas quoi ?!?");
        expected = Arrays.asList(
                new MiscToken("le"), // Misc because it can be determiner or pronoun : "le chat" and "il le regarde"
                new NounToken("chat"),
                new NeToken("n'"),
                new VerbToken("est"),
                new MiscToken("pas"),
                new PronounToken("quoi"),
                new QuestionMarkToken("?!?"));
        assertEquals(expected, actual);
    }
}