package main;

import java.util.LinkedList;
import java.util.List;

import exceptions.CantFindSuchAnEntityException;
import exceptions.NotEnoughKnowledgeException;
import exceptions.ParserException;
import exceptions.WrongGrammarRuleException;
import grammar.AbstractEntityConcept;
import grammar.AbstractVerb;
import grammar.Adjective;
import grammar.DeclarativeSentence;
import grammar.DefiniteDeterminer;
import grammar.Designation;
import grammar.Determiner;
import grammar.Entity;
import grammar.EntityConcept;
import grammar.EntityInterrogative;
import grammar.IEntity;
import grammar.IndefiniteDeterminer;
import grammar.NounDesignation;
import grammar.Order;
import grammar.Sentence;
import grammar.Token;
import grammar.Token.TokenType;
import grammar.Tokenizer;
import grammar.Verb;
import grammar.VerbInterrogative;
import grammar.VerbMeaning;
import simplenlg.features.Tense;
import simplenlg.framework.LexicalCategory;
import simplenlg.lexicon.XMLLexicon;

public class GrammarParser {

	private LinkedList<Token> tokens;
	private Token lookAhead;
	private final List<Designation> actualAIVocabulary;
	private final List<Designation> newVocabulary;
	private final List<Entity> actualAIKnownEntities;
	private final List<Entity> newEntities;
	private final XMLLexicon lexicon;


	public GrammarParser(final List<Designation> AIvocabulary, final List<Entity> AIEntities, final XMLLexicon lexicon) {
		this.actualAIVocabulary = AIvocabulary;
		this.actualAIKnownEntities = AIEntities;
		this.newVocabulary = new LinkedList<Designation>();
		this.newEntities = new LinkedList<Entity>();
		this.lexicon = lexicon;
	}


	public Sentence parse(String input) throws ParserException, CantFindSuchAnEntityException {
		Tokenizer tokenizer = new Tokenizer();

		// Order of tokenizer.adding is important if some regexes are not exclusive
		tokenizer.add("\\?+", TokenType.QUESTION_MARK);
		tokenizer.add("[a-zA-ZçÇàÀùÙéÉèÈêÊëËôÔöÖîÎ_]+", TokenType.COMMON_WORD);

		tokenizer.tokenize(input);

		return parse(tokenizer.getTokens());
	}

	private Sentence parse(LinkedList<Token> tokens) throws ParserException, CantFindSuchAnEntityException {
		this.tokens = tokens;
		lookAhead = this.tokens.getFirst();

		Sentence res = null;
		try {
			res = start();
		} catch (NotEnoughKnowledgeException e) {
			System.err.println("AI lacks basic knowledge");
			e.printStackTrace();
		} catch (WrongGrammarRuleException e) { // Grammar didn't match
			throw new ParserException("I'm not programmed to understand this, sorry");
		}

		if (lookAhead.token != TokenType.EPSILON)
			throw new ParserException("Unexpected symbol \"" + lookAhead.sequence + "\" found");

		return res;
	}


	private void nextToken() {
		tokens.pop();
		// At the end of input we return an epsilon token
		if (tokens.isEmpty())
			lookAhead = new Token(TokenType.EPSILON, "");
		else
			lookAhead = tokens.getFirst();
	}


	/***************************************/
	/***************************************/
	/************ GRAMMAR RULES ************/
	/***************************************/
	/***************************************/


	/**
	 * Start -> order | declarativeSentence
	 * 
	 * Starting rule of the grammar
	 * @throws CantFindSuchAnEntityException 
	 */
	private Sentence start() throws NotEnoughKnowledgeException, WrongGrammarRuleException, CantFindSuchAnEntityException {
		Sentence res;

		try {
			res = order();
		} catch (WrongGrammarRuleException e) {
			res = declarativeSentence();
		}

		return res;
	}

