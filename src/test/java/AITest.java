import grammar.entity.Entity;
import grammar.sentence.DeclarativeSentence;
import main.AI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simplenlg.features.Tense;

import static org.junit.jupiter.api.Assertions.*;

class AITest {
    private AI ai;


    @BeforeEach
    void setup() {
//        MockitoAnnotations.initMocks(this);
        ai = new AI();
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
    }
}