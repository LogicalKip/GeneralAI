package grammar.rules;

import exceptions.CantFindSuchAnEntityException;
import exceptions.WrongGrammarRuleException;
import grammar.Adjective;
import grammar.Designation;
import grammar.FrenchGrammar;
import grammar.entity.EntityInterrogative;
import grammar.entity.IEntity;
import grammar.sentence.DeclarativeSentence;
import grammar.sentence.SimpleSentence;
import grammar.sentence.StativeSentence;
import grammar.token.NeToken;
import grammar.token.QuestionMarkToken;
import grammar.verb.Verb;
import simplenlg.framework.LexicalCategory;
import util.VocabRetriever;

import java.rmi.UnexpectedException;
import java.util.Optional;

import static grammar.FrenchGrammar.getBase;
import static grammar.FrenchGrammar.getUpdatedVocabulary;
import static grammar.rules.GrammarUtils.canBeAdjective;
import static grammar.rules.GrammarUtils.canBePas;
import static grammar.rules.GrammarUtils.canBeVerb;
import static grammar.rules.GrammarUtils.isKnownAdjective;
import static grammar.rules.GrammarUtils.isKnownVerb;

/**
 * A simple sentence with a subject, verb and object, such as "a cat eats a mouse", or "I see what ?"
 */
public class SimpleSentenceRule extends GrammarRule {

    @Override
    protected Object internalApply() throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {

        boolean sentenceIsNegated = false;
        boolean sentenceIsInterrogative = false;

        IEntity subject = entity();


        if (subject instanceof EntityInterrogative) {
            sentenceIsInterrogative = true;
        }

        if (currentToken instanceof NeToken) {
            sentenceIsNegated = true;
            nextToken();
        }

        if (!canBeVerb(currentToken)) {
            fail();
        }

        String verbString = currentToken.getOriginalString();
        nextToken();

        if (canBePas(currentToken)) {
            sentenceIsNegated = true;
            nextToken();
        }

        Verb verb = processCorrespondingVerb(verbString);
        SimpleSentence sentence;

        if (verb.IS_STATIVE_VERB) {
            sentence = handleStativeVerb(subject, verb);
        } else {
            IEntity object = entity();
            sentence = new DeclarativeSentence(subject, verb, object);
            if (object instanceof EntityInterrogative) {
                sentenceIsInterrogative = true;
            }
        }

        if (currentToken instanceof QuestionMarkToken) {
            nextToken();
            sentenceIsInterrogative = true;
        }

        sentence.setNegative(sentenceIsNegated);
        sentence.setInterrogative(sentenceIsInterrogative);

        return sentence;
    }

    private SimpleSentence handleStativeVerb(IEntity subject, Verb verb) throws WrongGrammarRuleException {
        SimpleSentence sentence;
        if (!canBeAdjective(currentToken)) {
            fail();
        }
        sentence = new StativeSentence(subject, verb, processCorrespondingAdjective(currentToken.getOriginalString()));
        nextToken();
        return sentence;
    }

    /**
     * Something where an entity is expected, i.e who/what, or a nominal group, or even "you"
     */
    private IEntity entity() throws CantFindSuchAnEntityException, WrongGrammarRuleException, UnexpectedException {
        IEntity res;
        try {
            res = (IEntity) new NominalGroupRule().apply(this);
        } catch (WrongGrammarRuleException e) {
            res = (IEntity) new PronounRule().apply(this);
        }
        return res;
    }


    /**
     * Returns the concept corresponding to the parameter (it is implied that it represents a verb) if it's in the AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
     */
    private Verb processCorrespondingVerb(String verb) {
        String base = isKnownVerb(verb) ? getBase(verb, LexicalCategory.VERB) : verb;

        Optional<Designation> designation = VocabRetriever.getFirstDesignationFrom(getUpdatedVocabulary(), base, Verb.class);
        return designation
                .map(x -> {
                    x.incrementTimesUserUsedIt();
                    return (Verb) x.getDesignatedConcept();
                })
                .orElseGet(() -> handleNewVerb(base));//FIXME UT to test that it's not orElse() (check that no side effect in cases where the orElse isn't needed)
    }

    private Verb handleNewVerb(String designation) {
        Verb newConcept = new Verb();
        Designation newDesignation = new Designation(designation, newConcept);
        newDesignation.incrementTimesUserUsedIt();
        FrenchGrammar.getNewVocabulary().add(newDesignation);
        return newConcept;
    }

    /**
     * Returns the concept corresponding to the parameter (it is implied that it represents an adjective) if it's in the AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
     */
    private Adjective processCorrespondingAdjective(String adjective) {
        String base = isKnownAdjective(adjective) ? getBase(adjective, LexicalCategory.ADJECTIVE) : adjective;

        Optional<Designation> designation = VocabRetriever.getFirstDesignationFrom(getUpdatedVocabulary(), base, Adjective.class);

        return designation
                .map(x -> ((Adjective) x.getDesignatedConcept()))
                .orElseGet(() -> handleNewAdjective(base));//FIXME UT to test that it's not orElse() (check that no side effect in cases where the orElse isn't needed)
    }

    private Adjective handleNewAdjective(String base) {
        Adjective newConcept = new Adjective();
        FrenchGrammar.getNewVocabulary().add(new Designation(base, newConcept));
        return newConcept;
    }

}
