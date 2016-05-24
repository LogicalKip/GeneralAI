package grammar;

public class Order extends Sentence {
	private Verb verb;
	
	private String object;

	public Order(Verb verb, String object) {
		super();
		this.verb = verb;
		this.object = object;
	}

	public Verb getVerb() {
		return verb;
	}

	public String getObject() {
		return object;
	}
}
