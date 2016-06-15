package grammar;

/**
 * The AI itself
 * A singleton
 */
public class Myself extends Entity {
	private Myself() {
		super(AIConcept.getInstance());
	}
	
	private static Myself instance;
	
	public static Myself getInstance() {
		if (instance == null) {
			instance = new Myself();
		}
		return instance;
	}
}
