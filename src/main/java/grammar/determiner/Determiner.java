package grammar.determiner;

import grammar.AbstractConcept;
import grammar.gender.Gender;

/**
 * Determiner of a noun, such as "the", "a"
 * Different genders mean different determiners
 */
public abstract class Determiner extends AbstractConcept {
    private Gender gender;

    Determiner(Gender g) {
        this.gender = g;
    }

    @Override
    public String toString() {
        return "DETERMINER-" + (gender == null ? "UnkownGender" : gender.getClass().getSimpleName());
    }

    public Gender getGender() {
        return gender;
    }
}
