package grammar;

/**
 * Refers to the concept of "what entity" in a question. In English, could be designated by "who" or "what"
 * A singleton
 */
public class EntityInterrogative extends AbstractEntityConcept implements InterrogativeWord, IEntity {
	
	private EntityInterrogative() {
	}
	
	private static EntityInterrogative instance;
	
	public static EntityInterrogative getInstance() {
		if (instance == null) {
			instance = new EntityInterrogative();
		}
		return instance;
	}
	
	@Override
	public String toString() {
		return "WHICH_ENTITY";
	}
}
