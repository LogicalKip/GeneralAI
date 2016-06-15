package grammar;

/**
 * The human talking to the AI
 *
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
