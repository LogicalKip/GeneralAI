package util;

import grammar.AbstractConcept;
import grammar.Adjective;
import grammar.Designation;
import grammar.verb.Verb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VocabRetrieverTest {
    private static List<Designation> vocabulary;
    private static AbstractConcept c2;
    private static AbstractConcept c1;

    @BeforeAll
    static void setup() {
        c1 = new Adjective();
        c2 = new Verb(false, false);
        AbstractConcept c3 = new Adjective();
        AbstractConcept c4 = new Verb(false, false);
        vocabulary = List.of(
                new Designation("canMeanTwoThings", c1),
                new Designation("normalCase", c2),
                new Designation("d3", c3),
                new Designation("secondDesignation", c1),
                new Designation("canMeanTwoThings", c4),
                new Designation("d4", c4));
    }

    @Test
    void getFirstConceptDesignatedBy() {
        assertEquals(c1, VocabRetriever.getFirstConceptDesignatedBy(vocabulary, "canMeanTwoThings", AbstractConcept.class).get());
        assertEquals(c2, VocabRetriever.getFirstConceptDesignatedBy(vocabulary, "normalCase", AbstractConcept.class).get());
        assertEquals(c1, VocabRetriever.getFirstConceptDesignatedBy(vocabulary, "secondDesignation", AbstractConcept.class).get());
        assertEquals(Optional.empty(), VocabRetriever.getFirstConceptDesignatedBy(vocabulary, "nonexistent", AbstractConcept.class));
    }

    @Test
    void getFirstDesignationFrom() {
        Designation t1 = VocabRetriever.getFirstDesignationFrom(vocabulary, "canMeanTwoThings", AbstractConcept.class).get();
        assertEquals(c1, t1.getDesignatedConcept());
        assertEquals("canMeanTwoThings", t1.getValue());
        Designation t2 = VocabRetriever.getFirstDesignationFrom(vocabulary, "normalCase", AbstractConcept.class).get();
        assertEquals(c2, t2.getDesignatedConcept());
        assertEquals("normalCase", t2.getValue());
        Designation t3 = VocabRetriever.getFirstDesignationFrom(vocabulary, "secondDesignation", AbstractConcept.class).get();
        assertEquals("secondDesignation", t3.getValue());
        assertEquals(Optional.empty(), VocabRetriever.getFirstDesignationFrom(vocabulary, "nonexistent", AbstractConcept.class));
    }
}