package grammar.rules;

import exceptions.CantFindSuchAnEntityException;
import exceptions.WrongGrammarRuleException;
import grammar.entity.AIConcept;
import grammar.entity.Entity;
import grammar.entity.HumanUserConcept;
import grammar.sentence.Sentence;
import grammar.token.Token;
import org.junit.jupiter.api.Test;

import java.rmi.UnexpectedException;
import java.util.List;

class GrammarRuleTest {

    public GrammarRule getStartingRule(List<Token> tokens) {
        GrammarRule startingRule = new GrammarRule() {
            @Override
            protected Object internalApply() throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {
                Sentence res = (Sentence) new ParentRule().apply(this);
                if (didNotUseAllTokens()) {
                    fail();
                }
                return res;
            }

            @Override
            public Object apply(GrammarRule caller) throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {
                return internalApply();
            }
        };

        return startingRule;
    }

    @Test
    private void wrongRulesHaveNoSideEffect() throws CantFindSuchAnEntityException, UnexpectedException, WrongGrammarRuleException {
        getStartingRule(List.of()).apply(null);
    }

    /**
     * Calls 2 subrules, only the second is the correct one.
     */
    class ParentRule extends GrammarRule {
        @Override
        protected Object internalApply() throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {
            try {
                new ChildRule1().apply(this);
            } catch (WrongGrammarRuleException e) {
                new ChildRule2().apply(this);
            }
            return null;
        }
    }

    /**
     * Adds new entities and vocabulary but won't be the correct rule in the end and so shouldn't have any side effect
     */
    class ChildRule1 extends GrammarRule {
        @Override
        protected Object internalApply() throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {
            this.getNewEntities().add(new Entity(HumanUserConcept.getInstance()));
            fail();
            return null;
        }
    }

    /**
     * Adds new entities and vocabulary and will be the correct rule so only those additions should be used
     */
    class ChildRule2 extends GrammarRule {
        @Override
        protected Object internalApply() throws WrongGrammarRuleException, CantFindSuchAnEntityException, UnexpectedException {
            this.getNewEntities().add(new Entity(AIConcept.getInstance()));
            return null;
        }
    }

}