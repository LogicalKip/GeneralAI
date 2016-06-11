package main;

import java.util.LinkedList;
import java.util.List;

import grammar.AbstractConcept;
import grammar.AbstractEntityConcept;
import grammar.Adjective;
import grammar.DeclarativeSentence;
import grammar.DefiniteDeterminer;
import grammar.Designation;
import grammar.Determiner;
import grammar.Entity;
import grammar.Gender;
import grammar.IEntity;
import grammar.NounDesignation;
import simplenlg.features.Feature;
import simplenlg.features.Tense;
import simplenlg.framework.CoordinatedPhraseElement;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.XMLLexicon;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
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
		say(realiser.realiseSentence(parseSentence(sentence)));
	}

	public void say(List<DeclarativeSentence> sentences) {
		CoordinatedPhraseElement c = nlgFactory.createCoordinatedPhrase();
		for (DeclarativeSentence s : sentences) {
			c.addCoordinate(parseSentence(s));
		}
		say(realiser.realiseSentence(c));
	}
	
	/**
	 * Probably something like "edu/stanford/nlp/models/lexparser/SOME_MODEL.ser.gz"
	 */
	public abstract String getStanfordParserModelFilename();

	/**
	 * From a {@link DeclarativeSentence}, makes an object that can be computed by the simpleNLG library, and possibly adds some features to improve the result
	 */
	private SPhraseSpec parseSentence(DeclarativeSentence sentence) {
		IEntity s = sentence.getSubject();
		IEntity o = sentence.getObject();

		SPhraseSpec p = nlgFactory.createClause(
				computeEntityString(s),
				concatenateDesignations(sentence.getVerb()), 
				computeEntityString(o));

		p.setFeature(Feature.TENSE, Tense.CONDITIONAL);

		return p;
	}

	/**
	 * More or less toString() in the current language
	 * @return How the entity will be represented as a String after some SimpleNLG processing.
	 */
	private String computeEntityString(IEntity entityParam) {
		String res;

		if (entityParam instanceof Entity) {
			Entity entity = (Entity) entityParam;

			NPPhraseSpec element = nlgFactory.createNounPhrase(
					getDefiniteDeterminerFor(entity),
					concatenateDesignations(entity.getConcept()));
			for (Adjective qualifier : entity.getCharacteristics()) {
				element.addModifier(concatenateDesignations(qualifier));
			}

			res = realiser.realise(element).getRealisation();
		} else {
			res = concatenateDesignations((AbstractEntityConcept) entityParam);
		}
		return res;
	}

	/**
	 * Returns a definite determiner that fits the gender of one of the entity's designations
	 */
	private String getDefiniteDeterminerFor(Entity entity) {
		String determiner = "";
		for (Designation currDeterminer : this.vocabulary) {
			if (currDeterminer.getDesignatedConcept() instanceof DefiniteDeterminer) {
				Gender determinerGender = ((Determiner) currDeterminer.getDesignatedConcept()).getGender();
				for (Designation currEntityDesignation : getDesignations(entity.getConcept())) {
					NounDesignation currNounDesignation = (NounDesignation) currEntityDesignation;
					if (currNounDesignation.getGender().equals(determinerGender)) {
						return currDeterminer.getValue();
					}
				}
			}
		}
		return determiner;
	}

	public abstract XMLLexicon getXMLLexicon();

	private static String concatenateDesignations(List<Designation> designations) {
		String res;

		if (designations.isEmpty()) {
			res = "";
		} else {
			res = designations.get(0).getValue();
			for (Designation d : designations.subList(1, designations.size())) {
				res += "/" + d.getValue();
			}
		}

		return res;
	}

	public String concatenateDesignations(AbstractConcept concept) {
		return concatenateDesignations(getDesignations(concept));
	}

	private List<Designation> getDesignations(AbstractConcept concept) {
		List<Designation> res = new LinkedList<Designation>();
		for (Designation designation : this.vocabulary) {
			if (designation.getDesignatedConcept().equals(concept)) {
				res.add(designation);
			}
		}
		return res;
	}
}
