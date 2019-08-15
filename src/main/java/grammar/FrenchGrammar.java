package grammar;

import exceptions.CantFindSuchAnEntityException;
import exceptions.WrongGrammarRuleException;
import grammar.determiner.DefiniteDeterminer;
import grammar.determiner.Determiner;
import grammar.determiner.IndefiniteDeterminer;
import grammar.entity.*;
import grammar.sentence.DeclarativeSentence;
import grammar.sentence.Sentence;
import grammar.sentence.SimpleSentence;
import grammar.sentence.StativeSentence;
import grammar.token.*;
import grammar.verb.Verb;
import main.AI;
import output.Translator;
import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.NumberAgreement;
import simplenlg.features.Person;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.NLGFactory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.VPPhraseSpec;
import simplenlg.realiser.Realiser;

import java.util.LinkedList;
import java.util.List;

//FIXME potential future problem, even if grammar rules as they are now can't make it happen : get/create an entity, add it to newEntities, then rollback because the rule was the wrong one, but the entity is still here. If the sentence ends up correct, there will be a wrong entity added/duplicated. Could be the same with vocab/other things

/**
 * Parses french Strings into Java classes and concepts used by the main.AI, using a tree made of custom grammar rules.
 */
public class FrenchGrammar {

    private Lexicon lexicon;

    private List<Designation> newVocabulary;
    private List<Entity> newEntities;
    private List<Designation> actualAIVocabulary;
    private List<Entity> actualAIKnownEntities;

    /**
     * The index of the token being currently read
     */
    private int index;

    public FrenchGrammar(Lexicon lexicon, List<Designation> AIvocabulary, List<Entity> AIEntities) {
        this.lexicon = lexicon;

        this.actualAIVocabulary = AIvocabulary;
        this.actualAIKnownEntities = AIEntities;
        this.newVocabulary = new LinkedList<>();
        this.newEntities = new LinkedList<>();
    }

    public Sentence parse(String input) throws CantFindSuchAnEntityException, WrongGrammarRuleException {
        this.newVocabulary = new LinkedList<>();
        this.newEntities = new LinkedList<>();

        List<Token> tokens = new Tokenizer(lexicon).tokenize(input);

        index = 0;
        Sentence parsedSentence = start(tokens);

        if (this.index != tokens.size() - 1) { // some tokens are remaining
            throw new WrongGrammarRuleException();
        }

        return parsedSentence;

    }

    /**
     * First rule of the grammar tree
     */
    private Sentence start(List<Token> tokens) throws WrongGrammarRuleException, CantFindSuchAnEntityException {
        int startingIndex = index;

        Sentence res;

        try {
            res = order(tokens);
        } catch (WrongGrammarRuleException e) {
            this.index = startingIndex;
            res = simpleSentence(tokens);
        }


        optionalEndingPunctuations(tokens);

        return res;
    }

    private Order order(List<Token> tokens) throws WrongGrammarRuleException {
        Verb verb = imperative(tokens);

        String object = null;

        if (this.index < tokens.size() - 1) { // FIXME condition may be a problem. What if multiple sentences and what follows is a dot, for example ? parsing will carry on and object will be a dot
            this.index++;
            object = tokens.get(this.index).getOriginalString();
        }

        return new Order(verb, object);
    }

