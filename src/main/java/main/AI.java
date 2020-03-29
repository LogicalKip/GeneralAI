package main;

import exceptions.CantFindSuchAnEntityException;
import exceptions.NotEnoughKnowledgeException;
import exceptions.WrongGrammarRuleException;
import grammar.*;
import grammar.entity.*;
import grammar.sentence.DeclarativeSentence;
import grammar.sentence.Sentence;
import grammar.sentence.SimpleSentence;
import grammar.sentence.StativeSentence;
import grammar.verb.*;
import lombok.Getter;
import module.WikipediaModule;
import output.Translator;
import simplenlg.features.Tense;
import simplenlg.framework.NLGElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.UnexpectedException;
import java.util.*;

/**
 * TODO list (sans ordre particulier) :
 *
 * Faire de simplenlg un jar
 *
 * quoi [être] [XXXX] ? si ne sait pas, recherche avec WikipediaModule avant de renoncer
 *
 * apprentissage récursif sur wikipédia
 *
 * le chat qui mange la souris mange quoi ?
 *
 * catch outOfBoundseXception si pas assez de token pour une règle (rediriger vers la prochain regle ?)
 *
 * phrases d'état : pas besoin que la phrase avec "être" soit dans les faits. "le chat est quoi/petit ?" il faut aussi regarder dans les entités connues et leurs adjectifs quand c'est le verbe être
 *
 * pronoms personnels COD (le chat te regarde)
 *
 * réponses contextuelles, peut-être le matou mange quoi -> je ne sais pas, jamais entendu parler de matou -> matou signifie chat/un chat est un animal/etc, dans ce cas, le matou mange la souris. Une liste de questions auxquelles on a répondu "je ne sais pas" (avec : au fait, maintenant je sais que X mange Y) ?
 *
 * Liste de synonymes pour traiter "signifie" ? ça aiderait aussi pour "x signifie y ?" (x,y € {chat, quoi})
 *
 * si on autorise les mots absents du lexique (grâce à leur position), comment connaitre leurs bases, conjugaisons, pluriels, etc ?
 *
 * il faudrait déplacer "User" et "AI" (.instance()) dans entitiesKnown. Nécessaire pour "tu es grand. qui est grand ?"
 *
 * mergeEntities : adjectifs à fusionner
 *
 * tu/le chat est quoi ? -> quelle_entité et quel_adjectif
 *
 * pronom personnel 3ème personne
 *
 * dire monsieur aléatoirement au lieu de tout le temps
 *
 * où gérer les getXXXSentence plus proprement (sans faire plein d'aller-retours entre classes, si possible) ? Une factory ?
 *
 * pluriel
 *
 * ordres négatifs ?
 *
 * oui/non comme concepts. à utiliser lors de la réponse à une yesNo question
 * 
 * faire en sorte que les say en dur soient des Sentence créées dynamiquement, comme pour "je ne sais pas"
 * 
 * phrases négatives : vérification des incohérences (que faire si ça arrive ?) (on risque aussi de mergeEntities 2 entités qui possèdent des phrases mutuellement exclusives)
 * 
 * factoriser les messages de NotEnoughKnowledgeException (dans le constructeur) à partir du concept dont la designation est manquante
 * 
 * wiki github pour expliquer les différents cas d'utilisation, limitations selon les OS, installation
 * 
 * source d'erreur possible dans la grammaire : ça commence par marcher, on crée une entité ou du vocabulaire (dans la liste "newXXX"), puis la suite ne colle pas, donc on revient en arrière, on essaie avec une autre règle, elle marche, on recrée l'entite/vocab, elle marche jusqu'au bout et tout finit bien, mais on a deux fois l'entité/vocab dans la liste (voire deux légèrement différentes, dont une fausse). Faut-il réinitialiser les listes à chaque WrongGrammarRuleException (par exemple, en le forçant, en devant passer les listes au constructeur, qui les vide) ? Il faut rollback completement, aller dans une mauvais règle ne devrait avoir aucune influence. être ~LL(1) règle (peut-être ?) ça, mais la grammaire ne le restera pas forcément éternellement
 * 
 * signifie pourrait avoir plusieurs sens :
 * ce chat est le même animal que cet autre chat
 * les chats et les minets, c'est le même concept
 * 
 * Order utilise des ~Entity au lieu de String.
 * Noms propres
 * 
 * "Il y a" + new entity
 * 
 * "est le contraire de" (adjectifs). Ensuite, répondre aux questions en prenant en compte les contraires pour déduire (pas vivant = mort)
 * 
 * pas de déterminant -> on fait référence au concept (?) (matou signifie chat). Une différence avec déterminant ?
 * 
 * Changer la voix passive selon ce qui était demandé (p.setFeature(Feature.PASSIVE, true);) ? : (qui mange la pomme vs john mange quoi) Passive (eg, "John eats an apple" vs "An apple is eaten by John")
 * 
 * plusieurs faits dans une phrase (ET, virgule)
 * 
 * besoin de equals() dans certaines classes de grammaire ?
 * 
 * faire de vocabulary une map <designation->Concept> ?
 * 
 * "ces deux instances d'animaux sont la même boule de poils" alors que l'une est un chat et l'autre un cheval
 * 
 * héritage de concepts. chat_5=instance de chat, qui est un mammifère, qui est un animal, qui est un être vivant
 * 
 * faire des interfaces au lieu de classes pour les concepts pour qu'un mot puisse en etre plusieurs à la fois ?
 *
 * si l'utilisateur utilise des verbes d'état (Stative) inconnus, suivis d'un adjectif comme prévu, devrait-on les reconnaitre comme tels (Verb.IS_STATIVE_VERB = true), et rajouter la phrase dans la liste des faits (et bien sûr aucun lien concernant l'adjectif et l'entité, car ce verbe n'a aucun sens pour l'ia) ?
 * 
 * passer la phrase en .lower() ? (pb avec noms propres plus tard ?)
 *     
 * A debugger :
 * 
 * ex-problème datant de stanford, qui pourrait se reposer quand on autorisera des mots absents du lexique, en en déduisant le type selon la position
 * [AI] Initializing...
 * [AI] Ready.
 * un chat blanc mange une croquette
 * [AI] Compris.
 * qui mange quoi ?
 * [AI] Le chat blanc mangerait **le** croquette.
 * car le mot n'est pas dans le lexique. Même en demandant un "la", ça met un "le" par défaut car le genre n'est pas connu dans le lexique je suppose
 * 
 * qui est pas quoi ?
 * (probleme car "être" suppose un adjectif, tel que codé actuellement, alors que "quoi" représente WHAT_ENTITY, on n'a pas de WHAT_ADJECTIVE)
 */

