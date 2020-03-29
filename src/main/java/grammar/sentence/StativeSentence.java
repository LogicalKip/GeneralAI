package grammar.sentence;

import grammar.Adjective;
import grammar.entity.IEntity;
import grammar.verb.Verb;
import lombok.Getter;

/**
 * A sentence with a stative verb, and an adjective as "object"
 */
public class StativeSentence extends SimpleSentence {
	@Getter
	private Adjective adjective;

	public StativeSentence(IEntity subject, Verb verb, Adjective a) {
		super(subject, verb);
		this.adjective = a;
	}

	@Override
	public Object[] split() {
		return new Object[]{this.getSubject(), this.getVerb(), this.adjective};
	}

	@Override
	public boolean equals(Object otherObject) {
		if (otherObject instanceof StativeSentence) {
			StativeSentence s = (StativeSentence) otherObject;
			return 
					this.getSubject().equals(s.getSubject()) && 
					this.getVerb().equals(s.getVerb()) && 
					this.getAdjective().equals(s.getAdjective());
		}
		return super.equals(otherObject);
	}
}
