package grammar;

/**
 * Will be used to understand that two designations have the same underlying concept and are synonyms
 * A designation for this could be "means" or "signifies"
 * A singleton
 * @author charles
 *
 */
public class HasSameMeaningAs extends VerbMeaning {
	private HasSameMeaningAs() {
	}
	
	private static HasSameMeaningAs instance;
	
	public static HasSameMeaningAs getInstance() {
		if (instance == null) {
			instance = new HasSameMeaningAs();
		}
		return instance;
	}

	@Override
	public String toString() {
		return "<=>";
	}
}
