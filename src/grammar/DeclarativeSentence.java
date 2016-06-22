package grammar;

import java.util.LinkedList;
import java.util.List;

import simplenlg.features.Tense;

public class DeclarativeSentence extends Sentence implements Cloneable {
	private Tense tense;

	private boolean isNegative;

	private IEntity subject;

	private AbstractVerb verb;

	private IEntity object;

	private boolean interrogative;

	public DeclarativeSentence(IEntity subject, AbstractVerb verb, IEntity object) {
		this.subject = subject;
		this.verb = verb;
		this.object = object;
		this.interrogative = false;
		this.isNegative = false;
		this.tense = Tense.PRESENT;
	}
	
	public Object[] split() {
		Object res[] =  {this.getSubject(), this.getVerb(), this.getObject()};
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

	public IEntity getSubject() {
		return subject;
	}

	public AbstractVerb getVerb() {
		return verb;
	}

	public IEntity getObject() {
		return object;
	}

	public void replace(Entity from, Entity to) {
		this.subject = this.subject.equals(from) ? to : this.subject;
		this.object = this.object.equals(from) ? to : this.object;
	}

	@Override
	public boolean equals(Object otherObject) {
		if (otherObject instanceof DeclarativeSentence) {
			DeclarativeSentence s = (DeclarativeSentence) otherObject;
			return this.subject.equals(s.getSubject()) && 
					this.verb.equals(s.getVerb()) &&
					this.object.equals(s.getObject());
		}
		return super.equals(otherObject);
	}
	
	public List<Entity> getMentionedEntities() {
		List<Entity> res = new LinkedList<Entity>();

		addInIfEntity(res, this.subject);
		addInIfEntity(res, this.object);
		
		return res;
	}
	
	private void addInIfEntity(List<Entity> list, IEntity object) {
		if (object instanceof Entity) {
			list.add((Entity)object);
		}
	}
	
	public boolean isNegative() {
		return isNegative;
	}

	public void setNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}
	
	public void setNegative() {
		this.isNegative = true;
	}

	public Tense getTense() {
		return tense;
	}

	public void setTense(Tense tense) {
		this.tense = tense;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
