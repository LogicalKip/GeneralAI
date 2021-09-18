package grammar.rules;

import exceptions.CantFindSuchAnEntityException;
import exceptions.WrongGrammarRuleException;
import grammar.FrenchGrammar;
import grammar.entity.AbstractEntityConcept;
import grammar.entity.Entity;
import grammar.entity.EntityInterrogative;
import grammar.entity.IEntity;
import grammar.entity.Myself;
import grammar.entity.User;
import output.Translator;
import simplenlg.framework.LexicalCategory;
import util.VocabRetriever;

import java.rmi.UnexpectedException;
import java.util.Optional;

import static grammar.FrenchGrammar.getBase;
import static grammar.FrenchGrammar.getUpdatedVocabulary;
import static grammar.rules.GrammarUtils.canBePronoun;

/**
 * For one personal pronoun (I, you) or interrogative pronoun (who)
 */
public class PronounRule extends GrammarRule {
    @Override
    protected Object internalApply() throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {
        if (!canBePronoun(currentToken)) {
            fail();
        }
        IEntity res;
        try {
            res = processCorrespondingEntityInterrogative(currentToken.getOriginalString());
        } catch (WrongGrammarRuleException e1) {
            res = personalPronoun();
        }
        nextToken();
        return res;
    }


    private IEntity personalPronoun() throws WrongGrammarRuleException {
        return getEntityFromPronoun(currentToken.getOriginalString())
                .orElseThrow(WrongGrammarRuleException::new);
    }

    private Optional<Entity> getEntityFromPronoun(String pronoun) {
        if (isSecondSingularPersonalPronoun(pronoun)) {
            return Optional.of(Myself.getInstance());
        } else if (isFirstSingularPersonalPronoun(pronoun)) {
            return Optional.of(User.getInstance());
        }
        return Optional.empty();
    }


    /**
     * @return True if the given word is the equivalent of "I" in French
     */
    private boolean isFirstSingularPersonalPronoun(String word) {
        return Translator.getBaseFirstSingularPersonalPronoun(FrenchGrammar.getLexicon()).equals(word);
    }

    /**
     * @return True if the given word is the equivalent of "You" (as a subject) in French
     */
    private boolean isSecondSingularPersonalPronoun(String word) {
        return Translator.getBaseSecondSingularPersonalPronoun(FrenchGrammar.getLexicon()).equals(word);
    }

    private EntityInterrogative processCorrespondingEntityInterrogative(String designation) throws WrongGrammarRuleException {
        Optional<AbstractEntityConcept> designatedConcept = VocabRetriever.getFirstConceptDesignatedBy(getUpdatedVocabulary(), getBase(designation, LexicalCategory.PRONOUN), AbstractEntityConcept.class);

        return designatedConcept
                .filter(x -> x instanceof EntityInterrogative)
                .map(x -> (EntityInterrogative) x)
                .orElseThrow(WrongGrammarRuleException::new);
    }
}
