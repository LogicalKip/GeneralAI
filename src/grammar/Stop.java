package grammar;

/**
 * Means (for now...) stopping the AI
 * @author charles
 *
 */
public class Stop extends VerbMeaning {
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