	/**
	 * May be interrogative.
	 * 
	 * declarativeSentence -> nounPhrase verb nounPhrase QUESTION_MARK?
	 * @return
	 * @throws CantFindSuchAnEntityException 
	 */
	private DeclarativeSentence declarativeSentence() throws WrongGrammarRuleException, NotEnoughKnowledgeException, CantFindSuchAnEntityException {
		DeclarativeSentence res;

		IEntity subject = nounPhrase();
		AbstractVerb   verb    = verb();
		IEntity object  = nounPhrase();

		boolean interrogative = (lookAhead.token == TokenType.QUESTION_MARK);
		if (interrogative) {
			nextToken();
		}

		res = new DeclarativeSentence(subject, verb, object);
		res.setInterrogative(interrogative);

		return res;
	}

	/**
	 * verb -> WORD
	 * @return
	 */
	private AbstractVerb verb() throws WrongGrammarRuleException {
		AbstractVerb res;

		if (lookAhead.token == TokenType.COMMON_WORD) {
			res = processCorrespondingVerb();
			nextToken();
		} else {
			throw new WrongGrammarRuleException();
		}

		return res;
	}

	/**
	 * order -> KNOWN_VERB WORD | KNOWN_VERB
	 * @return
	 */
	private Order order() throws WrongGrammarRuleException, NotEnoughKnowledgeException {
		Order res;
		if (lookAhead.token == TokenType.COMMON_WORD && 
				lookAhead.sequence.matches(getKnownVerbsRegex())) {
			String verbSequence = lookAhead.sequence;
			String objectSequence = null;
			Verb designatedVerbalConcept = null;

			for (Designation d : getUpdatedVocabulary()) {
				if (d.getValue().equals(verbSequence) && d.getDesignatedConcept() instanceof Verb) {
					designatedVerbalConcept = (Verb) d.getDesignatedConcept();
					break;
				}
			}

			nextToken();

			if (lookAhead.token == TokenType.COMMON_WORD) {
				objectSequence = lookAhead.sequence;
				nextToken();
			} 
			res = new Order(designatedVerbalConcept, objectSequence);
		} else {
			throw new WrongGrammarRuleException();
		}
		return res;
	}

	/**
	 * nounPhrase -> determiner adjectives noun adjectives | interrogativeEntity
	 * 
	 * E.g : a nice small cat | what
	 * E.g : the what | who
	 * @throws CantFindSuchAnEntityException 
	 */
	private IEntity nounPhrase() throws WrongGrammarRuleException, NotEnoughKnowledgeException, CantFindSuchAnEntityException {
		IEntity res;

		try {
			List<Adjective> qualifiers = new LinkedList<Adjective>();
			Determiner determiner = determiner();
			
			qualifiers.addAll(adjectives());
			
			String nounSequence = noun();
			
			qualifiers.addAll(adjectives());
			
			res = processCorrespondingEntity(determiner, qualifiers, nounSequence);
		} catch (WrongGrammarRuleException e) {
			res = interrogativeEntity();
		}

		return res;
	}

