package main;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.Tree;
import exceptions.CantFindSuchAnEntityException;
import exceptions.WrongGrammarRuleException;
import grammar.AbstractEntityConcept;
import grammar.AbstractVerb;
import grammar.Adjective;
import grammar.Adverb;
import grammar.DeclarativeSentence;
import grammar.DefiniteDeterminer;
import grammar.Designation;
import grammar.Determiner;
import grammar.Entity;
import grammar.EntityConcept;
import grammar.EntityInterrogative;
import grammar.IEntity;
import grammar.IndefiniteDeterminer;
import grammar.Myself;
import grammar.Not;
import grammar.NounDesignation;
import grammar.Order;
import grammar.Sentence;
import grammar.SimpleSentence;
import grammar.StativeSentence;
import grammar.StativeVerb;
import grammar.User;
import grammar.Verb;
import simplenlg.framework.LexicalCategory;
import simplenlg.lexicon.XMLLexicon;

/**
 * Parses user input into a {@link Sentence}, using the Stanford Natural Language Processing library and a (sort of) grammar
 */
public class StanfordParser {

	private LexicalizedParser lp;
	private List<Designation> newVocabulary;
	private List<Entity> newEntities;
	private List<Designation> actualAIVocabulary;
	private List<Entity> actualAIKnownEntities;
	private XMLLexicon lexicon;

	public StanfordParser(String parserModel, List<Designation> AIvocabulary, List<Entity> AIEntities, XMLLexicon lex) {
		lp = LexicalizedParser.loadModel(parserModel);
		this.actualAIVocabulary = AIvocabulary;
		this.actualAIKnownEntities = AIEntities;
		this.newVocabulary = new LinkedList<Designation>();
		this.newEntities = new LinkedList<Entity>();
		this.lexicon = lex;
	}

	public Sentence parse(String input) throws CantFindSuchAnEntityException, WrongGrammarRuleException {
		this.newVocabulary = new LinkedList<Designation>();
		this.newEntities = new LinkedList<Entity>();

		Sentence res = null;

		for (List<HasWord> sentence : new DocumentPreprocessor(new StringReader(input))) { // for when several sentences are possible
			Tree parsedSentence = lp.apply(sentence);
			return start(parsedSentence.children()[0]); // First child to skip the ROOT part of the tree
		}

		return res;
	}


	/**
	 * The very first rule of the grammar. Represents every sentence the AI may understand
	 * start -> order | declarativeSentence
	 */
	private Sentence start(Tree t) throws WrongGrammarRuleException, CantFindSuchAnEntityException {
//		t.pennPrint(); // Uncomment to display the tree, for debug or R&D purposes
		try {
			return order(t);
		} catch (WrongGrammarRuleException e) {
			try {
				return relativeClauseSentence(t);
			} catch (WrongGrammarRuleException e2) {
				return simpleSentence(t);
			}
		}
	}

	/**
	 * A relative clause with a missing subject and a question mark can be seen as a sentence, 
	 * because it can be guessed that the user would want to know the subject.
	 * (I see the cat) [who eats a mouse] -> WHAT_ENTITY eats a mouse ?
	 */
	private Sentence relativeClauseSentence(Tree t) throws WrongGrammarRuleException, CantFindSuchAnEntityException {
		if (t.value().equals("SENT")) {
			Tree[] children = t.children();
			int i = 0;

			@SuppressWarnings("unused") // May want to test if the pronoun is "who", "where", etc to give an adequate answer later. 
			String relativePronoun = getLeaf(children[i], "PROREL"); // If there isn't any pronoun, an exception is thrown
			i++;

			AbstractVerb verb = processCorrespondingVerb(getVerb(children[i]));
			i++;

			boolean negative;
			try {
				negation(children[i]);
				i++;
				negative = true;
			} catch (WrongGrammarRuleException e) {
				negative = false;
			}

			SimpleSentence res;


			try {
				if (!(verb instanceof StativeVerb)) {
					throw new WrongGrammarRuleException();
				}
				Adjective adjective = processCorrespondingAdjective(singleAdjective(children[i]));
				i++;


				res = new StativeSentence(EntityInterrogative.getInstance(), (StativeVerb) verb, (Adjective) adjective);
			} catch (WrongGrammarRuleException e) {
				IEntity object = NP(children[i]);
				i++;

				res = new DeclarativeSentence(EntityInterrogative.getInstance(), verb, object);
			}

			res.setNegative(negative);

			if (! isInterrogationMark(children[i])) {
				throw new WrongGrammarRuleException();
			}
			res.setInterrogative(true);

			return res;
		}
		throw new WrongGrammarRuleException();
	}

