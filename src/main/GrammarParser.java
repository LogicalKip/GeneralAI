package main;

import java.util.LinkedList;
import java.util.List;

import exceptions.NotEnoughKnowledgeException;
import exceptions.ParserException;
import exceptions.WrongGrammarRuleException;
import grammar.AbstractEntity;
import grammar.AbstractVerb;
import grammar.DeclarativeSentence;
import grammar.Designation;
import grammar.Determiner;
import grammar.Entity;
import grammar.Gender;
import grammar.Order;
import grammar.Sentence;
import grammar.Token;
import grammar.Token.TokenType;
import grammar.Tokenizer;
import grammar.Verb;
import grammar.VerbInterrogative;
import grammar.VerbMeaning;
import simplenlg.features.Tense;

public class GrammarParser {

	private LinkedList<Token> tokens;
	private Token lookAhead;
	private final List<Designation> actualAIVocabulary;
	private final List<Designation> newVocabulary;


	public GrammarParser(final List<Designation> AIvocabulary) {
		this.actualAIVocabulary = AIvocabulary;
		this.newVocabulary = new LinkedList<Designation>();
	}


	public Sentence parse(String input) throws ParserException {
		Tokenizer tokenizer = new Tokenizer();

		// Order of tokenizer.adding is important if some regexes are not exclusive
		tokenizer.add("\\?+", TokenType.QUESTION_MARK);
		tokenizer.add("[a-zA-ZçÇàÀùÙéÉèÈêÊëËôÔöÖîÎ_]+", TokenType.COMMON_WORD);

		tokenizer.tokenize(input);

		return parse(tokenizer.getTokens());
	}

	private Sentence parse(LinkedList<Token> tokens) throws ParserException {
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
	 */
	private Sentence start() throws NotEnoughKnowledgeException, WrongGrammarRuleException {
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
	 */
	private DeclarativeSentence declarativeSentence() throws WrongGrammarRuleException, NotEnoughKnowledgeException {
		DeclarativeSentence res;

		AbstractEntity subject = nounPhrase();
		AbstractVerb   verb    = verb();
		AbstractEntity object  = nounPhrase();

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
		if (lookAhead.token == TokenType.COMMON_WORD && 
				lookAhead.sequence.matches(getKnownVerbsRegex())) {
			String verbSequence = lookAhead.sequence;
			String objectSequence = null;
			Verb designatedVerbalConcept = null;

			for (Designation d : actualAIVocabulary) {
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
			return new Order(designatedVerbalConcept, objectSequence);
		} else {
			throw new WrongGrammarRuleException();
		}

	}

	/**
	 * nounPhrase -> determiner noun | noun
	 */
	private AbstractEntity nounPhrase() throws WrongGrammarRuleException, NotEnoughKnowledgeException {
		AbstractEntity res;

		try {
			Gender determinerGender = determiner();
			res = noun();
			if (res instanceof Entity) {
				((Entity) res).setGender(determinerGender);
			}
		} catch (WrongGrammarRuleException e) {
			res = noun();
		}

		return res;
	}

	private Gender determiner() throws WrongGrammarRuleException, NotEnoughKnowledgeException {
		if (lookAhead.sequence.matches(getDeterminerRegex())) {
			Determiner determiner = (Determiner) AI.getFirstConceptDesignatedBy(actualAIVocabulary, lookAhead.sequence, Determiner.class);
			Gender determinerGender = determiner.getGender();
			nextToken();
			return determinerGender;
		} else {
			throw new WrongGrammarRuleException();
		}
	}

	/**
	 * noun -> WORD
	 * @return
	 */
	private AbstractEntity noun() throws WrongGrammarRuleException {
		AbstractEntity res;
		if (lookAhead.token == TokenType.COMMON_WORD) {
			res = processCorrespondingEntity();
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

	public List<Designation> getNewVocabulary() {
		return newVocabulary;
	}


	
	/**
	 * Returns the concept corresponding to the current token (it is implied that the current token represents an entity) if it's in the AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
	 */
	private AbstractEntity processCorrespondingEntity() {
		AbstractEntity res, designatedConcept = (AbstractEntity) AI.getFirstConceptDesignatedBy(actualAIVocabulary, lookAhead.sequence, AbstractEntity.class);

		if (designatedConcept == null) { // Unknown word
			AbstractEntity newConcept = new Entity();
			res = newConcept;
			newVocabulary.add(new Designation(lookAhead.sequence, newConcept));
		} else {
			res = designatedConcept;
		}

		return res;
	}

	/**
	 * Returns the concept corresponding to the current token (it is implied that the current token represents a verb) if it's in the AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
	 */
	private AbstractVerb processCorrespondingVerb() {
		AbstractVerb res, designatedConcept = (AbstractVerb) AI.getFirstConceptDesignatedBy(actualAIVocabulary, lookAhead.sequence, AbstractVerb.class);

		if (designatedConcept == null) { // Unknown word
			AbstractVerb newConcept = new Verb(Tense.PRESENT, new VerbMeaning());
			res = newConcept;
			newVocabulary.add(new Designation(lookAhead.sequence, newConcept));
		} else {
			res = designatedConcept;
		}

		return res;
	}


	
	/**
	 * Returns a regex of the form "(c1)|(c2)|...|(cn)", where c1...cn are all designations (strings) in the AI vocabulary that designate an object of the given class.
	 * @return Ex : "(eat)|(play)" if the parameter is Verb
	 */
	private String getDesignationRegex(Class<?> classDesignated) throws NotEnoughKnowledgeException {
		String res;

		LinkedList<String> temp = new LinkedList<String>();
		for (Designation d : actualAIVocabulary) {
			if (classDesignated.isInstance(d.getDesignatedConcept())) {
				temp.add(d.getValue());
			}
		}

		if (temp.isEmpty()) {
			throw new NotEnoughKnowledgeException("AI should know some " + classDesignated.getName() + "s !");
		} else {
			res = "(" + temp.pop() + ")";
			for (String s : temp) {
				res += "|(" + s + ")";
			}
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
}
