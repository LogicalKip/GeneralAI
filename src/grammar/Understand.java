package grammar;

/**
 * A singleton
 */
public class Understand extends Verb {
	private Understand() {
	}
	
	private static Understand instance;
	
	public static Understand getInstance() {
		if (instance == null) {
			instance = new Understand();
		}
		return instance;
	}
}