	private Order order(Tree t) throws WrongGrammarRuleException {
		if (t.value().equals("SENT")) {
			Order res;
			Tree[] children = t.children();
			Verb orderVerb = infinitiveVerbalPhrase(children[0]);
			String object = null;
			try {
				object = getLeaf(children[0], getNounValues());
			} catch (WrongGrammarRuleException e1) {
			}
			res = new Order(orderVerb, object); 

			return res;
		}
		throw new WrongGrammarRuleException();
	}

	private String singleAdjective(Tree t) throws WrongGrammarRuleException {
		if ((t.value().equals("AP") || t.value().equals("NP")) && 
				t.children().length == 1 && 
				t.children()[0].value().equals("ADJ")) {
			return t.children()[0].children()[0].value();
		} 

		throw new WrongGrammarRuleException();
	}

	private SimpleSentence simpleSentence(Tree t) throws WrongGrammarRuleException, CantFindSuchAnEntityException {
		try {
			return pronounSubjectSentence(t);
		} catch (WrongGrammarRuleException e) {
			return nominalGroupSubjectSentence(t);
		}
	}

	/**
	 * A sentence that starts with a pronoun, such as "I want a cat" or "you are a cat". May not include cats
	 */
	private SimpleSentence pronounSubjectSentence(Tree t) throws WrongGrammarRuleException, CantFindSuchAnEntityException {
		if (t.value().equals("SENT")) {
			SimpleSentence res;
			Tree[] children = t.children();

			if (children.length == 1 && children[0].value().equals("COORD")) {
				children = children[0].children();
			}

			if (children.length < 2 || ! children[0].value().equals("VN")) {
				throw new WrongGrammarRuleException();
			}

			int i = 0;

			Entity subject = getEntityFromPronoun(getLeaf(children[i], "CLS"));
			AbstractVerb verb = processCorrespondingVerb(getVerb(children[i]));
			i++;

			boolean negative; 
			try {
				negation(children[i]);
				negative = true;
				i++;
			} catch (WrongGrammarRuleException e) {
				negative = false;
			}

			if (verb instanceof StativeVerb) {
				try {
					String adjective = singleAdjective(children[i]);
					res = new StativeSentence(subject, (StativeVerb) verb, processCorrespondingAdjective(adjective));
				} catch (WrongGrammarRuleException e) {
					IEntity object = NP(children[i]);
					res = new DeclarativeSentence(subject, verb, object);
				}
			} else {
				IEntity object = NP(children[i]);
				res = new DeclarativeSentence(subject, verb, object);
			}

			res.setInterrogative(isInterrogationMark(children[children.length-1]));

			res.setNegative(negative);
			return res;
		}
		throw new WrongGrammarRuleException();
	}


	/**
	 * A sentence that starts with a nominal group, such as "the cat eats the mouse" or "the cat is nice"
	 */
	private SimpleSentence nominalGroupSubjectSentence(Tree t) throws WrongGrammarRuleException, CantFindSuchAnEntityException {
		if (t.value().equals("SENT")) {
			SimpleSentence res;
			Tree[] children = t.children();
			if (children.length < 3) {
				throw new WrongGrammarRuleException();
			}
			
			int i = 0;

			IEntity subject = NP(children[i]);
			i++;

			AbstractVerb verb = processCorrespondingVerb(getVerb(children[i]));
			i++;

			boolean negative; 
			try {
				negation(children[i]);
				negative = true;
				i++;
			} catch (WrongGrammarRuleException e) {
				negative = false;
			}

			if (verb instanceof StativeVerb) {
				try {
					String adjective = singleAdjective(children[i]);
					res = new StativeSentence(subject, (StativeVerb) verb, processCorrespondingAdjective(adjective));
				} catch (WrongGrammarRuleException e) {
					IEntity object = NP(children[i]);
					res = new DeclarativeSentence(subject, verb, object);
				}
			} else {
				IEntity object = NP(children[i]);
				res = new DeclarativeSentence(subject, verb, object);
			}

			res.setInterrogative(isInterrogationMark(children[children.length-1]));

			res.setNegative(negative);
			return res;
		}
		throw new WrongGrammarRuleException();
	}

