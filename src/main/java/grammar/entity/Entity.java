package grammar.entity;

import grammar.Adjective;

import java.util.HashSet;
import java.util.Set;

/**
 * Think of {@link EntityConcept} objects as classes and of {@link Entity} objects as instances of classes.
 * E.g : Mr snuggles, a specific cat with a white spot on the head, is an {@link Entity}. For the concept of a cat (mammal, small, etc), see {@link EntityConcept}
 */
public class Entity implements IEntity {
	private EntityConcept concept;
	
	private Set<Adjective> characteristics;

	public EntityConcept getConcept() {
		return concept;
	}

	public Entity(EntityConcept concept) {
		super();
		this.concept = concept;
		this.characteristics = new HashSet<>();
	}

	public void setConcept(EntityConcept concept) {
		this.concept = concept;
	}

	public Set<Adjective> getCharacteristics() {
		return characteristics;
	}

	public void addCharacteristic(Adjective adjective) {
		this.characteristics.add(adjective);
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj; // Even with all attributes equals, 2 entities may not be the same. An entity equals only itself. Therefore, the default implementation is correct and should not be changed.
	}
}
