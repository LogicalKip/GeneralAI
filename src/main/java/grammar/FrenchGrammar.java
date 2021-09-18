package grammar;

import exceptions.CantFindSuchAnEntityException;
import exceptions.WrongGrammarRuleException;
import grammar.entity.Entity;
import grammar.rules.GrammarRule;
import grammar.rules.GrammarUtils;
import grammar.sentence.Sentence;
import grammar.token.Token;
import grammar.token.Tokenizer;
import lombok.Getter;
import simplenlg.framework.LexicalCategory;
import simplenlg.lexicon.Lexicon;

import java.rmi.UnexpectedException;
import java.util.LinkedList;
import java.util.List;

//FIXME potential future problem, even if grammar rules as they are now can't make it happen : get/create an entity, add it to newEntities, then rollback because the rule was the wrong one, but the entity is still here. If the sentence ends up correct, there will be a wrong entity added/duplicated. Could be the same with vocab/other things

/**
 * Parses french Strings into Java classes and concepts used by the AI, using a tree made of custom grammar rules.
 */
public class FrenchGrammar {

    @Getter
    private static Lexicon lexicon;

    @Getter
    private static List<Designation> newVocabulary;
    @Getter
    private static List<Entity> newEntities;
    @Getter
    private static List<Designation> actualAIVocabulary;
    @Getter
    private static List<Entity> actualAIKnownEntities;

    public FrenchGrammar(Lexicon lexicon, List<Designation> aiVocabulary, List<Entity> aiEntities) {
        FrenchGrammar.lexicon = lexicon;
        GrammarUtils.setLexicon(lexicon);

        actualAIVocabulary = aiVocabulary;
        actualAIKnownEntities = aiEntities;
        newVocabulary = new LinkedList<>();
        newEntities = new LinkedList<>();
    }

    /**
     * More or less returns the stem of the given word
     */
    public static String getBase(String word, LexicalCategory category) {
        return FrenchGrammar.getLexicon().getWordFromVariant(word, category).getBaseForm();
    }

    public static List<Designation> getUpdatedVocabulary() {
        List<Designation> res = new LinkedList<>();
        res.addAll(actualAIVocabulary);
        res.addAll(newVocabulary);
        return res;
    }

    public Sentence parse(String input) throws CantFindSuchAnEntityException, WrongGrammarRuleException, UnexpectedException {
        newVocabulary = new LinkedList<>();
        newEntities = new LinkedList<>();

        List<Token> tokens = new Tokenizer(lexicon).tokenize(input);

        return (Sentence) GrammarRule.getStartingRule(tokens).apply(null);
    }
}
