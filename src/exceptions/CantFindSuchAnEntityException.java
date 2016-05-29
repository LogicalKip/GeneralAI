package exceptions;

import grammar.EntityConcept;

public class CantFindSuchAnEntityException extends Exception {

	private static final long serialVersionUID = 1548727791539077399L;

	private EntityConcept concept;
	
	
	public CantFindSuchAnEntityException(EntityConcept concept) {
		super();
		this.concept = concept;
	}


	public EntityConcept getConcept() {
		return concept;
	}
}
