package grammar;

/**
 * The actual entity concept possibly referred to by several designations.
 * E.g The concept of a cat.
 * @author charles
 *
 */
public class Entity extends AbstractEntity {
	private Gender gender;

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}
}
