package grammar;

import java.util.LinkedList;
import java.util.List;

/**
 * Think of {@link EntityConcept} objects as classes and of {@link Entity} objects as instances of classes.
 * E.g : Mr snuggles, a specific cat with a white spot on the head, is an {@link Entity}. For the concept of a cat (mammal, small, etc), see {@link EntityConcept}
 * @author charles
 *
 */
public class Entity implements IEntity {
	private EntityConcept concept;
	
	private List<Adjective> characteristics;

	public EntityConcept getConcept() {
		return concept;
	}

	public Entity(EntityConcept concept) {
		super();
		this.concept = concept;
		this.characteristics = new LinkedList<Adjective>();
	}

	public void setConcept(EntityConcept concept) {
		this.concept = concept;
	}

	public List<Adjective> getCharacteristics() {
		return characteristics;
	}

	public void setCharacteristics(List<Adjective> characteristics) {
		this.characteristics = characteristics;
	}
}