public class AI {
    /**
     * A list of facts learned from the outside
     */
    @Getter
    private Set<SimpleSentence> knowledge;
    /**
     * The instances of entity we know about. Ex : that cat, that other cat over there, the user.
     * The ones that have been mentioned last must go at the end of the list so that it can be guessed which one the user will be talking about.
     */
    @Getter
    private List<Entity> entitiesKnown;
    private Scanner kb;
    private boolean stopProgram;
    private Translator translator;
    private FrenchGrammar parser;
    private WikipediaModule wikipediaModule;

    public AI(Translator translator) {
        this.translator = translator;
        say("Initializing...");

        this.knowledge = new HashSet<>();
        this.addBasicKnowledge();
        this.wikipediaModule = new WikipediaModule();
        this.stopProgram = false;
        this.entitiesKnown = new LinkedList<>();
        this.parser = new FrenchGrammar(this.translator.getXMLLexicon(), this.translator.getVocabulary(), this.entitiesKnown);

        kb = new Scanner(System.in);
    }

    public void start() {
        say("Ready.");

        while (!stopProgram) {
            String input = getInput();
            parseAndProcessSentence(input);
        }
        terminate();
    }

    void parseAndProcessSentence(String input) {
        try {
            Sentence parsedInput = parser.parse(input);

            if (parsedInput instanceof Order) {
                obeyOrder((Order) parsedInput);
            } else if (parsedInput instanceof SimpleSentence) {
                this.translator.getVocabulary().addAll(parser.getNewVocabulary());

                processSimpleSentenceFromUser((SimpleSentence) parsedInput, parser.getNewEntities());
            }
        } catch (NotEnoughKnowledgeException e) {
            say(e.getMessage());
        } catch (CantFindSuchAnEntityException e) {
            processNoSuchEntityException(e);
        } catch (WrongGrammarRuleException e) {
            say(getIDontUnderstandSentence());
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
    }

    private void processNoSuchEntityException(CantFindSuchAnEntityException exception) {
        StringBuilder res = new StringBuilder("I'm not aware of " + translator.computeEntityString(exception.getConcept(), exception.getQualifiers()) + " ");

        List<Entity> similarEntities = new LinkedList<>();
        for (Entity e : entitiesKnown) {
            if (exception.getConcept().equals(e.getConcept())) {
                similarEntities.add(e);
            }
        }

        List<Designation> designations = translator.getDesignations(exception.getConcept());
        String designation = designations.isEmpty() ?
                "of those things" :
                designations.get(0).getValue();

        if (similarEntities.isEmpty()) {
            res.append("In fact, I don't even know any ").append(designation);
        } else {
            res.append("I only know of :");
            for (Entity e : similarEntities) {
                res.append("\n\t- ").append(translator.computeEntityString(e, false));
            }
        }
        say(res.toString());
    }

    /**
     * Called at the beginning
     */
    private void addBasicKnowledge() {
        // Not much for now !
    }

    private void obeyOrder(Order order) throws NotEnoughKnowledgeException {
        Verb orderMeaning = order.getVerb();
        if (orderMeaning instanceof Stop) {
            if (order.getObject() == null) {
                this.stopProgram = true;
            } else {
                try {
                    executeCommand("killall " + order.getObject());
                    say(translator.getSoftwareStoppedSentence(order.getObject()));
                } catch (IOException e) {
                    say("Je ne sais pas comment faire ça.");
                } catch (NoSuchElementException e) {
                    say("Je ne vois pas de quoi vous parlez.");
                }
            }
        } else if (orderMeaning instanceof StartSoftware) {
            startSoftware(order.getObject());
        } else if (orderMeaning instanceof Explain) {
            if (order.getObject() == null) {
                say(getExplainWhatSentence());
            } else {
                try {
                    say(wikipediaModule.getDefinition(order.getObject(), translator.getLanguageParameterForGetDefProgram()));
                } catch (IOException e) {
                    say("Je ne saurais pas définir ça."); //FIXME nlg-created sentence instead of hardcoded
                }
            }
        }
    }

    private void startSoftware(String software) throws NotEnoughKnowledgeException {
        if (software == null) {
            say(getStartWhatSentence());
        } else {
            try {
                String command = software.toLowerCase();
                executeBackgroundCommand(command);
                say(translator.getSoftwareStartedSentence(software));
            } catch (IOException e) {
                say("\"" + software + "\" ne semble pas être une commande correcte");
            }
        }
    }

    private void executeBackgroundCommand(String command) throws IOException {
        Runtime.getRuntime().exec(command);
    }

    /**
     * Executes a shell command and returns the output.
     * Will not return as long as the process has output to send (~ as long as it is alive)
     *
     * @return The output returned by the command (ex : the files for a ls)
     */
    private String executeCommand(String command) throws IOException, NoSuchElementException {
        StringBuilder output = new StringBuilder();

        Process p;
        p = Runtime.getRuntime().exec(command);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        if (errorReader.readLine() != null) {
            throw new NoSuchElementException();
        }

        return output.toString();
    }

    private void processSimpleSentenceFromUser(SimpleSentence simpleSentence, List<Entity> newEntities) throws UnexpectedException {
        if (simpleSentence.isInterrogative()) {
            answer(simpleSentence);
        } else if (this.knowledge.contains(simpleSentence)) {
            say(getIKnowSentence());
        } else {
            boolean hasReplied = false;
            if (!(simpleSentence.getVerb() instanceof HasSameMeaningAs)) {
                this.knowledge.add(simpleSentence);
            }
            Verb meaning = simpleSentence.getVerb();

            if (simpleSentence instanceof StativeSentence) {
                if (meaning instanceof Be && !simpleSentence.isNegative()) {
                    StativeSentence stativeSentence = (StativeSentence) simpleSentence;
                    ((Entity) stativeSentence.getSubject()).addCharacteristic(stativeSentence.getAdjective());
                }
                this.entitiesKnown.addAll(newEntities);
            } else {
                DeclarativeSentence decl = (DeclarativeSentence) simpleSentence;
                if (meaning instanceof HasSameMeaningAs) {
                    if (decl.getSubject() instanceof Entity &&
                            decl.getObject() instanceof Entity) {
                        EntityConcept subjectConcept = ((Entity) decl.getSubject()).getConcept();
                        EntityConcept objectConcept = ((Entity) decl.getObject()).getConcept();
                        if (subjectConcept.equals(objectConcept)) {
                            say(getIKnowSentence());
                            hasReplied = true;
                        } else {
                            mergeEntityConcepts(
                                    subjectConcept,
                                    objectConcept);
                        }
                    }
                } else {
                    this.entitiesKnown.addAll(newEntities);
                }
            }

            if (!hasReplied) {
                say(translator.getUnderstoodSentence());
            }
        }

        // Move mentioned entities at the end so that, in the future, given an incomplete description of an entity, we may pick those at the end because they are the most likely to be referred to (they were the last mentioned)
        for (Entity e : simpleSentence.getMentionedEntities()) {
            if (entitiesKnown.contains(e)) {
                this.entitiesKnown.remove(e);
                this.entitiesKnown.add(e);
            }
        }
    }

    /**
     * Learns that the two given (entity) concepts refer to the same thing
     * It is implied that the vocabulary has designations referring to both parameters
     */
    private void mergeEntityConcepts(EntityConcept a, EntityConcept b) {
        // Merge in vocabulary
        for (Designation currDesignation : this.translator.getVocabulary()) {
            if (currDesignation.getDesignatedConcept().equals(a)) {
                currDesignation.setDesignatedConcept(b);
            }
        }

        // Merge in entities known
        for (Entity currEntity : this.entitiesKnown) {
            if (currEntity.getConcept().equals(a)) {
                currEntity.setConcept(b);
            }
        }

        // Later, if those concepts have any, merge their attributes
    }

    /**
     * Learns that the two given entities refer to the same thing
     * It is implied that the vocabulary has designations referring to both parameters
     */
    private void mergeEntities(Entity a, Entity b) {
        for (SimpleSentence currSentence : this.knowledge) {
            currSentence.replace(a, b);
        }
        this.entitiesKnown.remove(a);
        // Later, merge their attributes if they have any
    }


    private void answer(SimpleSentence question) throws UnexpectedException {
        if (question.isInterrogative()) {
            if (question.getVerb() instanceof HasSameMeaningAs) {
                if (question instanceof DeclarativeSentence) {
                    answerQuestionAboutSynonyms(((DeclarativeSentence) question));
                } else {
                    throw new UnexpectedException("ERROR - Question about " + HasSameMeaningAs.getInstance() + " is not a " + DeclarativeSentence.class + ", but a " + question.getClass());
                }
            } else {
                List<SimpleSentence> answers = new LinkedList<>();

                Object[] questionConcepts = question.split();
                boolean yesNoQuestion = true;
                for (Object c : questionConcepts) {
                    if (c instanceof InterrogativeWord) {
                        yesNoQuestion = false;
                        break;
                    }
                }

                for (SimpleSentence currFact : this.knowledge) {
                    boolean answersTheQuestion = deducingIfFactAnswersTheQuestion(question, questionConcepts, yesNoQuestion, currFact);

                    if (answersTheQuestion) {
                        answers.add(currFact);
                    }
                }

                if (answers.isEmpty()) {
                    say(getIDontKnowSentence());
                } else {
                    if (yesNoQuestion) {
                        answerYesNoQuestion(answers.get(0).isNegative(), question.isNegative());
                    } else {
                        List<SimpleSentence> actualAnswers = new LinkedList<>();
                        for (SimpleSentence a : answers) {
                            if (a.isNegative() == question.isNegative()) {
                                try {
                                    SimpleSentence actualAnswer = (SimpleSentence) a.clone();
                                    actualAnswer.setTense(Tense.CONDITIONAL);
                                    actualAnswers.add(actualAnswer);
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        say(actualAnswers);
                    }
                }
            }
        } else {
            System.err.println("Can't answer what is not a question. Should never happen :/");
        }
    }

    private void answerYesNoQuestion(boolean answerIsNegative, boolean questionIsNegative) {
        if (answerIsNegative == questionIsNegative) {
            say("En effet.");
        } else {
            if (questionIsNegative) {
                say("Si.");
            } else {
                say("Non.");
            }
        }
    }

    /**
     * Answers question where its verb is HasSameMeaningAs
     */
    private void answerQuestionAboutSynonyms(DeclarativeSentence question) {
        IEntity subject = question.getSubject();
        IEntity object = question.getObject();

        boolean subjectKnown = !(subject instanceof EntityInterrogative);
        boolean objectKnown = !(object instanceof EntityInterrogative);
        if (subjectKnown && objectKnown) {
            boolean conceptsDifferent = !(((Entity) subject).getConcept().equals(((Entity) object).getConcept()));
            answerYesNoQuestion(conceptsDifferent, question.isNegative());
        } else if (!subjectKnown && !objectKnown) {
            say("Je ne suis pas programmé pour philosopher.");
        } else if (subjectKnown && !objectKnown) {
            respondWithSynonymsOf((Entity) subject, question.isNegative());
        } else if (!subjectKnown && objectKnown) {
            respondWithSynonymsOf((Entity) object, question.isNegative());
        }
    }

    private void respondWithSynonymsOf(Entity entity, boolean questionWasNegative) {
        if (questionWasNegative) {
            say("Beaucoup de choses, je le crains.");
        } else {
            List<String> wordsMatchingConcept = getWordsMatchingConcept(entity.getConcept());
            if (wordsMatchingConcept.size() == 1) {
                say("Je ne connais aucun synonyme de \"" + wordsMatchingConcept.get(0) + "\".");
            } else {
                StringBuilder answer = new StringBuilder();
                if (wordsMatchingConcept.size() == 2) {
                    answer.append(wordsMatchingConcept.get(0)).append(" et ").append(wordsMatchingConcept.get(1)).append(" sont synonymes.");
                } else {
                    for (String match : wordsMatchingConcept) {
                        answer.append(match).append(", ");
                    }
                    answer.append("sont tous synonymes.");
                }
                say(answer.toString());
            }
        }
    }

    private List<String> getWordsMatchingConcept(EntityConcept concept) {
        List<String> synonyms = new LinkedList<>();
        for (Designation designation : translator.getVocabulary()) {
            if (designation.getDesignatedConcept().equals(concept)) {
                synonyms.add(designation.getValue());
            }
        }
        return synonyms;
    }

    private boolean deducingIfFactAnswersTheQuestion(SimpleSentence question, Object[] questionConcepts, boolean yesNoQuestion, SimpleSentence currFact) {
        boolean answersTheQuestion;
        if (!yesNoQuestion && question.isNegative() != currFact.isNegative()) {
            answersTheQuestion = false;
        } else {
            answersTheQuestion = true;
            Object[] currFactConcepts = currFact.split();
            for (int i = 0; i < questionConcepts.length; i++) { // Looking at the question and potential answer in parallel
                Object currentQuestionConcept = questionConcepts[i];
                Object currentAnswerConcept = currFactConcepts[i];

                if (currentQuestionConcept instanceof InterrogativeWord) {
                    continue;
                }
                if (!currentQuestionConcept.equals(currentAnswerConcept)) {
                    answersTheQuestion = false;
                    break;
                }
            }
        }
        return answersTheQuestion;
    }

    /**
     * Returns a sentence that means "I don't know", without specifying what
     */
    private SimpleSentence getIDontKnowSentence() {
        return getIKnowSentence().negate();
    }

    /**
     * Returns a sentence that means "I know", without specifying what
     */
    private SimpleSentence getIKnowSentence() {
        return new DeclarativeSentence(Myself.getInstance(), Knowing.getInstance());
    }

    /**
     * "What do I [verb] ?"
     */
    private SimpleSentence getVerbWhatSentence(Verb verb) {
        SimpleSentence res = new DeclarativeSentence(Myself.getInstance(), verb, EntityInterrogative.getInstance());
        res.setInterrogative(true);
        return res;
    }

    private SimpleSentence getStartWhatSentence() {
        return getVerbWhatSentence(StartSoftware.getInstance());
    }

    private SimpleSentence getExplainWhatSentence() {
        return getVerbWhatSentence(Explain.getInstance());
    }

    private SimpleSentence getIStopMyselfSentence() {
        return new DeclarativeSentence(Myself.getInstance(), Stop.getInstance(), Myself.getInstance());
    }

    private SimpleSentence getIDontUnderstandSentence() {
        SimpleSentence res = new DeclarativeSentence(Myself.getInstance(), Understand.getInstance(), null);
        res.setNegative();
        return res;
    }

    private void say(NLGElement s) {
        this.translator.say(s);
    }

    private void say(String s) {
        this.translator.say(s);
    }

    private void say(List<SimpleSentence> s) {
        this.translator.say(s);
    }

    private void say(SimpleSentence s) {
        this.translator.say(s);
    }


    private void terminate() {
        say(getIStopMyselfSentence());
        if (kb != null) {
            kb.close();
        }
    }

    private String getInput() {
        return kb.nextLine();
    }

    private boolean isOnWindows() {
        return System.getProperty("os.name").contains("Windows");
    }
}
