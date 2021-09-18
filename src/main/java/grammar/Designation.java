package grammar;


import lombok.Getter;
import lombok.Setter;

/**
 * A name that refer to a concept
 * Ex: "kitty" to refer to the concept of cat. There wouldn't be a "kitties" one though, only with the base form, to make it simpler.
 */
public class Designation {
    @Getter
    private final String value;

    @Getter
    private int timesUserUsedIt;

    @Getter
    @Setter
    private AbstractConcept designatedConcept;

    public Designation(String value, AbstractConcept designatedConcept) {
        super();
        this.timesUserUsedIt = 0;
        this.value = value;
        this.designatedConcept = designatedConcept;
    }

    @Override
    public String toString() {
        return value + " -> " + designatedConcept.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Designation) {
            Designation designation = (Designation) o;
            return designation.getValue().equals(value) && designation.getDesignatedConcept().equals(designatedConcept);
        }
        return false;
    }

    public void incrementTimesUserUsedIt() {
        this.timesUserUsedIt++;
    }
}
