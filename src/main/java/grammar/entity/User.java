package grammar.entity;

/**
 * The human talking to the AI.
 * It is implied there can be only one for now.
 * 
 * A singleton
 */
public class User extends Entity {
	private User() {
		super(HumanUserConcept.getInstance());
	}
	
	private static User instance;
	
	public static User getInstance() {
		if (instance == null) {
			instance = new User();
		}
		return instance;
	}
}
