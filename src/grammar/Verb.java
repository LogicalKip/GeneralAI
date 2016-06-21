package grammar;

import simplenlg.features.Tense;

/**
 * Refers to the concept of a verb. Note that [eating now] and [eating in the future] are two different {@link Verb}
 * @author charles
 *
 */
public class Verb extends AbstractVerb {
	private Tense tense;

	private VerbMeaning meaning;

	public Verb(Tense tense, VerbMeaning meaning) {
		super();
		this.tense = tense;
		this.meaning = meaning;
	}
	
	public Tense getTense() {
		return tense;
	}

	public VerbMeaning getMeaning() {
		return meaning;
	}
	@Override
	public String toString() {
		return "[" + meaning + " (" + tense + ")]";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Verb) {
			return ((Verb) o).getMeaning().equals(getMeaning()) && ((Verb) o).getTense().equals(getTense());
		} else {
			return super.equals(o);
		}
	}

}
