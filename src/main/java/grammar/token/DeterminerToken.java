package grammar.token;

public class DeterminerToken extends Token {
    public static final String CORRESPONDING_NLG_CATEGORY = "DETERMINER"; //TODO class field ? what to do with those without category ?

    public DeterminerToken(String originalString) {
        super(originalString);
    }
}
