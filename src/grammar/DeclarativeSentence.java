package grammar;

import java.util.List;

public class DeclarativeSentence extends SimpleSentence {
	private AbstractVerb verb;

	private IEntity object;

	public DeclarativeSentence(IEntity subject, AbstractVerb verb, IEntity object) {
		super(subject);
		this.object = object;
		this.verb = verb;
	}

	@Override
	public AbstractVerb getVerb() {
		return verb;
	}

	public IEntity getObject() {
		return object;
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
					this.object.equals(s.getObject());
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
		Object res[] =  {this.getSubject(), this.getVerb(), this.getObject()};
		return res;
	}
}
