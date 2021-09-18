package main;

import grammar.entity.Entity;
import grammar.sentence.DeclarativeSentence;
import grammar.sentence.SimpleSentence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import output.FrenchTranslator;
import simplenlg.features.Tense;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

class AITest {
    private AI ai;
    private String lastAIAnswer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        FrenchTranslator translator = Mockito.spy(new FrenchTranslator());
        doAnswer((Answer<Void>) invocationOnMock -> {
            lastAIAnswer = invocationOnMock.getArguments()[0].toString();
            return null;
        }).when(translator).say(anyString());

        ai = new AI(translator);

    }

    @Test
    void referencingConceptDefinedPreviouslyInTheSentence() {
        ai.parseAndProcessSentence("un chat regarde le chat");
        ok();
    }

    @Test
    void catMirror() {
        ai.parseAndProcessSentence("le chat regarde le chat");
        ok();
    }

    @Test
    void catEatsMouse() {
        ai.parseAndProcessSentence("un chat mange une souris");

        // Entities
        assertEquals(2, ai.getEntitiesKnown().size());
        Entity cat = ai.getEntitiesKnown().get(0);
        Entity mouse = ai.getEntitiesKnown().get(1);
        assertNotEquals(cat.getConcept(), mouse.getConcept());

        // Facts
        assertEquals(1, ai.getKnowledge().size());
        for (SimpleSentence simpleSentence : ai.getKnowledge()) {
            assertTrue(simpleSentence instanceof DeclarativeSentence);
            DeclarativeSentence fact = (DeclarativeSentence) simpleSentence;
            assertFalse(fact.isNegative());
            assertFalse(fact.isInterrogative());
            assertEquals(fact.getSubject(), cat);
            assertEquals(fact.getObject(), mouse);
            assertEquals(fact.getTense(), Tense.PRESENT);
        }
        ok();
    }

    @Test
    void functionalTest() {
        ai.parseAndProcessSentence("un petit chat noir mange une souris blanche.");
        ok();
        ai.parseAndProcessSentence("la souris est jolie        ");
        ok();
        ai.parseAndProcessSentence("  qui mange la souris       ?    ");
        assertEquals("Le petit chat noir mangerait la jolie souris blanche, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("la souris mange qui ?");
        assertEquals("Je ne sais pas, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("la souris mange le fromage");
        ok();
        ai.parseAndProcessSentence("une grosse souris mange une pomme");
        ok();
        ai.parseAndProcessSentence("la souris mange quoi ?");
        assertEquals("La grosse souris mangerait la pomme, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("la blanche souris mange quoi ?");
        assertEquals("La jolie souris blanche mangerait le fromage, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("une bête mange un bel oiseau");
        ok();
        ai.parseAndProcessSentence("qui mangerait qui ?");
        answerContainsAll(lastAIAnswer, Arrays.asList("le petit chat noir mangerait la jolie souris blanche", "la jolie souris blanche mangerait le fromage", "la grosse souris mangerait la pomme", "la bête mangerait le bel oiseau"));
        ai.parseAndProcessSentence("un chat signifie une bête");
        ok();
        ai.parseAndProcessSentence("la souris ne mange pas la bête");
        ok();
        ai.parseAndProcessSentence("quoi mange quoi ?");
        answerContainsAll(lastAIAnswer, Arrays.asList("la petite bête noire mangerait la jolie souris blanche", "la jolie souris blanche mangerait le fromage", "la grosse souris mangerait la pomme", "la bête mangerait le bel oiseau"));
        ai.parseAndProcessSentence("quoi ne mange pas quoi ?");
        assertEquals("La jolie souris blanche ne mangerait pas la bête, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("la souris mange le chat ?");
        assertEquals("Non.", lastAIAnswer);
        ai.parseAndProcessSentence("la souris ne mange pas le chat ???");
        assertEquals("En effet.", lastAIAnswer);
        ai.parseAndProcessSentence("qui mangera quoi ?");
        answerContainsAll(lastAIAnswer, Arrays.asList("le petit chat noir mangerait la jolie souris blanche", "la jolie souris blanche mangerait le fromage", "la grosse souris mangerait la pomme", "le chat mangerait le bel oiseau"));
        ai.parseAndProcessSentence("l'oiseau regarde l'araignée");//FIXME test gender unknown or deduced from noun by using lexicon
        ok();
        ai.parseAndProcessSentence("qui regarde qui ?");
        assertEquals("Le bel oiseau regarderait l'araignée, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("je comprends le principe");
        ok();
        ai.parseAndProcessSentence("tu comprends la phrase");
        ok();
        ai.parseAndProcessSentence("qui comprend quoi ?");
        answerContainsAll(lastAIAnswer, Arrays.asList("vous comprendriez le principe", "je comprendrais la phrase"));
        ai.parseAndProcessSentence("explique firefox");
        assertTrue(lastAIAnswer.matches("Mozilla Firefox.*navigateur.*"));
        ai.parseAndProcessSentence("arrête");
        //FIXME add test to start firefox
    }

    private void answerContainsAll(String answer, List<String> facts) {
        Pattern pattern = Pattern.compile("^(?=[A-Z])([^,]+, ){NB_FACTS_FOLLOWED_BY_COMMA}[^,]+ et [^,]+, monsieur\\.$".replaceAll("NB_FACTS_FOLLOWED_BY_COMMA", String.valueOf(facts.size() - 2)), CASE_INSENSITIVE);// starts with caps, contains N - 2 strings separated by commas, then 2 strings separated by "et" and the final ", monsieur".
        assertTrue(pattern.matcher(answer).find());
        for (String fact : facts) {
            assertTrue(answer.toLowerCase().contains(fact.toLowerCase()));
        }
    }

    private void ok() {
        assertEquals("Compris.", lastAIAnswer);
    }
}