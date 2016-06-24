package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import exceptions.CantFindSuchAnEntityException;
import exceptions.NotEnoughKnowledgeException;
import exceptions.ParserException;
import exceptions.WrongGrammarRuleException;
import grammar.AbstractConcept;
import grammar.DeclarativeSentence;
import grammar.SimpleSentence;
import grammar.Designation;
import grammar.Entity;
import grammar.EntityConcept;
import grammar.EntityInterrogative;
import grammar.Explain;
import grammar.HasSameMeaningAs;
import grammar.InterrogativeWord;
import grammar.Knowing;
import grammar.Myself;
import grammar.Order;
import grammar.Sentence;
import grammar.StartSoftware;
import grammar.StativeSentence;
import grammar.Stop;
import grammar.Understand;
import grammar.Verb;
import simplenlg.features.Tense;
import simplenlg.framework.NLGElement;

/*
 * TODO list :
 * 
 * rajouter les nouveaux verbes d'état dynamiques comme pour ceux d'action
 * 
 * phrases d'état : pas besoin que la phrase avec "être" soit dans les faits. "le chat est quoi/petit ?" il faut aussi regarder dans les entités connues et leurs adjectifs quand c'est le verbe être
 * 
 * "je suis un homme" -> getBase() donne suivre ou être ?
 * 
 * mergeEntities : adjectifs à fusionner
 * 
 * tu/le chat est quoi ? -> quelle_entité et quel_adjectif
 * 
 * pronom personnel 3ème personne
 * 
 * Grammar : factorisation possible ?
 * 
 * dire monsieur aléatoirement au lieu de tout le temps
 * 
 * où gérer les getXXXSentence plus proprement (sans faire plein d'aller-retours entre classes, si possible) ? Une factory ?
 * 
 * pluriel
 * 
 * négation : "ne" en plus de "pas", gérer les différents endroits possibles dans la phrase où on peut le trouver
 * 
 * comment utiliser dynamiquement SNLG en anglais ou français ?
 * 
 * ordres négatifs ?
 * 
 * verbes d'état et phrases avec verbe d'état+adjectifs
 * 
 * quoi [être] [XXXX] ? si ne sait pas, recherche avec showDef avant de renoncer
 *
 * remplacer les ajouts manuels de "le", "une", etc (peut-être même "quoi") par des recherches dans le lexique
 * 
 * possible de réduire les redondances dues à l'utilisation de Stanford, SimpleNLG et l'ajout manuel de mots ? (ex de piste : (PROWH dans l'arbre) + ("quoi" signifie QUELLE_ENTITE dans le Translator), surement d'autres trucs)
 * 
 * oui/non comme concepts. à utiliser lors de la réponse à une yesNo question
 * 
 * faire en sorte que les say en dur soient des Sentence créées dynamiquement (donc dans le langage de l'utilisateur), comme pour "je ne sais pas"
 * 
 * phrases négatives : vérification des incohérences (que faire si ça arrive ?) (on risque aussi de mergeEntities 2 entités qui possèdent des phrases négatives mutuellement exclusives)
 * 
 * apostrophes d'élision (en entrée) : "l'homme", "n'est", etc
 * 
 * factoriser les messages de NotEnoughKnowledgeException (dans le constructeur) à partir du concept dont la designation est manquante
 * 
 * gérer plusieurs utilisateurs potentiels (en gardant les infos sur eux), et pouvoir en changer (comment ?)
 * 
 * wiki github pour expliquer les différents cas d'utilisation, limitations selon les OS, installation, indiquer qu'il faut Tree.pennPrint() pour afficher l'arbre
 * 
 * source d'erreur possible dans la grammaire : ça commence par marcher, on crée une entité ou du vocabulaire (dans la liste "newXXX"), puis la suite ne colle pas, donc on revient en arrière, on essaie avec une autre règle, elle marche, on recrée l'entite/vocab, elle marche jusqu'au bout et tout finit bien, mais on a deux fois l'entité/vocab dans la liste (voire deux légèrement différentes, dont une fausse). Faut-il réinitialiser les listes à chaque WrongGrammarRuleException (par exemple, en le forçant, en devant passer les listes au constructeur, qui les vide) ? Il faut rollback completement, aller dans une mauvais règle ne devrait avoir aucune influence. être ~LL(1) règle ça, mais la grammaire ne le restera pas forcément éternellement
 * 
 * signifie pourrait avoir plusieurs sens :
 * ce chat est le même animal que cet autre chat
 * les chats et les minets, c'est le même concept
 * 
 * Order utilise des ~Entity au lieu de String. 
 * Noms propres
 * 
 * "There is" + new entity
 * 
 * gérer les "~tokens" de la grammaire avec des Tree faits main (notamment pour des cas un peu compliqués très différents des autres langues, comme "il y a" -> (VN (CLS il) (CLO y) (V a)))
 *
 * pas de déterminant -> on fait référence au concept (?) (matou signifie chat)
 * 
 * Changer la voix passive selon ce qui était demandé (p.setFeature(Feature.PASSIVE, true);) ? : (qui mange la pomme vs john mange quoi) Passive (eg, "John eats an apple" vs "An apple is eaten by John")
 * 
 * ne pas faire confiance à l'utilisateur
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
 * Deux désignations d'un genre différent peuvent (probablement) désigner le même concept. C'est le mot/désignation qui a un genre au final, pas le concept lui-même
 * 
 * A debugger :
 * 
[AI] Initializing...
[AI] Ready.
un chat blanc mange une croquette
[AI] Compris.
qui mange quoi ?
[AI] Le chat blanc mangerait **le** croquette.
 car le mot n'est pas dans le lexique. Même en demandant un "la", ça met un "le" par défaut car le genre n'est pas connu dans le lexique je suppose
 *
 quoi signifie quoi ?
 [AI] PROBLEM signifierait le chat, monsieur.
 *
 qui regarde pas quoi ?
(SENT
  (NP (PROREL qui))
  (VN (V regarde))
  (ADV pas)
  (NP (PROWH quoi))
  (PUNC ?))
[AI] Le chat ne regarderait pas la souris.
qui est pas quoi ?
(SENT
  (NP (PROREL qui))
  (VN (V est))
  (MWADV (ADV pas) (PRO quoi))
  (PUNC ?))
[AI] I don't understand that.
 *
 un mignon petit chat noir regarde un chat blanc
(SENT
  (NP (DET un)
    (AP (ADJ mignon)))
  (NP (ADJ petit) (NC chat)
    (AP (ADJ noir)))
  (VN (V regarde))
  (NP (DET un) (NC chat)
    (AP (ADJ blanc))))
[AI] I don't understand that.
 *
 le chat mange le chien
 (SENT
  (NP (DET le) (NC chat)
    (MWN (N mange) (DET le) (N chien))))
[AI] I don't understand that.
 */

