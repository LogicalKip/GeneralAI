package grammar;

import exceptions.CantFindSuchAnEntityException;
import grammar.determiner.DefiniteDeterminer;
import grammar.determiner.Determiner;
import grammar.determiner.IndefiniteDeterminer;
import grammar.entity.AbstractEntityConcept;
import grammar.entity.Entity;
import grammar.entity.EntityConcept;
import grammar.entity.EntityInterrogative;
import grammar.entity.IEntity;
import grammar.gender.FeminineGender;
import grammar.gender.Gender;
import grammar.gender.MasculineGender;
import simplenlg.framework.LexicalCategory;
import simplenlg.lexicon.Lexicon;
import util.VocabRetriever;

import java.rmi.UnexpectedException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class EntityHandler {

    private Lexicon lexicon = FrenchGrammar.getLexicon();

    public static List<String> getDesignationsFor(Set<Adjective> adjectives, List<Designation> vocab) {
        return vocab
                .stream()
                .filter(d -> d.getDesignatedConcept() instanceof Adjective)
                .filter(d -> adjectives.contains(d.getDesignatedConcept()))
                .map(Designation::getValue)
                .collect(toList());
    }

    private boolean isKnownNoun(String word) {
        return lexicon.hasWordFromVariant(word, LexicalCategory.NOUN);
    }

    /**
     * Returns the entity corresponding to the given parameters if the AI knows about it, or adds an entry with a new entity and/or concept in the new vocabulary/entities otherwise.
     */
    public IEntity processCorrespondingEntity(Determiner determiner, Set<Adjective> qualifiers, String nounDesignation) throws CantFindSuchAnEntityException, UnexpectedException {
        IEntity res;
        if (isKnownNoun(nounDesignation)) {
            nounDesignation = FrenchGrammar.getBase(nounDesignation, LexicalCategory.NOUN);
        }

        Optional<Designation> designation = VocabRetriever.getFirstDesignationFrom(FrenchGrammar.getActualAIVocabulary(), nounDesignation, AbstractEntityConcept.class);

        if (designation.isEmpty()) { // Unknown word TODO use map() and orElse() and extract methoss
            EntityConcept correspondingConcept;

            String finalNounDesignation = nounDesignation;
            Optional<Designation> designationFromBeforeInSentence = FrenchGrammar.getNewVocabulary().stream() //FIXME weird logic ?
                    .filter(d -> d instanceof NounDesignation)
                    .filter(d -> d.getValue().equals(finalNounDesignation))
                    .findFirst();
            if (designationFromBeforeInSentence.isPresent()) {
                correspondingConcept = (EntityConcept) designationFromBeforeInSentence.get().getDesignatedConcept();
            } else {
                correspondingConcept = new EntityConcept();

                NounDesignation newDesignation = new NounDesignation(nounDesignation, correspondingConcept);
                newDesignation.incrementTimesUserUsedIt();
                newDesignation.setGender(deduceGender(determiner, nounDesignation));
                FrenchGrammar.getNewVocabulary().add(newDesignation);
            }

            Entity newEntity = new Entity(correspondingConcept);
            newEntity.getCharacteristics().addAll(qualifiers);
            res = newEntity;
            FrenchGrammar.getNewEntities().add(newEntity);
        } else {
            AbstractEntityConcept designatedConcept = (AbstractEntityConcept) designation.get().getDesignatedConcept();
            designation.get().incrementTimesUserUsedIt();

            if (designatedConcept instanceof EntityInterrogative) {
                res = (EntityInterrogative) designatedConcept;
            } else if (designatedConcept instanceof EntityConcept) {
                if (determiner instanceof IndefiniteDeterminer) {
                    Entity newEntity = new Entity((EntityConcept) designatedConcept);
                    newEntity.getCharacteristics().addAll(qualifiers);
                    res = newEntity;
                    FrenchGrammar.getNewEntities().add(newEntity);
                } else if (determiner instanceof DefiniteDeterminer) {
                    res = getLastMentionOfA((EntityConcept) designatedConcept, qualifiers);
                    if (res == null) {
                        throw new CantFindSuchAnEntityException((EntityConcept) designatedConcept, getDesignationsFor(qualifiers, FrenchGrammar.getUpdatedVocabulary()));
                    }
                } else {
                    throw new UnexpectedException("There is a 3rd determiner class ? Not expected ! " + determiner);
                }
            } else {
                throw new UnexpectedException("A concept during the parsing is of neither expected classes. Some code needs an update. " + designatedConcept);
            }
        }

        return res;
    }

    /**
     * Returns the last created entity such that its concept equals the parameter and its adjectives match the given list
     */
    private Entity getLastMentionOfA(EntityConcept concept, Set<Adjective> qualifiers) {
        Entity lastOccurrence = null;

        for (Entity currEntity : FrenchGrammar.getActualAIKnownEntities()) {
            if (currEntity.getConcept().equals(concept) &&
                    currEntity.getCharacteristics().containsAll(qualifiers)) {
                lastOccurrence = currEntity;
            }
        }

        return lastOccurrence;
    }


    /**
     * Returns the gender of the determiner, or looks up the lexicon if it was unknown (ex : l' can be masculine or feminine)
     */
    private Gender deduceGender(Determiner determiner, String nounDesignation) {
        if (determiner.getGender() == null) {
            String base = FrenchGrammar.getBase(nounDesignation, LexicalCategory.NOUN);
            if (this.lexicon.hasWord(base, LexicalCategory.NOUN)) {
                String genderAccordingToLexicon = this.lexicon.getWord(base, LexicalCategory.NOUN).getFeatureAsString("gender");
                if (genderAccordingToLexicon.equalsIgnoreCase("FEMININE")) {
                    return FeminineGender.getInstance();
                }
            }
            return MasculineGender.getInstance();
        } else {
            return determiner.getGender();
        }
    }
}
