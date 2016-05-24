package exceptions;

/**
 * Use in grammar rules with alternatives (A -> B | C), to know that the current rule cannot be the right one, since the first expected non-terminal is not the correct one.
 * @author charles
 *
 */
public class WrongGrammarRuleException extends Exception {
	private static final long serialVersionUID = -116326258296217072L;
}
