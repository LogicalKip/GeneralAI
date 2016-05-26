package grammar;

/**
 * Think of {@link EntityConcept} objects as classes and of {@link Entity} objects as instances of classes.
 * E.g : Mr snuggles, a specific cat with a white spot on the head, is an {@link Entity}. For the concept of a cat (mammal, small, etc), see {@link EntityConcept}
 * @author charles
 *
 */
public class Entity implements IEntity {
	private EntityConcept referredConcept;

	public EntityConcept getReferredConcept() {
		return referredConcept;
	}

	public Entity(EntityConcept referredConcept) {
		super();
		this.referredConcept = referredConcept;
	}

	public void setReferredConcept(EntityConcept referredConcept) {
		this.referredConcept = referredConcept;
	}
	
	//TODO private List<Adjective> characteristics
}
