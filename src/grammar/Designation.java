package grammar;


/**
 * A name that refer to a concept
 * Ex: "devour" to refer to the concept of eating, or "kitty" to refer to the concept of cat.
 * @author charles
 *
 */
public class Designation {
	private String value;
	
	private AbstractConcept designatedConcept;

	public Designation(String value, AbstractConcept designatedConcept) {
		super();
		this.value = value;
		this.designatedConcept = designatedConcept;
	}

	public String getValue() {
		return value;
	}

	public AbstractConcept getDesignatedConcept() {
		return designatedConcept;
	}

	public void setDesignatedConcept(AbstractConcept designatedConcept) {
		this.designatedConcept = designatedConcept;
	}
	
	@Override
	public String toString() {
		return value + " -> " + designatedConcept.toString();
	}
}
