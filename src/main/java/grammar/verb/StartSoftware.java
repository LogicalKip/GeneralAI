package grammar.verb;

/**
 * E.g : "start", "launch", usually followed by the software to start
 *
 */
public class StartSoftware extends Verb {
	private StartSoftware() {
	}
	
	private static StartSoftware instance;
	
	public static StartSoftware getInstance() {
		if (instance == null) {
			instance = new StartSoftware();
		}
		return instance;
	}

	@Override
	public String toString() {
		return "startsoftware";
	}
}
