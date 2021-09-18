package grammar.rules;

import exceptions.CantFindSuchAnEntityException;
import exceptions.WrongGrammarRuleException;
import grammar.Adjective;
import grammar.Designation;
import grammar.EntityHandler;
import grammar.FrenchGrammar;
import grammar.determiner.Determiner;
import simplenlg.framework.LexicalCategory;
import util.VocabRetriever;

import java.rmi.UnexpectedException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static grammar.FrenchGrammar.getBase;
import static grammar.FrenchGrammar.getUpdatedVocabulary;
import static grammar.rules.GrammarUtils.canBeAdjective;
import static grammar.rules.GrammarUtils.canBeDeterminer;
import static grammar.rules.GrammarUtils.canBeNoun;

/**
 * nominal group = determiner (adjective)* noun (adjective)*
 */
public class NominalGroupRule extends GrammarRule {
    @Override
    protected Object internalApply() throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {//FIXME add test for adjective unknown until then
        if (!canBeDeterminer(currentToken)) {
            fail();
        }

        String determinerString = currentToken.getOriginalString();
        Optional<Determiner> determiner = VocabRetriever.getFirstConceptDesignatedBy(getUpdatedVocabulary(), determinerString, Determiner.class);
        assert determiner.isPresent(); //FIXME could be a determiner from the xml but not in vocab, should it really crash for a random user ? ask for a MR ?

        nextToken();

        Set<String> adjectivesStrings = goThroughContiguousAdjectives(); //FIXME test with same adjective twice

        if (!canBeNoun(currentToken)) {
            fail();
        }

        String noun = currentToken.getOriginalString();
        nextToken();

        adjectivesStrings.addAll(goThroughContiguousAdjectives());

        Set<Adjective> adjectives = new HashSet<>();
        for (String s : adjectivesStrings) {
            String adjectiveBase = getBase(s, LexicalCategory.ADJECTIVE);
            Optional<Adjective> adjectiveConcept = VocabRetriever.getFirstConceptDesignatedBy(getUpdatedVocabulary(), adjectiveBase, Adjective.class);
            if (adjectiveConcept.isEmpty()) {
                adjectiveConcept = Optional.of(new Adjective());
                FrenchGrammar.getNewVocabulary().add(new Designation(adjectiveBase, adjectiveConcept.get()));
            }
            adjectives.add(adjectiveConcept.get());
        }
        return new EntityHandler().processCorrespondingEntity(determiner.get(), adjectives, noun);
    }


    /**
     * Starting with current token, as long as the tokens can be adjectives, add their String to the set.
     * Method ends on the next token, i.e. the first non-adjective token found.
     * There can be 0 adjectives.
     */
    private Set<String> goThroughContiguousAdjectives() throws WrongGrammarRuleException {
        Set<String> res = new HashSet<>();

        while (canBeAdjective(currentToken)) {
            res.add(currentToken.getOriginalString());
            nextToken();
        }

        return res;
    }
}
