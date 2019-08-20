package grammar.determiner;

import grammar.gender.Gender;

public class DefiniteDeterminer extends Determiner {
	public DefiniteDeterminer(Gender g) {
		super(g);
	}

	@Override
	public String toString() {
		return "DEFINITE-" + super.toString();
	}
}
