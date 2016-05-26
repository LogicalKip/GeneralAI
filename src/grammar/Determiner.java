package grammar;

/**
 * Determiner of a noun, such as "the", "a"
 * Different genders mean different determiners
 * 
 * @author charles
 *
 */
public abstract class Determiner extends AbstractConcept {
	
	private Gender gender;
	
	public Determiner(Gender g) {
		this.gender = g;
	}
	
	@Override
	public String toString() {
		return "DETERMINER";
	}

	public Gender getGender() {
		return gender;
	}
}
