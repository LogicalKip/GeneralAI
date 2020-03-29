package grammar.token;

import lombok.Getter;

public class Token {

    @Getter
    protected final String originalString;

    public Token(String originalString) {
        this.originalString = originalString;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(this.getClass()) && this.originalString.equals(((Token) obj).originalString);
    }
}
