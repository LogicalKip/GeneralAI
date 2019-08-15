package grammar;

import grammar.sentence.Sentence;
import grammar.verb.Verb;

public class Order extends Sentence {
	private String object;

	public Order(Verb verb, String object) {
		super(verb);
		this.object = object;
	}


	public String getObject() {
		return object;
	}
}
