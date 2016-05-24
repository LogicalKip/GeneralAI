package main;

import java.util.LinkedList;
import java.util.List;

import grammar.AbstractConcept;
import grammar.DeclarativeSentence;
import grammar.Designation;
import simplenlg.features.Feature;
import simplenlg.features.Tense;
import simplenlg.framework.CoordinatedPhraseElement;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.XMLLexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.Realiser;

/**
 * Turns the internal, abstract concepts of the AI in something readable to the user.
 * Linked to a human language, it's the one that knows that language's vocabulary and special rules
 * @author charles
 *
 */
public abstract class Translator {
	private final String languageName;
	protected AI ai;
	protected List<Designation> vocabulary;
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
	 * From a {@link DeclarativeSentence}, makes an object that can be computed by the simpleNLG library, and possibly adds some features to improve the result
	 */
	private SPhraseSpec parseSentence(DeclarativeSentence sentence) {
		SPhraseSpec p = nlgFactory.createClause(
				concatenateDesignations(getDesignations(sentence.getSubject())),
				concatenateDesignations(getDesignations(sentence.getVerb())), 
				concatenateDesignations(getDesignations(sentence.getObject())));

		p.setFeature(Feature.TENSE, Tense.CONDITIONAL);

		return p;
	}

	protected abstract XMLLexicon getXMLLexicon();

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
