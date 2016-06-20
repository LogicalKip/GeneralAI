package grammar;

public class Knowing extends VerbMeaning {
	private Knowing() {
	}
	
	private static Knowing instance;
	
	public static Knowing getInstance() {
		if (instance == null) {
			instance = new Knowing();
		}
		return instance;
	}
}