    /**
     * A simple sentence with a subject, verb and object, such as "a cat eats a mouse", or "I see what ?"
     */
    private SimpleSentence simpleSentence(List<Token> tokens) throws WrongGrammarRuleException, CantFindSuchAnEntityException {
        boolean sentenceIsNegated = false;
        boolean sentenceIsInterrogative = false;

        IEntity subject = entity(tokens);
        this.index++;


        if (subject instanceof EntityInterrogative) {
            sentenceIsInterrogative = true;
        }

        if (tokens.get(this.index) instanceof NeToken) {
            sentenceIsNegated = true;
            this.index++;
        }

        if (canBeVerb(tokens.get(this.index))) {
            String verbString = tokens.get(this.index).getOriginalString();
            this.index++;

            if (canBePas(tokens.get(this.index))) {
                sentenceIsNegated = true;
                this.index++;
            }

            SimpleSentence sentence;
            Verb verb = processCorrespondingVerb(verbString);

            if (verb.IS_STATIVE_VERB) {
                Token nextToken = tokens.get(this.index);
                if (canBeAdjective(nextToken)) {
                    sentence = new StativeSentence(subject, verb, processCorrespondingAdjective(nextToken.getOriginalString()));
                } else {
                    throw new WrongGrammarRuleException();
                }
            } else {
                IEntity object = entity(tokens);
                sentence = new DeclarativeSentence(subject, verb, object);
                if (object instanceof EntityInterrogative) {
                    sentenceIsInterrogative = true;
                }
            }

            sentence.setNegative(sentenceIsNegated);

            if (nextTokenIsQuestionMark(tokens)) {
                this.index++;
                sentenceIsInterrogative = true;
            }
            sentence.setInterrogative(sentenceIsInterrogative);

            return sentence;
        }

        throw new WrongGrammarRuleException();
    }

    private boolean nextTokenIsQuestionMark(List<Token> tokens) {
        return this.index + 1 < tokens.size() && tokens.get(this.index + 1) instanceof QuestionMarkToken;
    }

