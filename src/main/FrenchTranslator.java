package main;

import java.util.LinkedList;

import grammar.Designation;
import grammar.Determiner;
import grammar.EntityInterrogative;
import grammar.Explain;
import grammar.Gender;
import grammar.HasSameMeaningAs;
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
		this.vocabulary = new LinkedList<Designation>();
		this.vocabulary.add(new Designation("quoi", EntityInterrogative.getInstance()));
		this.vocabulary.add(new Designation("qui", EntityInterrogative.getInstance()));;
		this.vocabulary.add(new Designation("quoi", VerbInterrogative.getInstance()));
		this.vocabulary.add(new Designation("signifie", new Verb(Tense.PRESENT, HasSameMeaningAs.getInstance())));
		this.vocabulary.add(new Designation("lance", new Verb(Tense.PRESENT, StartSoftware.getInstance())));
		this.vocabulary.add(new Designation("stop", new Verb(Tense.PRESENT, Stop.getInstance())));
		this.vocabulary.add(new Designation("définis", new Verb(Tense.PRESENT, Explain.getInstance())));

		Gender feminine = new Gender();
		Gender masculine = new Gender();
		Determiner determinerF = new Determiner(feminine);
		Determiner determinerM = new Determiner(masculine);
		this.vocabulary.add(new Designation("le", determinerM));
		this.vocabulary.add(new Designation("la", determinerF));
		this.vocabulary.add(new Designation("un", determinerM));
		this.vocabulary.add(new Designation("une", determinerF));
	}
	
	@Override
	public String getLanguageParameterForGetDefProgram() {
		return "fr";
	}

	@Override
	protected XMLLexicon getXMLLexicon() {
		return new simplenlg.lexicon.french.XMLLexicon("res/default-french-lexicon.xml");
	}

}
