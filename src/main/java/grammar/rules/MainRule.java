package grammar.rules;

import exceptions.CantFindSuchAnEntityException;
import exceptions.WrongGrammarRuleException;
import grammar.Order;
import grammar.sentence.Sentence;
import grammar.sentence.SimpleSentence;
import grammar.token.MiscFinalPunctuationToken;

import java.rmi.UnexpectedException;

public class MainRule extends GrammarRule {
    @Override
    protected Object internalApply() throws CantFindSuchAnEntityException, UnexpectedException, WrongGrammarRuleException {
        // All possibilities
        Sentence res;
        try {
            res = (SimpleSentence) new SimpleSentenceRule().apply(this);
        } catch (WrongGrammarRuleException e) {
            res = (Order) new OrderRule().apply(this);
        }

        // Facultative final punctuation
        while (currentToken instanceof MiscFinalPunctuationToken) {
            nextToken();
        }

        return res;
    }
}
