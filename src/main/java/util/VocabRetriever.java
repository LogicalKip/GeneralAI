package util;

import grammar.Designation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class VocabRetriever {
    /**
     * Searches the given vocabulary for a concept with given name and given class. If several exist somehow, return the first.
     *
     * @return Returns empty if none are found
     * FIXME we would always want vocabulry to be updateVocab(), find a way without having to pass it (and possibly send something wrwong) ?
     */
    public static <classLookedFor> Optional<classLookedFor> getFirstConceptDesignatedBy(final List<Designation> vocabulary, final String designation, final Class<classLookedFor> clazz) {
        return getFirstDesignationFrom(vocabulary, designation, clazz)
                .map(Designation::getDesignatedConcept)
                .map(c -> ((classLookedFor) c));
    }

    public static Optional<Designation> getFirstDesignationFrom(final List<Designation> vocabulary, final String designation, final Class<?> classLookedFor) {
        return getAllDesignationFrom(vocabulary, designation, classLookedFor)
                .findFirst();
    }

    private static Stream<Designation> getAllDesignationFrom(final List<Designation> vocabulary, final String designation, final Class<?> classLookedFor) {
        return vocabulary.stream()
                .filter(d -> d.getValue().equals(designation) && classLookedFor.isInstance(d.getDesignatedConcept()));
    }
}