    /**
     * nominal group = determiner (adjective)* noun (adjective)*
     */
    private IEntity nominalGroup(List<Token> tokens) throws WrongGrammarRuleException, CantFindSuchAnEntityException {
        if (canBeDeterminer(tokens.get(this.index))) {
            String determinerString = tokens.get(this.index).getOriginalString();
            this.index++;

            List<String> adjectivesStrings = new LinkedList<>(goThroughContiguousAdjectives(tokens));

            if (canBeNoun(tokens.get(this.index))) {
                String noun = tokens.get(this.index).getOriginalString();

                if (adjectiveDetectedAfterThis(tokens)) {
                    this.index++;
                    adjectivesStrings.addAll(goThroughContiguousAdjectives(tokens));
                    this.index--; // finish on the last adjective
                }


                System.out.print("Detected nominal group " + determinerString + " " + noun + ".");
                if (adjectivesStrings.size() > 0) {
                    System.out.print(" All " + adjectivesStrings.size() + " adjectives : ");
                }
                for (String adjective : adjectivesStrings) {
                    System.out.print(adjective + ";");
                }
                System.out.println();

                Determiner determiner = (Determiner) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), getBase(determinerString, LexicalCategory.DETERMINER), Determiner.class);


                List<Adjective> adjectives = new LinkedList<>();
                for (String s : adjectivesStrings) {
                    String adjectiveBase = getBase(s, LexicalCategory.ADJECTIVE);
                    Adjective adjectiveConcept = (Adjective) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), adjectiveBase, Adjective.class);
                    if (adjectiveConcept == null) {
                        adjectiveConcept = new Adjective();
                        this.newVocabulary.add(new Designation(adjectiveBase, adjectiveConcept));
                    }
                    adjectives.add(adjectiveConcept);
                }


                return processCorrespondingEntity(determiner, adjectives, noun);

            }
        }


        throw new WrongGrammarRuleException();
    }

    /**
     * Something where an entity is expected, i.e who/what, or a nominal group
     */
    private IEntity entity(List<Token> tokens) throws CantFindSuchAnEntityException, WrongGrammarRuleException {
        int startingIndex = index;

        IEntity res;
        try {
            res = nominalGroup(tokens);
        } catch (WrongGrammarRuleException e) {
            this.index = startingIndex;

            try {
                if (canBePronoun(tokens.get(this.index))) {
                    res = processCorrespondingEntityInterrogative(tokens.get(this.index).getOriginalString());
                } else {
                    throw new WrongGrammarRuleException();
                }
            } catch (WrongGrammarRuleException e1) {
                this.index = startingIndex;
                res = personalPronoun(tokens);
            }
        }
        return res;
    }

    private Verb imperative(List<Token> tokens) throws WrongGrammarRuleException {
        Token currentToken = tokens.get(this.index);
        if (currentToken instanceof VerbToken) {
            String originalString = currentToken.getOriginalString();
            WordElement baseWord = lexicon.getWordFromVariant(originalString, LexicalCategory.VERB);

            String singularImperative = imperativeOf(baseWord, NumberAgreement.SINGULAR);
            String pluralImperative = imperativeOf(baseWord, NumberAgreement.PLURAL);

            if (originalString.equals(singularImperative) || originalString.equals(pluralImperative)) {
                Designation res = AI.getFirstDesignationFrom(getUpdatedVocabulary(), baseWord.getBaseForm(), Verb.class);
                if (res == null) {
                    throw new WrongGrammarRuleException();
                }

                res.incrementTimesUserUsedIt();
                return ((Verb) res.getDesignatedConcept());
            } else {
                throw new WrongGrammarRuleException();
            }
        } else {
            throw new WrongGrammarRuleException();
        }
    }

    /**
     * Subfunction used to conjugate a verb into its imperative form
     *
     * @param baseWord        the verb
     * @param numberAgreement The person is implied to be the second (you). This indicates the number (plural or singular)
     * @return The imperative without caps or dots.
     */
    private String imperativeOf(WordElement baseWord, NumberAgreement numberAgreement) {
        VPPhraseSpec verbPhrase = new NLGFactory(lexicon).createVerbPhrase(baseWord.getBaseForm());
        verbPhrase.setFeature(Feature.FORM, Form.IMPERATIVE);
        verbPhrase.setFeature(Feature.PERSON, Person.SECOND);
        verbPhrase.setFeature(Feature.NUMBER, numberAgreement);

        return new Realiser().realise(verbPhrase).getRealisation();
    }

    private IEntity personalPronoun(List<Token> tokens) throws WrongGrammarRuleException {
        IEntity res = null;
        Token tokenTested = tokens.get(this.index);

        if (canBePronoun(tokenTested)) {
            res = getEntityFromPronoun(tokenTested.getOriginalString());
        }

        if (res == null) {
            throw new WrongGrammarRuleException();
        }

        return res;
    }

    private Entity getEntityFromPronoun(String pronoun) {
        if (isSecondSingularPersonalPronoun(pronoun)) {
            return Myself.getInstance();
        } else if (isFirstSingularPersonalPronoun(pronoun)) {
            return User.getInstance();
        }
        return null;
    }


    private EntityInterrogative processCorrespondingEntityInterrogative(String designation) throws WrongGrammarRuleException {
        EntityInterrogative res;
        AbstractEntityConcept designatedConcept = (AbstractEntityConcept) AI.getFirstConceptDesignatedBy(getUpdatedVocabulary(), getBase(designation, LexicalCategory.PRONOUN), AbstractEntityConcept.class);

        if (designatedConcept instanceof EntityInterrogative) {
            res = (EntityInterrogative) designatedConcept;
        } else {
            throw new WrongGrammarRuleException();
        }
        return res;
    }

    private boolean adjectiveDetectedAfterThis(List<Token> tokens) {
        return this.index + 1 < tokens.size() && canBeAdjective(tokens.get(this.index + 1));
    }

    private List<String> goThroughContiguousAdjectives(List<Token> tokens) {
        List<String> res = new LinkedList<>();

        while (this.index < tokens.size() && canBeAdjective(tokens.get(this.index))) {
            res.add(tokens.get(this.index).getOriginalString());
            this.index++;
        }

        return res;
    }


    /**
     * Returns the entity corresponding to the given parameters if the main.AI knows about it, or adds an entry with a new entity and/or concept in the new vocabulary/entities otherwise.
     */
    private IEntity processCorrespondingEntity(Determiner determiner, List<Adjective> qualifiers, String nounDesignation) throws CantFindSuchAnEntityException {
        IEntity res = null;
        if (isKnownNoun(nounDesignation)) {
            nounDesignation = getBase(nounDesignation, LexicalCategory.NOUN);
        }

        Designation designation = AI.getFirstDesignationFrom(getUpdatedVocabulary(), nounDesignation, AbstractEntityConcept.class);

        if (designation == null) { // Unknown word
            EntityConcept newConcept = new EntityConcept();

            Entity newEntity = new Entity(newConcept);
            newEntity.getCharacteristics().addAll(qualifiers);
            res = newEntity;
            this.newEntities.add(newEntity);

            NounDesignation newDesignation = new NounDesignation(nounDesignation, newConcept);
            newDesignation.incrementTimesUserUsedIt();
            newDesignation.setGender(determiner.getGender());
            newVocabulary.add(newDesignation);
        } else {
            AbstractEntityConcept designatedConcept = (AbstractEntityConcept) designation.getDesignatedConcept();
            designation.incrementTimesUserUsedIt();

            if (designatedConcept instanceof EntityInterrogative) {
                res = (EntityInterrogative) designatedConcept;
            } else if (designatedConcept instanceof EntityConcept) {
                if (determiner instanceof IndefiniteDeterminer) {
                    Entity newEntity = new Entity((EntityConcept) designatedConcept);
                    newEntity.getCharacteristics().addAll(qualifiers);
                    res = newEntity;
                    this.newEntities.add(newEntity);
                } else if (determiner instanceof DefiniteDeterminer) {
                    res = getLastMentionOfA((EntityConcept) designatedConcept, qualifiers);
                    if (res == null) {
                        throw new CantFindSuchAnEntityException((EntityConcept) designatedConcept, getDesignationsOf(qualifiers, getUpdatedVocabulary()));
                    }
                } else {
                    System.err.println("There is a 3rd determiner class ? Not expected !");
                }
            } else {
                System.err.println("A concept during the parsing is of neither expected classes. Some code needs an update");
            }
        }

        return res;
    }

    /**
     * Returns the concept corresponding to the parameter (it is implied that it represents a verb) if it's in the main.AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
     */
    private Verb processCorrespondingVerb(String verb) {
        String designation = isKnownVerb(verb) ? getBase(verb, LexicalCategory.VERB) : verb;

        Verb res;
        Designation d = AI.getFirstDesignationFrom(getUpdatedVocabulary(), designation, Verb.class);

        if (d == null) { // Unknown word
            Verb newConcept = new Verb();
            res = newConcept;
            Designation newDesignation = new Designation(designation, newConcept);
            newDesignation.incrementTimesUserUsedIt();
            newVocabulary.add(newDesignation);
        } else {
            d.incrementTimesUserUsedIt();
            res = (Verb) d.getDesignatedConcept();
        }

        return res;
    }


    /**
     * Returns the concept corresponding to the parameter (it is implied that it represents an adjective) if it's in the main.AI vocabulary, or adds an entry with a new concept in the new vocabulary otherwise.
     */
    private Adjective processCorrespondingAdjective(String adjective) {
        String designation = isKnownAdjective(adjective) ? getBase(adjective, LexicalCategory.ADJECTIVE) : adjective;

        Adjective res;
        Designation d = AI.getFirstDesignationFrom(getUpdatedVocabulary(), designation, Adjective.class);

        if (d == null) { // Unknown word
            Adjective newConcept = new Adjective();
            res = newConcept;
            newVocabulary.add(new Designation(designation, newConcept));
        } else {
            res = (Adjective) d.getDesignatedConcept();
        }

        return res;
    }

    /**
     * Returns the last created entity such that its concept equals the parameter and its adjectives match the given list
     */
    private Entity getLastMentionOfA(EntityConcept concept, List<Adjective> qualifiers) {
        Entity lastOccurrence = null;

        for (Entity currEntity : this.actualAIKnownEntities) {
            if (currEntity.getConcept().equals(concept) &&
                    currEntity.getCharacteristics().containsAll(qualifiers)) {
                lastOccurrence = currEntity;
            }
        }

        return lastOccurrence;
    }


    private static List<String> getDesignationsOf(List<Adjective> adjectives, List<Designation> vocab) {
        List<String> res = new LinkedList<String>();

        for (Designation d : vocab) {
            if (adjectives.contains(d.getDesignatedConcept())) {
                res.add(d.getValue());
            }
        }
        return res;
    }


    //FIXME for all "canBe" : the fact the word is not in the lexicon doesn't mean it's not a noun/verb/etc. Only if it's IN the lexicon and NOT a noun/etc means it's NOT a noun/etc
    private boolean canBeDeterminer(Token token) {
        return token instanceof DeterminerToken || canBeA(DeterminerToken.CORRESPONDING_NLG_CATEGORY, token);
    }

    private boolean canBeVerb(Token token) {
        return token instanceof VerbToken || canBeA(VerbToken.CORRESPONDING_NLG_CATEGORY, token);
    }

    private boolean canBeNoun(Token token) {
        return token instanceof NounToken || canBeA(NounToken.CORRESPONDING_NLG_CATEGORY, token);
    }

    private boolean canBeAdjective(Token token) {
        return token instanceof AdjectiveToken || canBeA(AdjectiveToken.CORRESPONDING_NLG_CATEGORY, token);
    }

    private boolean canBePronoun(Token token) {
        return token instanceof PronounToken || canBeA(PronounToken.CORRESPONDING_NLG_CATEGORY, token);
    }

    /**
     * Checks if token can be the french adverb "pas", which means "not". Be aware that in french, "pas" is also a noun with a completely different meaning ("step")
     */
    private boolean canBePas(Token token) {
        return token instanceof MiscToken && token.getOriginalString().equalsIgnoreCase("pas");
    }

    /**
     * Returns true if at least one of the possible words corresponding to the token's original string is a 'category'
     * Ex : if that specific spelling ("souris") is sometimes that of an noun, sometimes of a verb, then canBeA(noun) and also canBeA(verb)
     *
     * @param category using the string NLG uses for its category
     */
    private boolean canBeA(String category, Token token) {
        for (WordElement word : lexicon.getWordsFromVariant(token.getOriginalString())) {
            if (word.getCategory().toString().equals(category)) {
                return true;
            }
        }
        return false;
    }

    private List<Designation> getUpdatedVocabulary() {
        List<Designation> res = new LinkedList<Designation>();
        res.addAll(actualAIVocabulary);
        res.addAll(newVocabulary);
        return res;
    }

    private boolean isKnownNoun(String word) {
        return lexicon.hasWordFromVariant(word, LexicalCategory.NOUN);
    }

    private boolean isKnownVerb(String word) {
        return lexicon.hasWordFromVariant(word, LexicalCategory.VERB);
    }

    private boolean isKnownAdjective(String word) {
        return lexicon.hasWordFromVariant(word, LexicalCategory.ADJECTIVE);
    }

    /**
     * More or less returns the stem of the given word
     */
    private String getBase(String word, LexicalCategory category) {
        return lexicon.getWordFromVariant(word, category).getBaseForm();
    }

    public List<Designation> getNewVocabulary() {
        return this.newVocabulary;
    }

    public List<Entity> getNewEntities() {
        return this.newEntities;
    }


    /**
     * @return True if the given word is the equivalent of "I" in French
     */
    public boolean isFirstSingularPersonalPronoun(String word) {
        return Translator.getBaseFirstSingularPersonalPronoun(lexicon).equals(word);
    }

    /**
     * @return True if the given word is the equivalent of "You" (as a subject) in French
     */
    public boolean isSecondSingularPersonalPronoun(String word) {
        return Translator.getBaseSecondSingularPersonalPronoun(lexicon).equals(word);
    }


    private void optionalEndingPunctuations(List<Token> tokens) {
        while (this.index < tokens.size() - 1 && tokens.get(this.index + 1) instanceof MiscFinalPunctuationToken) {
            this.index++;
        }
    }
}
