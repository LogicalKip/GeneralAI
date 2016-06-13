package grammar;

/**
 * What a particular verb concept means. Even if nothing is known about its meaning, it may still be known that several designations refer to a same verb meaning, and therefore that they mean the same thing. 
 * E.g The concept of [eating], without a particular tense
 * @author charles
 *
 */
public class VerbMeaning {
	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
