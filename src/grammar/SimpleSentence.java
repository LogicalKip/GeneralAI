package grammar;

import java.util.LinkedList;
import java.util.List;

import simplenlg.features.Tense;

public abstract class SimpleSentence extends Sentence implements Cloneable {
	private Tense tense;

	private boolean isNegative;

	private IEntity subject;

	private boolean interrogative;

	public SimpleSentence(IEntity subject) {
		this.subject = subject;
		this.interrogative = false;
		this.isNegative = false;
		this.tense = Tense.PRESENT;
	}
	
	public abstract Object[] split();

	public boolean isInterrogative() {
		return interrogative;
	}

	public void setInterrogative(boolean interrogative) {
		this.interrogative = interrogative;
	}

	public IEntity getSubject() {
		return subject;
	}

	public abstract Verb getVerb();


	public void replace(Entity from, Entity to) {
		this.subject = this.subject.equals(from) ? to : this.subject;
	}
	
	public List<Entity> getMentionedEntities() {
		List<Entity> res = new LinkedList<Entity>();

		addInIfEntity(res, this.subject);
		
		return res;
	}
	
	void addInIfEntity(List<Entity> list, IEntity object) {
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
