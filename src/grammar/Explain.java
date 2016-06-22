package grammar;

public class Explain extends Verb {
	private Explain() {
	}
	
	private static Explain instance;
	
	public static Explain getInstance() {
		if (instance == null) {
			instance = new Explain();
		}
		return instance;
	}

	@Override
	public String toString() {
		return "EXPLAIN";
	}
}
