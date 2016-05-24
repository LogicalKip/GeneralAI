package grammar;

/**
 * The concept that asks for a missing action in a question (the concept of "what action" e.g the cat DOESWHAT to the mouse)
 * 
 * A singleton
 * @author charles
 *
 */
public class VerbInterrogative extends AbstractVerb implements InterrogativeWord {
	private VerbInterrogative() {
	}
	
	private static VerbInterrogative instance;
	
	public static VerbInterrogative getInstance() {
		if (instance == null) {
			instance = new VerbInterrogative();
		}
		return instance;
	}

	@Override
	public String toString() {
		return "WHAT_VERB";
	}
}
