package grammar.sentence;

import grammar.entity.Entity;
import grammar.entity.IEntity;
import grammar.verb.Verb;
import lombok.Getter;
import lombok.Setter;
import simplenlg.features.Tense;

import java.util.LinkedList;
import java.util.List;

public abstract class SimpleSentence extends Sentence implements Cloneable {
	@Getter
	@Setter
	private Tense tense;

	@Getter
	@Setter
	private boolean isNegative;

	@Getter
	private IEntity subject;

	@Getter
	@Setter
	private boolean interrogative;

	SimpleSentence(IEntity subject, Verb verb) {
		super(verb);
		this.subject = subject;
		this.interrogative = false;
		this.isNegative = false;
		this.tense = Tense.PRESENT;
	}
	
	public abstract Object[] split();

	public SimpleSentence interrogative(boolean interrogative) {
		this.interrogative = interrogative;
		return this;
	}

	public SimpleSentence interrogative() {
		return this.interrogative(true);
	}

	public void replace(Entity from, Entity to) {
		this.subject = this.subject.equals(from) ? to : this.subject;
	}
	
	public List<Entity> getMentionedEntities() {
		List<Entity> res = new LinkedList<>();

		addInIfEntity(res, this.subject);
		
		return res;
	}
	
	void addInIfEntity(List<Entity> list, IEntity object) {
		if (object instanceof Entity) {
			list.add((Entity)object);
		}
	}

	public SimpleSentence negate() {
		this.isNegative = !this.isNegative;
		return this;
	}
	
	public void setNegative() {
		this.isNegative = true;
	}

	public SimpleSentence tense(Tense tense) {
		this.tense = tense;
		return this;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
