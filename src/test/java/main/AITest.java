package main;

import grammar.entity.Entity;
import grammar.sentence.DeclarativeSentence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import output.FrenchTranslator;
import simplenlg.features.Tense;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.anyString;
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
    void catEatsMouse() {
        ai.parseAndProcessSentence("un chat mange une souris");

        // Entities
        assertEquals(2, ai.getEntitiesKnown().size());
        Entity cat = ai.getEntitiesKnown().get(0);
        Entity mouse = ai.getEntitiesKnown().get(1);
        assertNotEquals(cat.getConcept(), mouse.getConcept());

        // Facts
        assertEquals(1, ai.getKnowledge().size());
        assertTrue(ai.getKnowledge().get(0) instanceof DeclarativeSentence);
        DeclarativeSentence fact = (DeclarativeSentence) ai.getKnowledge().get(0);
        assertFalse(fact.isNegative());
        assertFalse(fact.isInterrogative());
        assertEquals(fact.getSubject(), cat);
        assertEquals(fact.getObject(), mouse);
        assertEquals(fact.getTense(), Tense.PRESENT);

        ok();
    }

    @Test
    void functionalTest() {
        ai.parseAndProcessSentence("un petit chat noir mange une souris blanche.");
        ok();
        ai.parseAndProcessSentence("la souris est jolie        ");
        ok();
        ai.parseAndProcessSentence("qui mange la souris       ?    ");
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
        assertEquals("Le petit chat noir mangerait la jolie souris blanche, la jolie souris blanche mangerait le fromage, la grosse souris mangerait la pomme et la bête mangerait le bel oiseau, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("un chat signifie une bête");
        ok();
        ai.parseAndProcessSentence("la souris ne mange pas la bête");
        ok();
        ai.parseAndProcessSentence("quoi mange quoi ?");
        assertEquals("La petite bête noire mangerait la jolie souris blanche, la jolie souris blanche mangerait le fromage, la grosse souris mangerait la pomme et la bête mangerait le bel oiseau, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("quoi ne mange pas quoi ?");
        assertEquals("La jolie souris blanche ne mangerait pas la bête, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("la souris mange le chat ?");
        assertEquals("Non.", lastAIAnswer);
        ai.parseAndProcessSentence("la souris ne mange pas le chat ???");
        assertEquals("En effet.", lastAIAnswer);
        ai.parseAndProcessSentence("qui mangera quoi ?");
        assertEquals("Le petit chat noir mangerait la jolie souris blanche, la jolie souris blanche mangerait le fromage, la grosse souris mangerait la pomme et le chat mangerait le bel oiseau, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("l'oiseau regarde l'araignée");//FIXME test gender unknown or deduced from noun by using lexicon
        ok();
        ai.parseAndProcessSentence("qui regarde qui ?");
        assertEquals("Le bel oiseau regarderait l'araignée, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("je comprends le principe");
        ok();
        ai.parseAndProcessSentence("tu comprends la phrase");
        ok();
        ai.parseAndProcessSentence("qui comprend quoi ?");
        assertEquals("Vous comprendriez le principe et je comprendrais la phrase, monsieur.", lastAIAnswer);
        ai.parseAndProcessSentence("explique firefox");
        assertEquals("Mozilla Firefox [mɒˈzɪlə ˈfaɪɚfɑks] est un navigateur web libre et gratuit, développé et distribué par la Mozilla Foundation avec l'aide de milliers de bénévoles, grâce aux méthodes de développement du logiciel libre/open source et à la liberté du code source.", lastAIAnswer);
    }

    private void ok() {
        assertEquals("Compris.", lastAIAnswer);
    }
}