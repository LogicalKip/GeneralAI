package grammar;

import grammar.gender.Gender;
import lombok.Getter;
import lombok.Setter;

class NounDesignation extends Designation {

	NounDesignation(String value, AbstractConcept designatedConcept) {
		super(value, designatedConcept);
	}

	@Getter
	@Setter
	private Gender gender;
}
