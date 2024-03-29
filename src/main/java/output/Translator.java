package output;

import exceptions.NotEnoughKnowledgeException;
import grammar.AbstractConcept;
import grammar.Adjective;
import grammar.Designation;
import grammar.determiner.DefiniteDeterminer;
import grammar.determiner.IndefiniteDeterminer;
import grammar.entity.AbstractEntityConcept;
import grammar.entity.Entity;
import grammar.entity.EntityConcept;
import grammar.entity.EntityInterrogative;
import grammar.entity.IEntity;
import grammar.entity.Myself;
import grammar.entity.User;
import grammar.sentence.DeclarativeSentence;
import grammar.sentence.SimpleSentence;
import grammar.sentence.StativeSentence;
import grammar.verb.StartSoftware;
import grammar.verb.Stop;
import grammar.verb.Understand;
import lombok.Getter;
import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.InterrogativeType;
import simplenlg.features.NumberAgreement;
import simplenlg.features.Person;
import simplenlg.features.Tense;
import simplenlg.framework.CoordinatedPhraseElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.NLGElement;
import simplenlg.framework.NLGFactory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.phrasespec.VPPhraseSpec;
import simplenlg.realiser.Realiser;
import util.VocabRetriever;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Turns the internal, abstract concepts of the AI in something readable to the user.
 * Linked to a human language, it's the one that knows that language's vocabulary and special rules
 */
public abstract class Translator {
    @Getter
    private final String languageName;
    private final NLGFactory nlgFactory;
    private final Realiser realiser;
    @Getter
    protected List<Designation> vocabulary;
    private Lexicon lexicon;


    /**
     * @param language In its own language : "français", "english", "deutsch", etc
     */
    Translator(String language) {
        super();
        this.languageName = language;

        this.vocabulary = new LinkedList<>();
        this.addBasicVocabulary();
        this.nlgFactory = new NLGFactory(lexicon);
        this.realiser = new Realiser();
    }

    /**
     * Returns "you" (the subject one) in the current language
     */
    public static String getBaseSecondSingularPersonalPronoun(Lexicon lexicon) {
        return getBaseSingularPersonalPronoun(lexicon, Person.SECOND);
    }

    /**
     * Returns "I" in the current language
     */
    public static String getBaseFirstSingularPersonalPronoun(Lexicon lexicon) {
        return getBaseSingularPersonalPronoun(lexicon, Person.FIRST);
    }

    private static String getBaseSingularPersonalPronoun(Lexicon lexicon, Person p) {
        Map<String, Object> features = new HashMap<>();
        features.put(Feature.NUMBER, NumberAgreement.SINGULAR);
        features.put(Feature.PERSON, p);

        WordElement pronoun = lexicon.getWord(LexicalCategory.PRONOUN, features);

        return pronoun.getBaseForm();
    }

    static String getBasePluralPersonalPronoun(Lexicon lexicon, Person p) {
        Map<String, Object> features = new HashMap<>();
        features.put(Feature.NUMBER, NumberAgreement.PLURAL);
        features.put(Feature.PERSON, p);

        WordElement pronoun = lexicon.getWord(LexicalCategory.PRONOUN, features);

        return pronoun.getBaseForm();
    }

    /**
     * Creates a minimal vocabulary from the subclass language, by adding new {@link Designation}s in {@link Translator#vocabulary}
     */
    protected abstract void addBasicVocabulary();

    /**
     * Context : when asked to explain or define something, the AI uses a shell script that can take a language parameter such as "fr", "de", "en", etc. It will then returns the definition in that language, for a given word (considered in that language).
     * <p>
     * Override this method to get your definitions in a language other than the default one. See the script for more info.
     */
    public String getLanguageParameterForGetDefProgram() {
        return "";
    }

    public void say(String stringToSay) {
        System.out.println("[AI] " + stringToSay);
    }

    /**
     * Display an abstract sentence in a way the user can understand, thanks to the language provided by the subclass
     */
    public void say(SimpleSentence sentence) {
        say(realiser.realiseSentence(parseSentence(sentence, true)));
    }

    public void say(NLGElement sentence) {
        say(realiser.realiseSentence(sentence));
    }

