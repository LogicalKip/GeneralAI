package grammar;

/**
 * The adverb showing negation
 */
public class Not extends Adverb {
	private Not() {
	}
	
	private static Not instance;
	
	public static Not getInstance() {
		if (instance == null) {
			instance = new Not();
		}
		return instance;
	}
}
