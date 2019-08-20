package grammar;

import grammar.gender.Gender;

public class NounDesignation extends Designation {

	public NounDesignation(String value, AbstractConcept designatedConcept) {
		super(value, designatedConcept);
	}

	private Gender gender;
	
	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}
}
