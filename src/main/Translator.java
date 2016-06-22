package main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import exceptions.NotEnoughKnowledgeException;
import grammar.AbstractConcept;
import grammar.AbstractEntityConcept;
import grammar.Adjective;
import grammar.DeclarativeSentence;
import grammar.DefiniteDeterminer;
import grammar.Designation;
import grammar.Determiner;
import grammar.Entity;
import grammar.EntityConcept;
import grammar.EntityInterrogative;
import grammar.Gender;
import grammar.IEntity;
import grammar.IndefiniteDeterminer;
import grammar.Myself;
import grammar.NounDesignation;
import grammar.StartSoftware;
import grammar.Stop;
import grammar.Understand;
import grammar.User;
import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.InterrogativeType;
import simplenlg.features.NumberAgreement;
import simplenlg.features.Person;
import simplenlg.features.Tense;
import simplenlg.framework.CoordinatedPhraseElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.NLGElement;
import simplenlg.framework.NLGFactory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.XMLLexicon;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.phrasespec.VPPhraseSpec;
import simplenlg.realiser.Realiser;

/**
 * Turns the internal, abstract concepts of the AI in something readable to the user.
 * Linked to a human language, it's the one that knows that language's vocabulary and special rules
 * @author charles
 *
 */
public abstract class Translator {
	protected AI ai;
	protected List<Designation> vocabulary;

	private final String languageName;
	private Lexicon lexicon;
	private NLGFactory nlgFactory;
	private Realiser realiser;


	/**
	 * @param language In its own language : "français", "english", "deutsch", etc
	 */
	public Translator(AI ai, String language) {
		super();
		this.ai = ai;
		this.languageName = language;

		this.vocabulary = new LinkedList<Designation>();
		this.addBasicVocabulary();
		this.lexicon = getXMLLexicon();
		this.nlgFactory = new NLGFactory(lexicon);
		this.realiser = new Realiser();
	}

	/**
	 * Creates a minimal vocabulary from the subclass language, by adding new {@link Designation}s in {@link Translator#vocabulary}
	 */
	protected abstract void addBasicVocabulary();

	/**
	 * Context : when asked to explain or define something, the AI uses a shell script that can take a language parameter such as "fr", "de", "en", etc. It will then returns the definition in that language, for a given word (considered in that language).
	 * 
	 * Override this method to get your definitions in a language other than the default one. See the script for more info.
	 */
	public String getLanguageParameterForGetDefProgram() {
		return "";
	}

	public String getLanguageName() {
		return languageName;
	}

	public List<Designation> getVocabulary() {
		return vocabulary;
	} 

	public void say(String stringToSay) {
		System.out.println("[AI] " + stringToSay);
	}



	/**
	 * Display an abstract sentence in a way the user can understand, thanks to the language provided by the subclass
	 */
	public void say(DeclarativeSentence sentence) {
		say(realiser.realiseSentence(parseSentence(sentence, true)));
	}
	
	public void say(NLGElement sentence) {
		say(realiser.realiseSentence(sentence));
	}

	public void say(List<DeclarativeSentence> sentences) {
		CoordinatedPhraseElement coordination = nlgFactory.createCoordinatedPhrase();
		for (DeclarativeSentence s : sentences) {
			coordination.addCoordinate(parseSentence(s, false));
		}
		say(realiser.realiseSentence(addApostrophe(coordination)));
	}
	
	private NLGElement addApostrophe(NLGElement e) {
		CoordinatedPhraseElement politeCoordination = nlgFactory.createCoordinatedPhrase();
		politeCoordination.addCoordinate(e);
		politeCoordination.addCoordinate(getDefaultUserApostrophe());
		politeCoordination.setConjunction("");
		return politeCoordination;
	}

	/**
	 * Probably something like "edu/stanford/nlp/models/lexparser/SOME_MODEL.ser.gz"
	 */
	public abstract String getStanfordParserModelFilename();

