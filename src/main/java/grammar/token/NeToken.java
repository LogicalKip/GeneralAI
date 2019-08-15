package grammar.token;

/**
 * Matches the french word "ne", usually followed by a verb and/or "pas".
 */
public class NeToken extends Token {
    public NeToken(String originalString) {
        super(originalString);
    }
}
