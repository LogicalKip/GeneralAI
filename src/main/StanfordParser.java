package main;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.Tree;
import exceptions.CantFindSuchAnEntityException;
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

	public Sentence parse(String input) throws CantFindSuchAnEntityException {
		this.newVocabulary = new LinkedList<Designation>();
		this.newEntities = new LinkedList<Entity>();

		Sentence res = new Order(null, null);

		for (List<HasWord> sentence : new DocumentPreprocessor(new StringReader(input))) {
			Tree parsedSentence = lp.apply(sentence);

			parsedSentence.pennPrint();
			Tree[] children = parsedSentence.children()[0].children();

			for (Tree c : children) {
				System.out.println(c.value());
			}
			
			if (children.length >= 3 &&
					children[0].value().equals("NP") &&
					children[1].value().equals("VN") &&
					children[2].value().equals("NP")) {

				String subjectString = getLeaf(children[0], getNounValues());
				String subjectDeterminerString = getLeaf(children[0], getDeterminerValues());
				String verbString = getLeaf(children[1], getVerbValues());
				String objectString = getLeaf(children[2], getNounValues());
				String objectDeterminerString = getLeaf(children[2], getDeterminerValues());

				Determiner subjectDeterminer = (Determiner) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), subjectDeterminerString, Determiner.class);
				Determiner objectDeterminer = (Determiner) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), objectDeterminerString, Determiner.class);
				
				IEntity subject = processCorrespondingEntity(subjectDeterminer, new LinkedList<Adjective>(), subjectString);
				AbstractVerb verb = processCorrespondingVerb(verbString);
				IEntity object = processCorrespondingEntity(objectDeterminer, new LinkedList<Adjective>(), objectString);

				DeclarativeSentence decl = new DeclarativeSentence(subject, verb, object);
				decl.setInterrogative(isInterrogationMark(children[children.length-1]));
				res = decl;
				
				
				System.out.println("Phrase acceptée");
				System.out.println(children[0].yield() + " : " + subjectString);
				System.out.println(children[2].yield() + " : " + objectString);
				System.out.println("Verbe : " + verbString);
			} else {
				System.out.println("Phrase incorrecte");
			}
			
		}

		return res;
	}
	
	private boolean isInterrogationMark(Tree t) {
		return t.value().equals("PUNC") && t.children()[0].value().equals("?");
	}
	
	/**
	 * If given an array to recognize which leaves are nouns (ex : {"NC"}), will return the first (presumably only) leaf that is the noun in the given tree
	 * @return null if none could be found
	 */
	private String getLeaf(Tree tree, String[] values) {
		Tree[] children = tree.children();
		
		for (String value : values) {
			if (tree.value().equals(value)) {
				return children[0].value();
			}
		}

		if (tree.children().length > 0) {
			for (Tree child : children) {
				String res = getLeaf(child, values);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}
	
	private String[] getNounValues() {
		String[] res = {"NC", "N"};
		return res;
	}
	
	private String[] getVerbValues() {
		String[] res = {"V"};
		return res;
	}
	
	private String[] getDeterminerValues() {
		String[] res = {"DET"};
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