	/**
	 * From a {@link DeclarativeSentence}, makes an object that can be computed by the simpleNLG library, and possibly adds some features to improve the result
	 */
	private NLGElement parseSentence(DeclarativeSentence sentence, boolean addApostrophe) {
		IEntity s = sentence.getSubject();
		IEntity o = sentence.getObject();

		NLGElement p = nlgFactory.createClause(
				computeEntityString(s),
				getBestDesignation(sentence.getVerb()), 
				computeEntityString(o));
		
		p.setFeature(Feature.NEGATED, sentence.isNegative());
		p.setFeature(Feature.TENSE, sentence.getTense());
		
		if (sentence.isInterrogative()) {
			if (sentence.getSubject().equals(EntityInterrogative.getInstance())) {
				p.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_SUBJECT);
			}
			if (sentence.getObject().equals(EntityInterrogative.getInstance())) {
				p.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);// Or InterrogativeType.WHO_OBJECT
			}
		} else if (addApostrophe) {		
			p = addApostrophe(p);
		}
		
		return p;
	}
	
	public SPhraseSpec getSoftwareStartedSentence(String softwareName) throws NotEnoughKnowledgeException {
		SPhraseSpec res = nlgFactory.createClause(null, getBestDesignation(StartSoftware.getInstance()), softwareName);
		res.setFeature(Feature.PASSIVE, true);
		return res;
	}
	
	public SPhraseSpec getSoftwareStoppedSentence(String softwareName) throws NotEnoughKnowledgeException {
		SPhraseSpec res = nlgFactory.createClause(null, getBestDesignation(Stop.getInstance()), softwareName);
		res.setFeature(Feature.PASSIVE, true);
		res.setFeature(Feature.TENSE, Tense.PAST);
		return res;
	}
	
	public VPPhraseSpec getUnderstoodSentence() {
		VPPhraseSpec res = nlgFactory.createVerbPhrase(getBestDesignation(Understand.getInstance()));
		res.setFeature(Feature.FORM, Form.PAST_PARTICIPLE);
		return res;
	}


	/**
	 * More or less toString() in the current language
	 * @return How the entity will be represented as a String after some SimpleNLG processing.
	 */
	public String computeEntityString(IEntity entityParam, boolean definiteDeterminer) {
		String res;

		if (entityParam instanceof Entity) {
			Entity entity = (Entity) entityParam;

			if (entity.equals(Myself.getInstance())) {
				res = getBaseFirstSingularPersonalPronoun(lexicon);
			} else if (entity.equals(User.getInstance())) {
				res = getPolitenessPersonalPronoun();
			} else {
				NPPhraseSpec element = nlgFactory.createNounPhrase(
						definiteDeterminer ? getDefiniteDeterminerFor(entity) : getIndefiniteDeterminerFor(entity),
								getBestDesignation(entity.getConcept()));
				for (Adjective qualifier : entity.getCharacteristics()) {
					element.addModifier(getBestDesignation(qualifier));
				}

				res = realiser.realise(element).getRealisation();
			}
		} else {
			res = getBestDesignation((AbstractEntityConcept) entityParam);
		}
		return res;
	}

	public String computeEntityString(EntityConcept concept, List<String> qualifiers) {
		String res;

		NPPhraseSpec element = nlgFactory.createNounPhrase(
				getDeterminerFor(concept, false), 
				getBestDesignation(concept));
		for (String qualifier : qualifiers) {
			AbstractConcept qualifierConcept = AI.getFirstConceptDesignatedBy(getVocabulary(), qualifier, Adjective.class);
			String qualifierString;
			if (qualifierConcept == null) {
				qualifierString = qualifier;
			} else {
				qualifierString = getBestDesignation(qualifierConcept);
			}
			element.addModifier(qualifierString);
		}

		res = realiser.realise(element).getRealisation();

		return res;
	}

	/**
	 * More or less toString() in the current language
	 * @return How the entity will be represented as a String after some SimpleNLG processing.
	 */
	public String computeEntityString(IEntity entityParam) {
		return computeEntityString(entityParam, true);
	}

	/**
	 * Returns a determiner that fits the gender of one of the entity's designations
	 */
	private String getDeterminerFor(Entity entity, boolean definiteDeterminer) {
		return getDeterminerFor(entity.getConcept(), definiteDeterminer);
	}
	
	/**
	 * Returns a determiner that fits the gender of one of the entity's designations
	 */
	private String getDeterminerFor(EntityConcept entityConcept, boolean definiteDeterminer) {
		String determiner = "";
		for (Designation currDeterminer : this.vocabulary) {
			if (definiteDeterminer ? currDeterminer.getDesignatedConcept() instanceof DefiniteDeterminer
					: currDeterminer.getDesignatedConcept() instanceof IndefiniteDeterminer) {
				Gender determinerGender = ((Determiner) currDeterminer.getDesignatedConcept()).getGender();
				for (Designation currEntityDesignation : getDesignations(entityConcept)) {
					NounDesignation currNounDesignation = (NounDesignation) currEntityDesignation;
					if (currNounDesignation.getGender().equals(determinerGender)) {
						return currDeterminer.getValue();
					}
				}
			}
		}
		return determiner;
	}

	/**
	 * Returns a definite determiner that fits the gender of one of the entity's designations
	 */
	private String getDefiniteDeterminerFor(Entity entity) {
		return getDeterminerFor(entity, true);
	}

	/**
	 * Returns an indefinite determiner that fits the gender of one of the entity's designations
	 */
	private String getIndefiniteDeterminerFor(Entity entity) {
		return getDeterminerFor(entity, false);
	}

	public abstract XMLLexicon getXMLLexicon();

	private String getBestDesignation(AbstractConcept concept) {
		int max = -1;
		String res = "PROBLEM";
		if (concept == null) {
			res = null;
		} else {
			for (Designation d : getDesignations(concept)) {
				if (d.getTimesUserUsedIt() > max) {
					max = d.getTimesUserUsedIt();
					res = d.getValue();
				}
			}
		}
		return res;
	}

	public List<Designation> getDesignations(AbstractConcept concept) {
		List<Designation> res = new LinkedList<Designation>();
		for (Designation designation : this.vocabulary) {
			if (designation.getDesignatedConcept().equals(concept)) {
				res.add(designation);
			}
		}
		return res;
	}
	
	/**
	 * returns the first designation of given concept, throwing an exception if none is known
	 */
	public Designation getFirstDesignation(AbstractConcept concept) throws NotEnoughKnowledgeException {
		List<Designation> d = getDesignations(concept);
		if (d.isEmpty()) {
			throw new NotEnoughKnowledgeException("AI doesn't know a designation for " + concept);
		} else {
			return d.get(0);
		}
	}

	/**
	 * Returns "you" (the subject one) in the current language
	 */
	public static String getBaseSecondSingularPersonalPronoun(Lexicon lexicon) {
		return getBaseSingularPersonalPronoun(lexicon, Person.SECOND);
	}

	/**
	 * Returns "I" in the current language
	 */
	public static String getBaseFirstSingularPersonalPronoun(Lexicon lexicon) {
		return getBaseSingularPersonalPronoun(lexicon, Person.FIRST);
	}


	public static String getBaseSingularPersonalPronoun(Lexicon lexicon, Person p) {
		Map<String, Object> features = new HashMap<String, Object>();
		features.put(Feature.NUMBER, NumberAgreement.SINGULAR);
		features.put(Feature.PERSON, p);

		WordElement pronoun = lexicon.getWord(LexicalCategory.PRONOUN, features);

		return pronoun.getBaseForm();
	}
	
	public static String getBasePluralPersonalPronoun(Lexicon lexicon, Person p) {
		Map<String, Object> features = new HashMap<String, Object>();
		features.put(Feature.NUMBER, NumberAgreement.PLURAL);
		features.put(Feature.PERSON, p);

		WordElement pronoun = lexicon.getWord(LexicalCategory.PRONOUN, features);

		return pronoun.getBaseForm();
	}
	
	/**
	 * In some languages, there is a more polite way to say "you" (the singular one).
	 * Override with a call to {@link Translator#getBaseSingularPersonalPronoun(Lexicon, Person)} or {@link Translator#getBasePluralPersonalPronoun(Lexicon, Person)} if needed
	 */
	protected String getPolitenessPersonalPronoun() {
		return getBaseSecondSingularPersonalPronoun(lexicon);
	}
	
	
	/**
	 * Returns a way to call the user at first, such as "sir", or even a name. May be added to the end of sentences
	 */
	public abstract String getDefaultUserApostrophe();
	
}