public class AI {
	/**
	 * A list of facts learned from the outside
	 */
	private List<SimpleSentence> knowledge;
	/**
	 * The instances of entity we know about. Ex : that cat, that other cat over there, the user.
	 * The ones that have been mentioned last must go at the end of the list so that it can be guessed which one the user will be talking about.
	 */
	private List<Entity> entitiesKnown;
	private Scanner kb;
	private boolean stopPrgm;
	private Translator translator;
	private StanfordParser parser;

	public AI() {
		this.translator = new FrenchTranslator(this);
		say("Initializing...");

		this.knowledge = new LinkedList<SimpleSentence>();
		this.addBasicKnowledge();
		this.stopPrgm = false;
		this.entitiesKnown = new LinkedList<Entity>();
		this.parser = new StanfordParser(translator.getStanfordParserModelFilename(), translator.getVocabulary(), this.entitiesKnown, translator.getXMLLexicon()); 

		kb = new Scanner(System.in);	    
	}


	public void start() {
		say("Ready.");

		while (! stopPrgm) {
			try {
				Sentence parsedInput = parser.parse(getInput());

				if (parsedInput instanceof Order) {
					obeyOrder((Order) parsedInput);
				}
				else if (parsedInput instanceof SimpleSentence) {
					this.translator.getVocabulary().addAll(parser.getNewVocabulary());

					processSimpleSentenceFromUser((SimpleSentence) parsedInput, parser.getNewEntities());
				}
			} catch (ParserException | NotEnoughKnowledgeException e) {
				say(e.getMessage());
			} catch (CantFindSuchAnEntityException e) {
				processNoSuchEntityException(e);
			} catch (WrongGrammarRuleException e) {
				say(getIDontUnderstandSentence());
			}
		}
		terminate();
	}

