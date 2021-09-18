package grammar.token;

/**
 * Not a token provided by the user. doesn't represent a dot (see {@link MiscFinalPunctuationToken}.
 * This token is automatically added at the end of every parsed input to be used during analysis,
 * so that we know we've reached the end of all "real" tokens, without throwing any exception.
 */
public class EndOfInputToken extends Token {
    public EndOfInputToken(String originalString) {
        super(originalString);
    }
}
