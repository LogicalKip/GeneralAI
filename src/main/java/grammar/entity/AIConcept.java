package grammar.entity;

/**
 * What is an main.AI.
 * A singleton
 */
public class AIConcept extends EntityConcept {
	private AIConcept() {
	}
	
	private static AIConcept instance;
	
	public static AIConcept getInstance() {
		if (instance == null) {
			instance = new AIConcept();
		}
		return instance;
	}
}
