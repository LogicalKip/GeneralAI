package grammar;

/**
 * What is the man talking to the AI
 */
public class HumanUserConcept extends EntityConcept {
	private HumanUserConcept() {
	}
	
	private static HumanUserConcept instance;
	
	public static HumanUserConcept getInstance() {
		if (instance == null) {
			instance = new HumanUserConcept();
		}
		return instance;
	}
}
