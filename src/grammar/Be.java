package grammar;

/**
 * Probably the most important verb there is
 * A singleton
 *
 */
public class Be extends Verb {
	private Be() {
	    super(true, true);
	}
	
	private static Be instance;
	
	public static Be getInstance() {
		if (instance == null) {
			instance = new Be();
		}
		return instance;
	}
}
