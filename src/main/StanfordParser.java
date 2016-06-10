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
import grammar.Verb;
import grammar.VerbMeaning;
import simplenlg.features.Tense;

public class StanfordParser {

	/*
	 * TODO getBase()
	 */

	private LexicalizedParser lp;
	private List<Designation> newVocabulary;
	private List<Entity> newEntities;
	private List<Designation> actualAIVocabulary;
	private List<Entity> actualAIKnownEntities;

	public StanfordParser(String parserModel, List<Designation> AIvocabulary, List<Entity> AIEntities) {
		lp = LexicalizedParser.loadModel(parserModel);
		this.actualAIVocabulary = AIvocabulary;
		this.actualAIKnownEntities = AIEntities;
		this.newVocabulary = new LinkedList<Designation>();
		this.newEntities = new LinkedList<Entity>();
	}

	public Sentence parse(String input) throws CantFindSuchAnEntityException, WrongGrammarRuleException {
		this.newVocabulary = new LinkedList<Designation>();
		this.newEntities = new LinkedList<Entity>();

		Sentence res = null;

		for (List<HasWord> sentence : new DocumentPreprocessor(new StringReader(input))) { // for when several sentences are possible
			Tree parsedSentence = lp.apply(sentence);

			parsedSentence.pennPrint();

			return sentence(parsedSentence.children()[0]); // Skip the ROOT part of the tree
		}

		return res;
	}

	private Sentence sentence(Tree t) throws WrongGrammarRuleException, CantFindSuchAnEntityException {
		Tree[] children = t.children();
		if (t.value().equals("SENT") && children.length >= 3) {
			IEntity subject = NP(children[0]);

			AbstractVerb verb = processCorrespondingVerb(getVerb(t));

			IEntity object = NP(children[2]);

			DeclarativeSentence res = new DeclarativeSentence(subject, verb, object);
			res.setInterrogative(isInterrogationMark(children[children.length-1]));
			
			return res;
		} else {
			throw new WrongGrammarRuleException();
		}
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
					Adjective adjectiveConcept = (Adjective)AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), s, Adjective.class);
					if (adjectiveConcept == null) {
						adjectiveConcept = new Adjective();
						this.newVocabulary.add(new Designation(s, adjectiveConcept));
					}
					adjectives.add(adjectiveConcept);
				}
				return processCorrespondingEntity(determiner, adjectives, noun);
			} catch (WrongGrammarRuleException e) {
				return processCorrespondingEntityInterrogative(getLeaf(t, getProWHValues()));
			}
		} else {
			throw new WrongGrammarRuleException();
		}
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
	 * If given an array to recognize which leaves are nouns (ex : {"NC"}), will return the first leaf that is a noun in the given tree
	 * @throws WrongGrammarRuleException 
	 */
	private String getLeaf(Tree tree, String[] values) throws WrongGrammarRuleException {
		List<String> leaves = getLeaves(tree, values);
		if (leaves.isEmpty()) {
			throw new WrongGrammarRuleException();
		} else {
			return leaves.get(0);
		}
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
		/* FIXME	if (isKnownNoun(nounDesignation))  {
			nounDesignation = getBase(nounDesignation, LexicalCategory.NOUN);
		}
		 */

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


	/**
	 * Returns the concept corresponding to the current token (it is implied that the current token represents a verb) if it's in the AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
	 */
	private AbstractVerb processCorrespondingVerb(String verb) {
		String designation = /*FIXME isKnownVerb(verb) ? getBase(verb, LexicalCategory.VERB) : */verb;

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
}
