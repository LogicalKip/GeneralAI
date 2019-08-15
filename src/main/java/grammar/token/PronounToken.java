package grammar.token;

public class PronounToken extends Token {
    public PronounToken(String originalString) {
        super(originalString);
    }

    public static final String CORRESPONDING_NLG_CATEGORY = "PRONOUN";
}
