package main;

import grammar.DefiniteDeterminer;
import grammar.Designation;
import grammar.EntityInterrogative;
import grammar.Explain;
import grammar.Gender;
import grammar.HasSameMeaningAs;
import grammar.IndefiniteDeterminer;
import grammar.Knowing;
import grammar.Not;
import grammar.StartSoftware;
import grammar.Stop;
import grammar.Verb;
import grammar.VerbInterrogative;
import simplenlg.features.Tense;
import simplenlg.lexicon.XMLLexicon;

public class FrenchTranslator extends Translator {

	public FrenchTranslator(AI ai) {
		super(ai, "français");
	}

	@Override
	public void addBasicVocabulary() {
		this.vocabulary.add(new Designation("quoi", EntityInterrogative.getInstance()));
		this.vocabulary.add(new Designation("qui", EntityInterrogative.getInstance()));
		this.vocabulary.add(new Designation("quoi", VerbInterrogative.getInstance()));
		
		this.vocabulary.add(new Designation("signifier", new Verb(Tense.PRESENT, HasSameMeaningAs.getInstance())));
		this.vocabulary.add(new Designation("démarrer", new Verb(Tense.PRESENT, StartSoftware.getInstance())));

		this.vocabulary.add(new Designation("éteindre", new Verb(Tense.PRESENT, Stop.getInstance())));
		this.vocabulary.add(new Designation("arrêter",  new Verb(Tense.PRESENT, Stop.getInstance())));
		
		this.vocabulary.add(new Designation("savoir",  new Verb(Tense.PRESENT, Knowing.getInstance())));
		this.vocabulary.add(new Designation("définir", new Verb(Tense.PRESENT, Explain.getInstance())));
		

		this.vocabulary.add(new Designation("pas", Not.getInstance()));

		Gender feminine = new Gender();
		Gender masculine = new Gender();
		this.vocabulary.add(new Designation("le", new DefiniteDeterminer(masculine)));
		this.vocabulary.add(new Designation("la", new DefiniteDeterminer(feminine)));
		this.vocabulary.add(new Designation("un", new IndefiniteDeterminer(masculine)));
		this.vocabulary.add(new Designation("une", new IndefiniteDeterminer(feminine)));
	}
	
	@Override
	public String getLanguageParameterForGetDefProgram() {
		return "fr";
	}

	@Override
	public XMLLexicon getXMLLexicon() {
		return new simplenlg.lexicon.french.XMLLexicon("res/default-french-lexicon.xml");
	}

	@Override
	public String getStanfordParserModelFilename() {
		return "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz";
	}
}
