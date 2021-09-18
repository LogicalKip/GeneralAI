package grammar.rules;

import grammar.FrenchGrammar;
import grammar.token.AdjectiveToken;
import grammar.token.DeterminerToken;
import grammar.token.MiscToken;
import grammar.token.NounToken;
import grammar.token.PronounToken;
import grammar.token.Token;
import grammar.token.VerbToken;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.Lexicon;

public class GrammarUtils {

    private static Lexicon lexicon;

    //FIXME for all "canBe" : the fact the word is not in the lexicon doesn't mean it's not a noun/verb/etc. Only if it's IN the lexicon and NOT a noun/etc means it's NOT a noun/etc
    public static boolean canBeDeterminer(Token token) {
        return token instanceof DeterminerToken || canBeA(DeterminerToken.CORRESPONDING_NLG_CATEGORY, token);
    }

    public static boolean canBeVerb(Token token) {
        return token instanceof VerbToken || canBeA(VerbToken.CORRESPONDING_NLG_CATEGORY, token);
    }

    public static boolean canBeNoun(Token token) {
        return token instanceof NounToken || canBeA(NounToken.CORRESPONDING_NLG_CATEGORY, token);
    }

    public static boolean canBeAdjective(Token token) { //FIXME UT first (or second) adjective is not known but still can be one.
        return token instanceof AdjectiveToken || canBeA(AdjectiveToken.CORRESPONDING_NLG_CATEGORY, token);
    }

    public static boolean canBePronoun(Token token) {
        return token instanceof PronounToken || canBeA(PronounToken.CORRESPONDING_NLG_CATEGORY, token);
    }

    /**
     * Checks if token can be the french adverb "pas", which means "not". Be aware that in french, "pas" is also a noun with a completely different meaning ("step")
     */
    public static boolean canBePas(Token token) {
        return token instanceof MiscToken && token.getOriginalString().equalsIgnoreCase("pas");
    }

    /**
     * Returns true if at least one of the possible words corresponding to the token's original string is a 'category'
     * Ex : if that specific spelling ("souris") is sometimes that of an noun, sometimes of a verb, then canBeA(noun) and also canBeA(verb)
     *
     * @param category using the string NLG uses for its category
     */
    public static boolean canBeA(String category, Token token) {
        for (WordElement word : lexicon.getWordsFromVariant(token.getOriginalString())) {
            if (word.getCategory().toString().equals(category)) {
                return true;
            }
        }
        return false;
    }

    public static void setLexicon(Lexicon lexicon) {
        GrammarUtils.lexicon = lexicon;
    }
    public static boolean isKnownAdjective(String word) {
        return FrenchGrammar.getLexicon().hasWordFromVariant(word, LexicalCategory.ADJECTIVE);
    }

    public static boolean isKnownVerb(String word) {
        return FrenchGrammar.getLexicon().hasWordFromVariant(word, LexicalCategory.VERB);
    }
}
