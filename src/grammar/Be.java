package grammar;

/**
 * Probably the most important verb there is
 * A singleton
 *
 */
public class Be extends StativeVerb {
	private Be() {
	}
	
	private static Be instance;
	
	public static Be getInstance() {
		if (instance == null) {
			instance = new Be();
		}
		return instance;
	}
}
