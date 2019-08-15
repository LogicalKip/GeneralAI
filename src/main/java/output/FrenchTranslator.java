package output;

import grammar.*;
import grammar.determiner.DefiniteDeterminer;
import grammar.determiner.IndefiniteDeterminer;
import grammar.entity.EntityInterrogative;
import grammar.verb.*;
import simplenlg.features.Person;
import simplenlg.framework.LexicalCategory;
import simplenlg.lexicon.XMLLexicon;
import main.AI;
public class FrenchTranslator extends Translator {

	public FrenchTranslator(AI ai) {
		super(ai, "français");
	}

	@Override
	public void addBasicVocabulary() {
	    improveLexicon();

		this.vocabulary.add(new Designation("quoi", EntityInterrogative.getInstance()));
		this.vocabulary.add(new Designation("qui", EntityInterrogative.getInstance()));
		
		this.vocabulary.add(new Designation("signifier", HasSameMeaningAs.getInstance()));
		this.vocabulary.add(new Designation("lancer", StartSoftware.getInstance()));
		this.vocabulary.add(new Designation("démarrer", StartSoftware.getInstance()));

		this.vocabulary.add(new Designation("éteindre", Stop.getInstance()));
		this.vocabulary.add(new Designation("arrêter",  Stop.getInstance()));

		this.vocabulary.add(new Designation("être", Be.getInstance()));
		this.vocabulary.add(new Designation("savoir",  Knowing.getInstance()));
        this.vocabulary.add(new Designation("comprendre", Understand.getInstance()));
        this.vocabulary.add(new Designation("expliquer", Explain.getInstance()));
        this.vocabulary.add(new Designation("définir", Explain.getInstance()));


		this.vocabulary.add(new Designation("pas", Not.getInstance()));

		Gender feminine = new Gender();
		Gender masculine = new Gender();
		this.vocabulary.add(new Designation("le", new DefiniteDeterminer(masculine)));
		this.vocabulary.add(new Designation("la", new DefiniteDeterminer(feminine)));
		this.vocabulary.add(new Designation("un", new IndefiniteDeterminer(masculine)));
		this.vocabulary.add(new Designation("une", new IndefiniteDeterminer(feminine)));
	}

    private void improveLexicon() {
        this.getXMLLexicon().getWord("définir", LexicalCategory.VERB);
        this.getXMLLexicon().getWord("démarrer", LexicalCategory.VERB);
    }

    @Override
	public String getLanguageParameterForGetDefProgram() {
		return "fr";
	}

	@Override
	protected XMLLexicon loadXMLLexicon() {
		return new simplenlg.lexicon.french.XMLLexicon("res/default-french-lexicon.xml");
	}
	
	@Override
	public String getDefaultUserApostrophe() {
		return "monsieur";
	}

	@Override
	public String getStanfordParserModelFilename() {
		return "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz";
	}
	
	@Override
	protected String getPolitenessPersonalPronoun() {
		return getBasePluralPersonalPronoun(getXMLLexicon(), Person.SECOND);
	}
}
