package grammar.rules;

import exceptions.CantFindSuchAnEntityException;
import exceptions.WrongGrammarRuleException;
import grammar.FrenchGrammar;
import grammar.sentence.Sentence;
import grammar.token.EndOfInputToken;
import grammar.token.Token;
import simplenlg.framework.WordElement;

import java.rmi.UnexpectedException;
import java.util.List;

/**
 * Every rule should throw {@link WrongGrammarRuleException} when it can't be applied (for any reason).
 * It should also be positioned on the next token when it succeeds. (if rule is A B, then applied to A B C D it should be on C when it's over).
 * Each rule sets the new current token of its parent automatically when it succeeds, and doesn't change it if it fails.
 */
public abstract class GrammarRule {
    private static List<Token> tokens;
    protected Token currentToken;
    private int currentTokenIndex;

    /**
     * First rule doesn't work like the others since it has no parent.
     * Throws {@link WrongGrammarRuleException} if it can't completely succeed or if there are still tokens left after parsing.
     */
    public static GrammarRule getStartingRule(List<Token> tokens) {
        GrammarRule startingRule = new GrammarRule() {
            @Override
            protected Object internalApply() throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {
                Sentence res = (Sentence) new MainRule().apply(this);
                if (!usedAllTokens()) {
                    fail();
                }
                return res;
            }

            @Override
            public Object apply(GrammarRule caller) throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {
                return internalApply();
            }
        };
        GrammarRule.tokens = tokens;
        startingRule.setToken(0);

        return startingRule;
    }

    protected abstract Object internalApply() throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException;

    // FIXME find better pattern than returning Object : let the caller specify the return type expected through a Class parameter ? so compilation can catch some errors, and caller has no need to cast
    public Object apply(GrammarRule caller) throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {
        this.setToken(caller.currentTokenIndex);

        var res = internalApply();
        caller.setToken(this.currentTokenIndex);
        return res;
    }

    private void setToken(int tokenIndex) {
        this.currentTokenIndex = tokenIndex;
        this.currentToken = tokens.get(tokenIndex);
    }

    protected final void nextToken() throws WrongGrammarRuleException {
        if (currentTokenIndex >= tokens.size()) {
            fail();
        }
        setToken(currentTokenIndex + 1);
    }

    protected final boolean usedAllTokens() {
        return currentToken instanceof EndOfInputToken;
    }

    protected final void fail() throws WrongGrammarRuleException {
        throw new WrongGrammarRuleException();
    }


    /**
     * Returns true if at least one of the possible words corresponding to the token's original string is a 'category'
     * Ex : if that specific spelling ("souris") is sometimes that of an noun, sometimes of a verb, then canBeA(noun) and also canBeA(verb)
     *
     * @param category using the string NLG uses for its category
     */
    protected final boolean canBeA(String category, Token token) {
        for (WordElement word : FrenchGrammar.getLexicon().getWordsFromVariant(token.getOriginalString())) {
            if (word.getCategory().toString().equals(category)) {
                return true;
            }
        }
        return false;
    }


}
