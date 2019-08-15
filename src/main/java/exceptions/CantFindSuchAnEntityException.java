package exceptions;

import grammar.entity.EntityConcept;

import java.util.List;

/**
 * Means someone (probably the user) is referring to an entity corresponding to a certain description (ex : "you know, the black cat"), but the main.AI has no idea who this is referring to, probably because it has simply never heard of it
 */
public class CantFindSuchAnEntityException extends Exception {

	private static final long serialVersionUID = 1548727791539077399L;

	private EntityConcept concept;
	private List<String> qualifiers;
	
	
	public CantFindSuchAnEntityException(EntityConcept concept, List<String> qualifiers) {
		super();
		this.concept = concept;
		this.qualifiers = qualifiers;
	}


	public List<String> getQualifiers() {
		return qualifiers;
	}


	public EntityConcept getConcept() {
		return concept;
	}
}
