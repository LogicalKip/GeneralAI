package grammar.token;

public class UndefinedToken extends Token {
    public static final String CORRESPONDING_NLG_CATEGORY = "ANY";
    public UndefinedToken(String originalString) {
        super(originalString);
    }
}
