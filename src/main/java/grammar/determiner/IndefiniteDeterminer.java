package grammar.determiner;

import grammar.gender.Gender;

public class IndefiniteDeterminer extends Determiner {
	public IndefiniteDeterminer(Gender g) {
		super(g);
	}

	@Override
	public String toString() {
		return "INDEFINITE-" + super.toString();
	}
}