    public void say(List<SimpleSentence> sentences) {
        CoordinatedPhraseElement coordination = nlgFactory.createCoordinatedPhrase();
        for (SimpleSentence s : sentences) {
            coordination.addCoordinate(parseSentence(s, false));
        }
        say(realiser.realiseSentence(addApostrophe(coordination)));
    }

    private NLGElement addApostrophe(NLGElement e) {
        CoordinatedPhraseElement politeCoordination = nlgFactory.createCoordinatedPhrase();
        politeCoordination.addCoordinate(e);
        politeCoordination.addCoordinate(getDefaultUserApostrophe());
        politeCoordination.setConjunction("");
        return politeCoordination;
    }

    /**
     * Probably something like "edu/stanford/nlp/models/lexparser/SOME_MODEL.ser.gz"
     */
    public abstract String getStanfordParserModelFilename();

    /**
     * From a {@link SimpleSentence}, makes an object that can be computed by the simpleNLG library, and possibly adds some features to improve the result
     */
    private NLGElement parseSentence(SimpleSentence sentence, boolean addApostrophe) {
        IEntity subject = sentence.getSubject();

        String objectString;
        IEntity object = null;

        if (sentence instanceof DeclarativeSentence) {
            object = ((DeclarativeSentence) sentence).getObject();
            objectString = computeEntityString(object);
        } else {
            objectString = getBestDesignation(((StativeSentence) sentence).getAdjective());
        }

        NLGElement p = nlgFactory.createClause(
                computeEntityString(subject),
                getBestDesignation(sentence.getVerb()),
                objectString);

        p.setFeature(Feature.NEGATED, sentence.isNegative());
        p.setFeature(Feature.TENSE, sentence.getTense());

        if (sentence.isInterrogative()) {
            if (subject.equals(EntityInterrogative.getInstance())) {
                p.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_SUBJECT);
            }
            if (object != null && object.equals(EntityInterrogative.getInstance())) {
                p.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);// Or InterrogativeType.WHO_OBJECT
            }
        } else if (addApostrophe) {
            p = addApostrophe(p);
        }

        return p;
    }

    public SPhraseSpec getSoftwareStartedSentence(String softwareName) {
        SPhraseSpec res = nlgFactory.createClause(null, getBestDesignation(StartSoftware.getInstance()), softwareName);
        res.setFeature(Feature.PASSIVE, true);
        return res;
    }

    public SPhraseSpec getSoftwareStoppedSentence(String softwareName) {
        SPhraseSpec res = nlgFactory.createClause(null, getBestDesignation(Stop.getInstance()), softwareName);
        res.setFeature(Feature.PASSIVE, true);
        res.setFeature(Feature.TENSE, Tense.PAST);
        return res;
    }

    public VPPhraseSpec getUnderstoodSentence() {
        VPPhraseSpec res = nlgFactory.createVerbPhrase(getBestDesignation(Understand.getInstance()));
        res.setFeature(Feature.FORM, Form.PAST_PARTICIPLE);
        return res;
    }

    /**
     * More or less toString() in the current language
     *
     * @return How the entity will be represented as a String after some SimpleNLG processing.
     */
    public String computeEntityString(IEntity entityParam, boolean definiteDeterminer) {
        String res;

        if (entityParam instanceof Entity) {
            Entity entity = (Entity) entityParam;

            if (entity.equals(Myself.getInstance())) {
                res = getBaseFirstSingularPersonalPronoun(lexicon);
            } else if (entity.equals(User.getInstance())) {
                res = getPolitenessPersonalPronoun();
            } else {
                NPPhraseSpec element = nlgFactory.createNounPhrase(
                        definiteDeterminer ? getDefiniteDeterminerFor(entity) : getIndefiniteDeterminerFor(entity),
                        getBestDesignation(entity.getConcept()));
                for (Adjective qualifier : entity.getCharacteristics()) {
                    element.addModifier(getBestDesignation(qualifier));
                }

                res = realiser.realise(element).getRealisation();
            }
        } else {
            res = getBestDesignation((AbstractEntityConcept) entityParam);
        }
        return res;
    }

    public String computeEntityString(EntityConcept entity, List<String> qualifiers) { // FIXME should just send the adjectives to the exception (see caller) instead of adj -> string -> adj -> string ?
        String res;

        NPPhraseSpec element = nlgFactory.createNounPhrase(
                getDeterminerFor(false),
                getBestDesignation(entity));
        for (String currentQualifier : qualifiers) {
            String qualifierChosen;
            Optional<Adjective> adjectiveConcept = VocabRetriever.getFirstConceptDesignatedBy(getVocabulary(), currentQualifier, Adjective.class);
            if (adjectiveConcept.isPresent()) {
                qualifierChosen = getBestDesignation(adjectiveConcept.get());
            } else {
                qualifierChosen = currentQualifier;
            }
            element.addModifier(qualifierChosen);
        }

        res = realiser.realise(element).getRealisation();

        return res;
    }

    /**
     * More or less toString() in the current language
     *
     * @return How the entity will be represented as a String after some SimpleNLG processing.
     */
    private String computeEntityString(IEntity entityParam) {
        return computeEntityString(entityParam, true);
    }

    /**
     * Returns a determiner, definite or not
     * TODO Ideally it should match the gender of one of the designation that will be used, but Simplenlg can correct it for now (la chat -> le chat) a determiner gender 1 will be chosen because there is a designation 1, but what guarantees that the designation 1 will be used ? le bête can be the two chosen words but nlg wil correct it in la bete : "une bête mange une souris" ai.parseAndProcessSentence(" une bête  signifie   un chat ");
     */
    private String getDeterminerFor(boolean definiteDeterminer) {
        return this.vocabulary.stream()
                .filter(d -> (definiteDeterminer ? d.getDesignatedConcept() instanceof DefiniteDeterminer
                        : d.getDesignatedConcept() instanceof IndefiniteDeterminer))
                .findFirst().get().getValue();
    }

    /**
     * Returns a definite determiner that fits the gender of one of the entity's designations
     */
    private String getDefiniteDeterminerFor(Entity entity) {
        return getDeterminerFor(true);
    }

    /**
     * Returns an indefinite determiner that fits the gender of one of the entity's designations
     */
    private String getIndefiniteDeterminerFor(Entity entity) {
        return getDeterminerFor(false);
    }

    public Lexicon getXMLLexicon() {
        if (this.lexicon == null) {
            this.lexicon = loadXMLLexicon();
        }
        return this.lexicon;
    }

    protected abstract Lexicon loadXMLLexicon();

    private String getBestDesignation(AbstractConcept concept) {
        int max = -1;
        String res = "PROBLEM";
        if (concept == null) {
            res = null;
        } else {
            for (Designation d : getDesignations(concept)) {
                if (d.getTimesUserUsedIt() > max) {
                    max = d.getTimesUserUsedIt();
                    res = d.getValue();
                }
            }
        }
        return res;
    }

    public List<Designation> getDesignations(AbstractConcept concept) {
        List<Designation> res = new LinkedList<>();
        for (Designation designation : this.vocabulary) {
            if (designation.getDesignatedConcept().equals(concept)) {
                res.add(designation);
            }
        }
        return res;
    }

    /**
     * returns the first designation of given concept, throwing an exception if none is known
     */
    public Designation getFirstDesignation(AbstractConcept concept) throws NotEnoughKnowledgeException {
        List<Designation> d = getDesignations(concept);
        if (d.isEmpty()) {
            throw new NotEnoughKnowledgeException("AI doesn't know a designation for " + concept);
        } else {
            return d.get(0);
        }
    }

    /**
     * In some languages, there is a more polite way to say "you" (the singular one).
     * Override with a call to {@link Translator#getBaseSingularPersonalPronoun(Lexicon, Person)} or {@link Translator#getBasePluralPersonalPronoun(Lexicon, Person)} if needed
     */
    protected String getPolitenessPersonalPronoun() {
        return getBaseSecondSingularPersonalPronoun(lexicon);
    }


    /**
     * Returns a way to call the user at first, such as "sir", or even a name. May be added to the end of sentences
     */
    public abstract String getDefaultUserApostrophe();

}
