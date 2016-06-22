package grammar;

/**
 * Is now used to show that two concepts are actually one same concept. Ex : cat {@link HasSameMeaningAs} kitty. This however does not mean that those two animals previously referred to as "kitty" and "cat" are the same cat. They are two animals, probably in different places and with different skin colors, but they are the same race("/class"), which can be referred to as "kitty" or "cat".
 * A designation for this could be "means" or "signifies"
 * A singleton
 * @author charles
 *
 */
public class HasSameMeaningAs extends Verb {
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
