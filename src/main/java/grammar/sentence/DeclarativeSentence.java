package grammar.sentence;

import grammar.entity.Entity;
import grammar.entity.IEntity;
import grammar.verb.Verb;
import lombok.Getter;

import java.util.List;

public class DeclarativeSentence extends SimpleSentence {
	@Getter
	private IEntity object;

	public DeclarativeSentence(IEntity subject, Verb verb, IEntity object) {
		super(subject, verb);
		this.object = object;
	}

	public DeclarativeSentence(IEntity subject, Verb verb) {
		this(subject, verb, null);
	}

	@Override
	public void replace(Entity from, Entity to) {
		super.replace(from, to);
		this.object = this.object.equals(from) ? to : this.object;
	}


	@Override
	public boolean equals(Object otherObject) {
		if (otherObject instanceof DeclarativeSentence) {
			DeclarativeSentence s = (DeclarativeSentence) otherObject;
			return 
					this.getSubject().equals(s.getSubject()) && 
					this.getVerb().equals(s.getVerb()) && 
					this.getObject().equals(s.getObject());
		}
		return super.equals(otherObject);
	}

	@Override
	public List<Entity> getMentionedEntities() {
		List<Entity> res = super.getMentionedEntities();

		addInIfEntity(res, this.object);

		return res;
	}

	@Override
	public Object[] split() {
        return new Object[]{this.getSubject(), this.getVerb(), this.getObject()};
	}
}