	private void processNoSuchEntityException(CantFindSuchAnEntityException exception) {
		String res = "I'm not aware of " + translator.computeEntityString(exception.getConcept(), exception.getQualifiers()) + ". ";

		List<Entity> similarEntities = new LinkedList<Entity>();
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
			res += "In fact, I don't even know any " + designation;
		} else {
			res += "I only know of :";
			for (Entity e : similarEntities) {
				res += "\n\t- " + translator.computeEntityString(e, false);
			}
		}
		say(res);
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
				this.stopPrgm = true;
			} else {
				try {
					executeCommand("killall " + order.getObject());
					say(translator.getSoftwareStoppedSentence(order.getObject()));
				} catch (IOException e) {
					say("I don't know how to do that");
				} catch (NoSuchElementException e) {
					say("There is no such program to stop");
				}
			}
		} else if (orderMeaning instanceof StartSoftware) {
			startSoftware(order.getObject());
		} else if (orderMeaning instanceof Explain) {
			if (order.getObject() == null) {
				say(getExplainWhatSentence());
			} else {
				try {
					say(executeCommand("./showDef " + translator.getLanguageParameterForGetDefProgram() + " " + order.getObject()));
				} catch (IOException e) {
					say("I don't know how to explain that");
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
				say("I don't know any software named " + software);
			}
		}
	}

	private void executeBackgroundCommand(String command) throws IOException {
		Runtime.getRuntime().exec(command);
	}

	/**
	 * Executes a shell command and returns the output.
	 * Will not return as long as the process has output to send (~ as long as it is alive)
	 * @return The output returned by the command (ex : the files for a ls)
	 */
	private String executeCommand(String command) throws IOException, NoSuchElementException {
		StringBuffer output = new StringBuffer();

		Process p;
		p = Runtime.getRuntime().exec(command);
		BufferedReader reader = 
				new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";			
		while ((line = reader.readLine())!= null) {
			output.append(line + "\n");
		}

		BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		if (errorReader.readLine() != null) {
			throw new NoSuchElementException();
		}

		return output.toString();
	}

	private void processSimpleSentenceFromUser(SimpleSentence simpleSentence, List<Entity> newEntities) {
		if (simpleSentence.isInterrogative()) {
			answer(simpleSentence);
		} else if (this.knowledge.contains(simpleSentence)) {
			say(getIKnowSentence());
		} else {
			this.knowledge.add(simpleSentence);
			Verb meaning = (Verb) simpleSentence.getVerb();

			if (simpleSentence instanceof StativeSentence) {
				StativeSentence stativeSentence = (StativeSentence) simpleSentence;
				((Entity) stativeSentence.getSubject()).addCharacteristic(stativeSentence.getAdjective());
				
				this.entitiesKnown.addAll(newEntities);
			} else {
				if (meaning instanceof HasSameMeaningAs) {
					DeclarativeSentence decl = (DeclarativeSentence) simpleSentence;
					if (decl.getSubject() instanceof Entity &&
							decl.getObject() instanceof Entity) {
						mergeEntityConcepts(
								((Entity)decl.getSubject()).getConcept(), 
								((Entity)decl.getObject()).getConcept());
					}
				} else {
					this.entitiesKnown.addAll(newEntities);
				}
			}
			
			
			removeDuplicatesFromKnowledge();
			say(translator.getUnderstoodSentence());
		}

		// Move mentioned entities at the end so that, in the future, given an incomplete description of an entity, we may pick those at the end because they are the most likely to be referred to (they were the last mentioned)
		for (Entity e : simpleSentence.getMentionedEntities()) {
			if (entitiesKnown.contains(e)) {
				this.entitiesKnown.remove(e);
				this.entitiesKnown.add(e);
			}
		}
	}

	private void removeDuplicatesFromKnowledge() {
		List<SimpleSentence> updatedKnowledge = new LinkedList<SimpleSentence>();

		for (SimpleSentence sentence : this.knowledge) {
			if (! updatedKnowledge.contains(sentence)) {
				updatedKnowledge.add(sentence);
			}
		}

		this.knowledge = updatedKnowledge;
	}

	/**
	 * Learns that the two given (entity) concepts refer to the same thing
	 * It is supposed that the vocabulary has designations referring to both parameters 
	 */
	private void mergeEntityConcepts(EntityConcept a, EntityConcept b) {
		for (Designation currDesignation : this.translator.getVocabulary()) {
			if (currDesignation.getDesignatedConcept().equals(a)) {
				currDesignation.setDesignatedConcept(b);
			}
		}

		for (Entity currEntity : this.entitiesKnown) {
			if (currEntity.getConcept().equals(a)) {
				currEntity.setConcept(b);
			}
		}

		// Later, merge their attributes if they have any
	}

	/**
	 * Learns that the two given entities refer to the same thing
	 * It is supposed that the vocabulary has designations referring to both parameters 
	 */
	private void mergeEntities(Entity a, Entity b) {
		for (SimpleSentence currSentence : this.knowledge) {
			currSentence.replace(a, b);
		}
		this.entitiesKnown.remove(a);
		// Later, merge their attributes if they have any
	}


	private void answer(SimpleSentence question) {
		if (question.isInterrogative()) {
			List<SimpleSentence> answers = new LinkedList<SimpleSentence>();

			Object questionConcepts[] = question.split();
			boolean yesNoQuestion = true;
			for (Object c : questionConcepts) {
				if (c instanceof InterrogativeWord) {
					yesNoQuestion = false;
					break;
				}
			}

			for (SimpleSentence currFact : this.knowledge) {
				boolean answersTheQuestion = true;
				Object currFactConcepts[] = currFact.split();
				for (int i = 0 ; i < questionConcepts.length ; i++) { // Looking at the question and potential answer in parallel
					Object currentQuestionConcept = questionConcepts[i];
					Object currentAnswerConcept = currFactConcepts[i];
					if (! (currentQuestionConcept instanceof InterrogativeWord) && 
							!currentQuestionConcept.equals(currentAnswerConcept)) {
						answersTheQuestion = false;
						break;
					}
				}

				if (answersTheQuestion) {
					answers.add(currFact);
				}
			}

			if (answers.isEmpty()) {
				say(getIDontKnowSentence());
			} else {
				if (yesNoQuestion) {
					if (answers.get(0).isNegative() == question.isNegative()) {
						say("Indeed");
					} else {
						say("No");
					}
				} else {
					List<SimpleSentence> actualAnswers = new LinkedList<SimpleSentence>();
					for (SimpleSentence a : answers) {
						if (a.isNegative() == question.isNegative()) {
							SimpleSentence actualAnswer = null;
							try {
								actualAnswer = (SimpleSentence) a.clone();
							} catch (CloneNotSupportedException e) {
								e.printStackTrace();
							}
							actualAnswer.setTense(Tense.CONDITIONAL);
							actualAnswers.add(actualAnswer);
						}
					}
					say(actualAnswers);	
				}
			}
		} else {
			System.err.println("Can't answer what is not a question. Should never happen :/");
		}
	}

	/**
	 * Returns a sentence that means "I don't know", without specifying what
	 */
	private SimpleSentence getIDontKnowSentence() {
		SimpleSentence res = getIKnowSentence();
		res.setNegative();
		return res;
	}

	/**
	 * Returns a sentence that means "I know", without specifying what
	 */
	private SimpleSentence getIKnowSentence() {
		return new DeclarativeSentence(Myself.getInstance(), Knowing.getInstance(), null);
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
		SimpleSentence res = new DeclarativeSentence(Myself.getInstance(), Stop.getInstance(), Myself.getInstance());
		return res;
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
		String res;
		res = kb.nextLine();

		return res;
	}

	/**
	 * Searches the given vocabulary for a classLookedFor object designated by designation. If several exist somehow, return the first.
	 * Returns null if none are found
	 */
	public static AbstractConcept getFirstConceptDesignatedBy(final List<Designation> vocabulary, final String designation, final Class<?> classLookedFor) {
		AbstractConcept res;
		List<AbstractConcept> allConcepts = getAllConceptsDesignatedBy(vocabulary, designation, classLookedFor);
		if (allConcepts.isEmpty()) {
			res = null;
		} else {
			res = allConcepts.get(0);
		}
		return res;
	}


	/**
	 * Searches the given vocabulary for all classLookedFor objects designated by designation.
	 * Returns an empty list if none are found
	 */
	public static List<AbstractConcept> getAllConceptsDesignatedBy(final List<Designation> vocabulary, final String designation, final Class<?> classLookedFor) {
		List<AbstractConcept> res = new LinkedList<AbstractConcept>();
		for (Designation d : getAllDesignationFrom(vocabulary, designation, classLookedFor)) {
			res.add(d.getDesignatedConcept());
		}
		return res;
	}

	public static Designation getFirstDesignationFrom(final List<Designation> vocabulary, final String designation, final Class<?> classLookedFor) {
		Designation res;
		List<Designation> allConcepts = getAllDesignationFrom(vocabulary, designation, classLookedFor);
		if (allConcepts.isEmpty()) {
			res = null;
		} else {
			res = allConcepts.get(0);
		}
		return res;
	}


	public static List<Designation> getAllDesignationFrom(final List<Designation> vocabulary, final String designation, final Class<?> classLookedFor) {
		List<Designation> res = new LinkedList<Designation>();
		for (Designation d : vocabulary) {
			if (d.getValue().equals(designation) && classLookedFor.isInstance(d.getDesignatedConcept())) {
				res.add(d);
			}
		}
		return res;
	}


	public List<Entity> getEntitiesKnown() {
		return entitiesKnown;
	}

	private boolean isOnWindows() {
		return System.getProperty("os.name").contains("Windows");
	}
}
