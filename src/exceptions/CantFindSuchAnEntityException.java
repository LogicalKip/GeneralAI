package exceptions;

import java.util.List;

import grammar.Adjective;
import grammar.EntityConcept;

public class CantFindSuchAnEntityException extends Exception {

	private static final long serialVersionUID = 1548727791539077399L;

	private EntityConcept concept;
	private List<Adjective> qualifiers;
	
	
	public CantFindSuchAnEntityException(EntityConcept concept, List<Adjective> qualifiers) {
		super();
		this.concept = concept;
		this.qualifiers = qualifiers;
	}


	public List<Adjective> getQualifiers() {
		return qualifiers;
	}


	public EntityConcept getConcept() {
		return concept;
	}
}
