package exceptions;

/**
 * An exception that shouldn't happen if everything was programmed well.
 * It means the main.AI was not initialized with enough knowledge (words, concepts, designations, whatever) to function properly.
 * Typically, "it should have at least one"
 *
 */
public class NotEnoughKnowledgeException extends Exception {
	private static final long serialVersionUID = -335297979692402827L;
	public NotEnoughKnowledgeException(String msg) {
		super(msg);
	}
}
