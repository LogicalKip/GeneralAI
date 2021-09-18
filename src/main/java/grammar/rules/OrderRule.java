package grammar.rules;

import exceptions.CantFindSuchAnEntityException;
import exceptions.WrongGrammarRuleException;
import grammar.Designation;
import grammar.FrenchGrammar;
import grammar.Order;
import grammar.token.VerbToken;
import grammar.verb.Verb;
import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.NumberAgreement;
import simplenlg.features.Person;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.NLGFactory;
import simplenlg.framework.WordElement;
import simplenlg.phrasespec.VPPhraseSpec;
import simplenlg.realiser.Realiser;
import util.VocabRetriever;

import java.rmi.UnexpectedException;
import java.util.Optional;

import static grammar.FrenchGrammar.getUpdatedVocabulary;

public class OrderRule extends GrammarRule {
    @Override
    protected Object internalApply() throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {
        Verb verb = imperative();

        String object = null;

        if (!this.usedAllTokens()) {
            object = currentToken.getOriginalString();
            nextToken();
        }

        return new Order(verb, object);
    }

    private Verb imperative() throws WrongGrammarRuleException {
        if (!(currentToken instanceof VerbToken)) {
            fail();
        }

        String originalString = currentToken.getOriginalString();
        WordElement baseWord = FrenchGrammar.getLexicon().getWordFromVariant(originalString, LexicalCategory.VERB);

        String singularImperative = imperativeOf(baseWord, NumberAgreement.SINGULAR);
        String pluralImperative = imperativeOf(baseWord, NumberAgreement.PLURAL);

        if (originalString.equals(singularImperative) || originalString.equals(pluralImperative)) {
            Optional<Designation> res = VocabRetriever.getFirstDesignationFrom(getUpdatedVocabulary(), baseWord.getBaseForm(), Verb.class);
            if (res.isEmpty()) {
                fail();
            }

            nextToken();

            res.get().incrementTimesUserUsedIt();
            return ((Verb) res.get().getDesignatedConcept());
        } else {
            fail();
            return null; // won't reach this
        }
    }


    /**
     * Subfunction used to conjugate a verb into its imperative form
     *
     * @param baseWord        the verb
     * @param numberAgreement The person is implied to be the second (you). This indicates the number (plural or singular)
     * @return The imperative without caps or dots.
     */
    private String imperativeOf(WordElement baseWord, NumberAgreement numberAgreement) {
        VPPhraseSpec verbPhrase = new NLGFactory(FrenchGrammar.getLexicon()).createVerbPhrase(baseWord.getBaseForm());
        verbPhrase.setFeature(Feature.FORM, Form.IMPERATIVE);
        verbPhrase.setFeature(Feature.PERSON, Person.SECOND);
        verbPhrase.setFeature(Feature.NUMBER, numberAgreement);

        return new Realiser().realise(verbPhrase).getRealisation();
    }
}
