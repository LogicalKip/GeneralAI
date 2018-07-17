package grammar.token;

public class Token {

    protected final String originalString;

    public Token(String originalString) {
        this.originalString = originalString;
    }

    public String getOriginalString() {
        return originalString;
    }
}
