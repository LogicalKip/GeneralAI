package grammar.determiner;

import grammar.AbstractConcept;
import grammar.gender.Gender;
import lombok.Getter;

/**
 * Determiner of a noun, such as "the", "a"
 * Different genders mean different determiners
 */
public abstract class Determiner extends AbstractConcept {
    @Getter
    private Gender gender;

    Determiner(Gender g) {
        this.gender = g;
    }

    @Override
    public String toString() {
        return "DETERMINER-" + (gender == null ? "UnkownGender" : gender.getClass().getSimpleName());
    }
}