	private Adjective adjective() throws WrongGrammarRuleException {
		Adjective res;

		if (lookAhead.token == TokenType.COMMON_WORD && isKnownAdjective(lookAhead.sequence)) {
			String designation = getBase(lookAhead.sequence, LexicalCategory.ADJECTIVE);
			Adjective adjectiveConcept = (Adjective) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), designation, Adjective.class);
			if (adjectiveConcept == null) {
				adjectiveConcept = new Adjective();
				this.newVocabulary.add(new Designation(designation, adjectiveConcept));
			}
			res = adjectiveConcept;
			nextToken();
		} else {
			throw new WrongGrammarRuleException();
		}

		return res;
	}
	
	/**
	 * 0 or more consecutive adjectives
	 * 
	 * adjectives -> adjective adjectives | EPSILON
	 */
	private List<Adjective> adjectives() {
		List<Adjective> res = new LinkedList<Adjective>();
		
		try {
			res.add(adjective());
			res.addAll(adjectives());
		} catch (WrongGrammarRuleException e) {
		}
		
		return res;
	}

	private EntityInterrogative interrogativeEntity() throws WrongGrammarRuleException {
		EntityInterrogative res;

		if (lookAhead.token == TokenType.COMMON_WORD) {
			res = processCorrespondingEntityInterrogative();
			nextToken();
		} else {
			throw new WrongGrammarRuleException();
		}

		return res;
	}

	private Determiner determiner() throws WrongGrammarRuleException, NotEnoughKnowledgeException {
		Determiner determiner;
		if (lookAhead.token == TokenType.COMMON_WORD && lookAhead.sequence.matches(getDeterminerRegex())) {
			determiner = (Determiner) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), lookAhead.sequence, Determiner.class);
			nextToken();
		} else {
			throw new WrongGrammarRuleException();
		}
		return determiner;
	}

	/**
	 * noun -> WORD
	 * @return
	 */
	private String noun() throws WrongGrammarRuleException {
		String res;
		if (lookAhead.token == TokenType.COMMON_WORD) {
			res = lookAhead.sequence;
			nextToken();
		} else {
			throw new WrongGrammarRuleException();
		}
		return res;
	}

	/***************************************/
	/***************************************/
	/**************** OTHER ****************/
	/***************************************/
	/***************************************/

	private boolean isKnownAdjective(String word) {
		return lexicon.hasWordFromVariant(word, LexicalCategory.ADJECTIVE);
	}

	private boolean isKnownNoun(String word) {
		return lexicon.hasWordFromVariant(word, LexicalCategory.NOUN);
	}

	private boolean isKnownVerb(String word) {
		return lexicon.hasWordFromVariant(word, LexicalCategory.VERB);
	}

	/**
	 * More or less returns the stem of the given word
	 */
	private String getBase(String word, LexicalCategory category) {
		return lexicon.getWordFromVariant(word, category).getBaseForm();
	}

	public List<Designation> getNewVocabulary() {
		return newVocabulary;
	}

	/**
	 * Returns the concatenation of the AI vocabulary and the current new vocabulary as a new list
	 */
	private List<Designation> getUpdatedVocabulary() {
		List<Designation> res = new LinkedList<Designation>();
		res.addAll(actualAIVocabulary);
		res.addAll(newVocabulary);
		return res;
	}

	/**
	 * Returns the concept corresponding to the given noun if it's in the AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
	 * @throws CantFindSuchAnEntityException 
	 */
	private IEntity processCorrespondingEntity(Determiner determiner, List<Adjective> qualifiers, String nounDesignation) throws CantFindSuchAnEntityException {
		IEntity res = null;
		if (isKnownNoun(nounDesignation))  {
			nounDesignation = getBase(nounDesignation, LexicalCategory.NOUN);
		}
		
		AbstractEntityConcept designatedConcept = (AbstractEntityConcept) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), nounDesignation, AbstractEntityConcept.class);//FIXME ne marche pas pour quoi + adjectif, non ?

		if (designatedConcept == null) { // Unknown word
			EntityConcept newConcept = new EntityConcept();
			
			Entity newEntity = new Entity(newConcept);
			newEntity.getCharacteristics().addAll(qualifiers);
			res = newEntity;
			this.newEntities.add(newEntity);
			
			NounDesignation newDesignation = new NounDesignation(nounDesignation, newConcept);
			newDesignation.setGender(determiner.getGender());
			newVocabulary.add(newDesignation);
		} else {
			if (designatedConcept instanceof EntityInterrogative) {
				EntityInterrogative interrogative = (EntityInterrogative) designatedConcept;
				res = interrogative;
			} else if (designatedConcept instanceof EntityConcept) {
				if (determiner instanceof IndefiniteDeterminer){
					Entity newEntity = new Entity((EntityConcept) designatedConcept);
					newEntity.getCharacteristics().addAll(qualifiers);
					res = newEntity;
					this.newEntities.add(newEntity);
				} else if (determiner instanceof DefiniteDeterminer) {
					res = getLastMentionOfA((EntityConcept) designatedConcept, qualifiers);
					if (res == null) {
						throw new CantFindSuchAnEntityException((EntityConcept) designatedConcept);
					}
				} else {
					System.err.println("There is a 3rd determiner class ? Not expected !");
				}		
			} else {
				System.err.println("A concept during the parsing is of neither expected classes. Some code needs an update");
			}
		}

		return res;
	}

	private EntityInterrogative processCorrespondingEntityInterrogative() throws WrongGrammarRuleException {
		EntityInterrogative res = null;
		String designation = lookAhead.sequence;
		AbstractEntityConcept designatedConcept = (AbstractEntityConcept) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), designation, AbstractEntityConcept.class);

		if (designatedConcept instanceof EntityInterrogative) {
			res = (EntityInterrogative) designatedConcept;
		} else {
			throw new WrongGrammarRuleException();
		}
		return res;
	}

	/**
	 * Returns the concept corresponding to the current token (it is implied that the current token represents a verb) if it's in the AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
	 */
	private AbstractVerb processCorrespondingVerb() {
		String designation = isKnownVerb(lookAhead.sequence) ? getBase(lookAhead.sequence, LexicalCategory.VERB) : lookAhead.sequence;

		AbstractVerb res, designatedConcept = (AbstractVerb) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), designation, AbstractVerb.class);

		if (designatedConcept == null) { // Unknown word
			AbstractVerb newConcept = new Verb(Tense.PRESENT, new VerbMeaning());
			res = newConcept;
			newVocabulary.add(new Designation(designation, newConcept));
		} else {
			res = designatedConcept;
		}

		return res;
	}



	/**
	 * Returns a regex of the form "(c1)|(c2)|...|(cn)", where c1...cn are all designations (strings) in the AI vocabulary that designate an object of the given class.
	 * @return Ex : "(eat)|(want)" if the parameter is {@link Verb}.class
	 */
	private String getDesignationRegex(Class<?> classDesignated) throws NotEnoughKnowledgeException {
		String res;

		LinkedList<String> temp = new LinkedList<String>();
		for (Designation d : getUpdatedVocabulary()) {
			if (classDesignated.isInstance(d.getDesignatedConcept())) {
				temp.add(d.getValue());
			}
		}

		if (temp.isEmpty()) {
			throw new NotEnoughKnowledgeException("AI should know some " + classDesignated.getName() + "s !");
		}
		res = "(" + temp.pop() + ")";
		for (String s : temp) {
			res += "|(" + s + ")";
		}

		return res;
	}

	/**
	 * @throws NotEnoughKnowledgeException if no determiner is known
	 */
	private String getDeterminerRegex() throws NotEnoughKnowledgeException {
		return getDesignationRegex(Determiner.class);
	}

	/**
	 * Does not include {@link VerbInterrogative} verbs
	 * @throws NotEnoughKnowledgeException if no verb is known
	 */
	private String getKnownVerbsRegex() throws NotEnoughKnowledgeException {
		return getDesignationRegex(Verb.class);
	}


	public List<Entity> getNewEntities() {
		return newEntities;
	}

	/**
	 * Returns the last created entity such that its concept equals the parameter and its adjectives are the same as the given list
	 */
	private Entity getLastMentionOfA(EntityConcept concept, List<Adjective> qualifiers) {
		Entity lastOccurence = null;

		for (Entity currEntity : this.actualAIKnownEntities) {
			if (currEntity.getConcept().equals(concept) && 
					currEntity.getCharacteristics().containsAll(qualifiers)) {
				lastOccurence = currEntity;
			}
		}

		return lastOccurence;
	}
}
