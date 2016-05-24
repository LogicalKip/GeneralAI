package grammar;

/**
 * E.g : "start", "launch", usually followed by the software to start
 * @author charles
 *
 */
public class StartSoftware extends VerbMeaning {
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
