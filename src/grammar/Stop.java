package grammar;

/**
 * Means (for now...) stopping the AI
 *
 */
public class Stop extends Verb {
	private Stop() {
	}
	
	private static Stop instance;
	
	public static Stop getInstance() {
		if (instance == null) {
			instance = new Stop();
		}
		return instance;
	}

	@Override
	public String toString() {
		return "!STOP!";
	}
}
