package exceptions;

/**
 * Should never happen. Throw this when parsing the grammar, when a condition made true can only mean that the grammar is not LL(1) and that therefore there's something wrong with it
 *
 */
public class GrammarNotLL1Exception extends Exception {
	private static final long serialVersionUID = -5025823472560096620L;
}
