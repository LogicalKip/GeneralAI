package grammar;

/**
 * A sentence with a stative verb, and an adjective as "object"
 */
public class StativeSentence extends SimpleSentence {
	private StativeVerb verb;

	private Adjective adjective;

	public StativeSentence(IEntity subject, StativeVerb verb, Adjective a) {
		super(subject);
		this.verb = verb;
		this.adjective = a;
	}

	@Override
	public AbstractVerb getVerb() {
		return verb;
	}

	@Override
	public Object[] split() {
		Object res[] =  {this.getSubject(), this.getVerb(), this.adjective};
		return res;
	}

	public Adjective getAdjective() {
		return adjective;
	}

	@Override
	public boolean equals(Object otherObject) {
		if (otherObject instanceof StativeSentence) {
			StativeSentence s = (StativeSentence) otherObject;
			return 
					this.getSubject().equals(s.getSubject()) && 
					this.getVerb().equals(s.getVerb()) && 
					this.adjective.equals(s.getAdjective());
		}
		return super.equals(otherObject);
	}
}
