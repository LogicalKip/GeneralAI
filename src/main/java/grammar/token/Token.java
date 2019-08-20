package grammar.token;

public class Token {

    protected final String originalString;

    public Token(String originalString) {
        this.originalString = originalString;
    }

    public String getOriginalString() {
        return originalString;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(this.getClass()) && this.originalString.equals(((Token) obj).originalString);
    }
}