	private void negation(Tree t) throws WrongGrammarRuleException {
		if (t.value().equals("ADV")) {
			Tree[] children = t.children();
			if (AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), children[0].value(), Adverb.class)
					.equals(Not.getInstance())) {
				return;
			}
		}
		throw new WrongGrammarRuleException();
	}

	private Verb infinitiveVerbalPhrase(Tree t) throws WrongGrammarRuleException {
		if (t.value().equals("VPinf")) {
			String verb = getLeaf(t, "VINF");
			String verbBase = isKnownVerb(verb) ? getBase(verb, LexicalCategory.VERB) : verb;
			Designation res = AI.getFirstDesignationFrom(getUpdatedVocabulary(), verbBase, Verb.class);
			if (res == null) {
				throw new WrongGrammarRuleException();
			}

			res.incrementTimesUserUsedIt();
			return ((Verb)res.getDesignatedConcept());
		}
		throw new WrongGrammarRuleException();
	}

	/**
	 * Returns the entity mentioned by that nominal group
	 */
	private IEntity NP(Tree t) throws CantFindSuchAnEntityException, WrongGrammarRuleException {
		if (t.value().equals("NP")) {
			String noun;
			try {
				noun = getNoun(t);
				Determiner determiner = (Determiner) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), getDeterminer(t), Determiner.class);

				List<Adjective> adjectives = new LinkedList<Adjective>();
				for (String s : getLeaves(t, getAdjectivesValues())) {
					Adjective adjectiveConcept = (Adjective) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), s, Adjective.class);
					if (adjectiveConcept == null) {
						adjectiveConcept = new Adjective();
						this.newVocabulary.add(new Designation(s, adjectiveConcept));//TODO word is not in base : problem ?
					}
					adjectives.add(adjectiveConcept);
				}
				return processCorrespondingEntity(determiner, adjectives, noun);
			} catch (WrongGrammarRuleException e) {
				return processCorrespondingEntityInterrogative(getLeaf(t, getProWHValues()));
			}
		}
		throw new WrongGrammarRuleException();
	}

	private boolean isInterrogationMark(Tree t) {
		return t.value().equals("PUNC") && t.children()[0].value().equals("?");
	}

	/**
	 * If given an array to recognize which leaves are nouns (ex : {"NC"}), will return the leaves that are nouns in the given tree
	 */
	private List<String> getLeaves(Tree tree, String[] values) {
		Tree[] children = tree.children();
		List<String> res = new LinkedList<String>();

		for (String value : values) {
			if (tree.value().equals(value)) {
				res.add(children[0].value());
				return res;
			}
		}

		for (Tree child : children) {
			res.addAll(getLeaves(child, values));
		}

		return res;
	}


	/**
	 * If given an array to recognize which leaves are nouns (ex : {"NC"}), will return the first leaf that is a noun in the given tree. Same with verbs, or anything given in the array
	 * @throws WrongGrammarRuleException 
	 * @return Does not return the value that matched (ex : "NC"), but its son, which should be a leaf (ex : "cat")
	 */
	private String getLeaf(Tree tree, String[] values) throws WrongGrammarRuleException {
		List<String> leaves = getLeaves(tree, values);
		if (leaves.isEmpty()) {
			throw new WrongGrammarRuleException();
		}
		return leaves.get(0);
	}

	/**
	 * Simpler method to call {@link StanfordParser#getLeaf(Tree, String[])} with only one value
	 */
	private String getLeaf(Tree tree, String value) throws WrongGrammarRuleException {
		String[] tab = {value};
		return getLeaf(tree, tab);
	}

	private String getNoun(Tree tree) throws WrongGrammarRuleException {
		return getLeaf(tree, getNounValues());
	}

	private String getVerb(Tree tree) throws WrongGrammarRuleException {
		return getLeaf(tree, getVerbValues());
	}

	private String getDeterminer(Tree tree) throws WrongGrammarRuleException {
		return getLeaf(tree, getDeterminerValues());
	}

	private String[] getNounValues() {
		String[] res = {"NC", "N"};
		return res;
	}

	private String[] getVerbValues() {
		String[] res = {"V"};
		return res;
	}

	private String[] getProWHValues() {
		String[] res = {"PROWH"};
		return res;
	}

	private String[] getDeterminerValues() {
		String[] res = {"DET"};
		return res;
	}

	private String[] getAdjectivesValues() {
		String[] res = {"ADJ"};
		return res;
	}

	public List<Entity> getNewEntities() {
		return this.newEntities;
	}

	public List<Designation> getNewVocabulary() {
		return this.newVocabulary;
	}

	/**
	 * Returns the last created entity such that its concept equals the parameter and its adjectives match the given list
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
	 * Returns the entity corresponding to the given parameters if the AI knows about it, or adds an entry with a new entity and/or concept in the new vocabulary/entities otherwise.
	 */
	private IEntity processCorrespondingEntity(Determiner determiner, List<Adjective> qualifiers, String nounDesignation) throws CantFindSuchAnEntityException {
		IEntity res = null;
		if (isKnownNoun(nounDesignation))  {
			nounDesignation = getBase(nounDesignation, LexicalCategory.NOUN);
		}

		Designation designation = AI.getFirstDesignationFrom(getUpdatedVocabulary(), nounDesignation, AbstractEntityConcept.class);

		if (designation == null) { // Unknown word
			EntityConcept newConcept = new EntityConcept();

			Entity newEntity = new Entity(newConcept);
			newEntity.getCharacteristics().addAll(qualifiers);
			res = newEntity;
			this.newEntities.add(newEntity);

			NounDesignation newDesignation = new NounDesignation(nounDesignation, newConcept);
			newDesignation.incrementTimesUserUsedIt();
			newDesignation.setGender(determiner.getGender());
			newVocabulary.add(newDesignation);
		} else {
			AbstractEntityConcept designatedConcept = (AbstractEntityConcept) designation.getDesignatedConcept();
			designation.incrementTimesUserUsedIt();

			if (designatedConcept instanceof EntityInterrogative) {
				res = (EntityInterrogative) designatedConcept;
			} else if (designatedConcept instanceof EntityConcept) {
				if (determiner instanceof IndefiniteDeterminer){
					Entity newEntity = new Entity((EntityConcept) designatedConcept);
					newEntity.getCharacteristics().addAll(qualifiers);
					res = newEntity;
					this.newEntities.add(newEntity);
				} else if (determiner instanceof DefiniteDeterminer) {
					res = getLastMentionOfA((EntityConcept) designatedConcept, qualifiers);
					if (res == null) {
						throw new CantFindSuchAnEntityException((EntityConcept) designatedConcept, getDesignationsOf(qualifiers, getUpdatedVocabulary()));
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

	private static List<String> getDesignationsOf(List<Adjective> adjectives, List<Designation> vocab) {
		List<String> res = new LinkedList<String>();

		for (Designation d : vocab) {
			if (adjectives.contains(d.getDesignatedConcept())) {
				res.add(d.getValue());
			}
		}
		return res;
	}

	private EntityInterrogative processCorrespondingEntityInterrogative(String designation) throws WrongGrammarRuleException {
		EntityInterrogative res = null;
		AbstractEntityConcept designatedConcept = (AbstractEntityConcept) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), designation, AbstractEntityConcept.class);

		if (designatedConcept instanceof EntityInterrogative) {
			res = (EntityInterrogative) designatedConcept;
		} else {
			throw new WrongGrammarRuleException();
		}
		return res;
	}

	private Entity getEntityFromPronoun(String pronoun) {
		if (isSecondSingularPersonalPronoun(pronoun)) {
			return Myself.getInstance();
		} else if (isFirstSingularPersonalPronoun(pronoun)) {
			return User.getInstance();
		}
		return null;
	}

	/**
	 * Returns the concept corresponding to the parameter (it is implied that it represents a verb) if it's in the AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
	 */
	private AbstractVerb processCorrespondingVerb(String verb) {
		String designation = isKnownVerb(verb) ? getBase(verb, LexicalCategory.VERB) : verb;

		AbstractVerb res;
		Designation d = AI.getFirstDesignationFrom(getUpdatedVocabulary(), designation, AbstractVerb.class);

		if (d == null) { // Unknown word
			AbstractVerb newConcept = new Verb();
			res = newConcept;
			newVocabulary.add(new Designation(designation, newConcept));
		} else {
			res = (AbstractVerb) d.getDesignatedConcept();
		}

		return res;
	}


	/**
	 * Returns the concept corresponding to the parameter (it is implied that it represents an adjective) if it's in the AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
	 */
	private Adjective processCorrespondingAdjective(String adjective) {
		String designation = isKnownAdjective(adjective) ? getBase(adjective, LexicalCategory.ADJECTIVE) : adjective;

		Adjective res;
		Designation d = AI.getFirstDesignationFrom(getUpdatedVocabulary(), designation, Adjective.class);

		if (d == null) { // Unknown word
			Adjective newConcept = new Adjective();
			res = newConcept;
			newVocabulary.add(new Designation(designation, newConcept));
		} else {
			res = (Adjective) d.getDesignatedConcept();
		}

		return res;
	}

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
	 * @return True if the given word is the equivalent of "I" in the current language
	 */
	public boolean isFirstSingularPersonalPronoun(String word) {//TODO word is not in base : problem ?
		return Translator.getBaseFirstSingularPersonalPronoun(lexicon).equals(word);
	}

	/**
	 * @return True if the given word is the equivalent of "You" (as a subject) in the current language
	 */
	public boolean isSecondSingularPersonalPronoun(String word) {//TODO word is not in base : problem ?
		return Translator.getBaseSecondSingularPersonalPronoun(lexicon).equals(word);
	}


	/**
	 * More or less returns the stem of the given word
	 */
	private String getBase(String word, LexicalCategory category) {
		return lexicon.getWordFromVariant(word, category).getBaseForm();
	}
}
