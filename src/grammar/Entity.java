package grammar;

import java.util.LinkedList;
import java.util.List;

/**
 * Think of {@link EntityConcept} objects as classes and of {@link Entity} objects as instances of classes.
 * E.g : Mr snuggles, a specific cat with a white spot on the head, is an {@link Entity}. For the concept of a cat (mammal, small, etc), see {@link EntityConcept}
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
	
	@Override
	public boolean equals(Object obj) {
		return this == obj; // Even with all attributes equals, 2 entities may not be the same. An entity equals only itself. Therefore, the default implementation is correct and should not be changed.
	}
}
