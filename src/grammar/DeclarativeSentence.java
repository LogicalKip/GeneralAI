package grammar;

public class DeclarativeSentence extends Sentence {

	private AbstractEntity subject;

	private AbstractVerb verb;

	private AbstractEntity object;

	private boolean interrogative;

	public DeclarativeSentence(AbstractEntity subject, AbstractVerb verb, AbstractEntity object) {
		this.subject = subject;
		this.verb = verb;
		this.object = object;
		this.interrogative = false;
	}
	
	public AbstractConcept[] split() {
		AbstractConcept res[] =  {this.getSubject(), this.getVerb(), this.getObject()};
		return res;
	}

	@Override
	public String toString() {
		return "Sentence : " + subject + "+" + verb + "+" + object + " " + (isInterrogative() ? "?" : "!");
	}

	public boolean isInterrogative() {
		return interrogative;
	}

	public void setInterrogative(boolean interrogative) {
		this.interrogative = interrogative;
	}

	public AbstractEntity getSubject() {
		return subject;
	}

	public AbstractVerb getVerb() {
		return verb;
	}

	public AbstractEntity getObject() {
		return object;
	}
	
	public void replace(AbstractConcept from, AbstractConcept to) {
		if (from instanceof AbstractEntity && to instanceof AbstractEntity) {
			this.subject = this.subject.equals(from) ? (AbstractEntity) to : this.subject;
			this.object = this.object.equals(from) ? (AbstractEntity) to : this.object;
		} else if (from instanceof AbstractVerb  && to instanceof AbstractVerb) {
			this.verb = this.verb.equals(from) ? (AbstractVerb) to : this.verb;
		}
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (otherObject instanceof DeclarativeSentence) {
			DeclarativeSentence s = (DeclarativeSentence) otherObject;
			return this.subject.equals(s.getSubject()) && 
					this.verb.equals(s.getVerb()) &&
					this.object.equals(s.getObject());
		} else {
			return super.equals(otherObject);
		}
	}
	
}
