package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import exceptions.ParserException;
import grammar.AbstractConcept;
import grammar.AbstractEntity;
import grammar.DeclarativeSentence;
import grammar.Designation;
import grammar.Explain;
import grammar.HasSameMeaningAs;
import grammar.InterrogativeWord;
import grammar.Order;
import grammar.Sentence;
import grammar.StartSoftware;
import grammar.Stop;
import grammar.Verb;
import grammar.VerbMeaning;


/*
 * Changer la voix passive selon ce qui était demandé ? : (qui mange la pomme vs john mange quoi) Passive (eg, "John eats an apple" vs "An apple is eaten by John")
 * 
 * secure user input
 * 
 * designation random ou toutes quand plusieurs (pour say)
 * 
 * plusieurs faits dans une phrase (ET, virgule)
 * 
 * besoin de equals() dans certaines classes de grammaire ?
 * 
 * faire de vocabulary une map <designation->Concept> ?
 * 
 * faire des interfaces au lieu de classes pour les concepts pour qu'un mot puisse en etre plusieurs à la fois ?
 * 
 * Lors de la grammaire : et si jamais plusieurs fois le même (nouveau) mot dans une phrase déclarative ? Comme on utilise l'ancien vocab (et pas le nouveau), il n'apparait pas, n'apparait toujours pas, et on crée deux nouveaux concepts pour ce mot au lieu d'un
 * 
 * Deux désignations d'un genre différent peuvent désigner le même concept. C'est le mot/désignation qui a un genre au final, pas le concept lui-même
 */

public class AI {
	/**
	 * A list of facts learned from the outside
	 */
	private List<DeclarativeSentence> knowledge;
	private Scanner kb;
	private boolean stopPrgm;
	private Translator translator;

	public AI() {
		this.translator = new FrenchTranslator(this);
		say("Initializing...");

		this.knowledge = new LinkedList<DeclarativeSentence>();
		this.addBasicKnowledge();
		this.stopPrgm = false;

		kb = new Scanner(System.in);	    
	}


	public void start() {
		say("Ready.");

		while (! stopPrgm) {
			try {
				String userInput = getInput();
				GrammarParser parser = new GrammarParser(translator.getVocabulary());
				Sentence parsedInput = parser.parse(userInput);

				if (parsedInput instanceof Order) {
					obeyOrder((Order) parsedInput);
				}
				else if (parsedInput instanceof DeclarativeSentence) {
					this.translator.getVocabulary().addAll(parser.getNewVocabulary());

					processDeclarativeSentenceFromUser((DeclarativeSentence) parsedInput);
				}
			} catch (ParserException e) {
				say(e.getMessage());
			}

		}

		terminate();
	}

	/**
	 * Called at the beginning
	 */
	private void addBasicKnowledge() {
		// Not much for now !
	}

	private void obeyOrder(Order order) {
		VerbMeaning orderMeaning = order.getVerb().getMeaning();
		if (orderMeaning instanceof Stop) {
			if (order.getObject() == null) {
				this.stopPrgm = true;
			} else {
				try {
					executeCommand("killall " + order.getObject());
					say(order.getObject() + " stopped");
				} catch (IOException e) {
					say("I don't know how to do that");
				} catch (NoSuchElementException e) {
					say("There is no such program to stop");
				}
			}
		} else if (orderMeaning instanceof StartSoftware) {
			startSoftware(order.getObject());
		} else if (orderMeaning instanceof Explain) {
			try {
				say(executeCommand("./showDef " + translator.getLanguageParameterForGetDefProgram() + " " + order.getObject()));
			} catch (IOException e) {
				say("I don't know how to explain that");
			}
		}
	}

	private void startSoftware(String software) {
		try {
			executeBackgroundCommand(software.toLowerCase());
			say(software + " started");
		} catch (IOException e) {
			say("I don't know any software named " + software);
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

	private void processDeclarativeSentenceFromUser(DeclarativeSentence declarativeSentence) {
		if (declarativeSentence.isInterrogative()) {
			answer(declarativeSentence);
		} else {
			this.knowledge.add(declarativeSentence);

			VerbMeaning meaning = ((Verb) declarativeSentence.getVerb()).getMeaning();
			if (meaning instanceof HasSameMeaningAs) {
				mergeEntities(declarativeSentence.getSubject(), declarativeSentence.getObject());
			}

			removeDuplicatesFromKnowledge();
			say("Compris.");
		}
	}

	private void removeDuplicatesFromKnowledge() {
		List<DeclarativeSentence> updatedKnowledge = new LinkedList<DeclarativeSentence>();

		for (DeclarativeSentence sentence : this.knowledge) {
			if (! updatedKnowledge.contains(sentence)) {
				updatedKnowledge.add(sentence);
			}
		}

		this.knowledge = updatedKnowledge;
	}

	/**
	 * Learns that the two given entities refer to the same concept
	 * It is supposed that the vocabulary has designations referring to both parameters 
	 * @param a
	 * @param b
	 */
	private void mergeEntities(AbstractEntity a, AbstractEntity b) {
		for (Designation currDesignation : this.translator.getVocabulary()) {
			if (currDesignation.getDesignatedConcept().equals(a)) {
				currDesignation.setDesignatedConcept(b);
			}
		}

		for (DeclarativeSentence currSentence : this.knowledge) {
			currSentence.replace(a, b);
		}

		// Later, merge their attributes if they have any
	}




	private void answer(DeclarativeSentence question) {
		if (question.isInterrogative()) {
			List<DeclarativeSentence> answers = new LinkedList<DeclarativeSentence>();

			AbstractConcept questionConcepts[] = question.split();

			for (DeclarativeSentence currFact : this.knowledge) {
				boolean answersTheQuestion = true;
				AbstractConcept currFactConcepts[] = currFact.split();
				for (int i = 0 ; i < questionConcepts.length ; i++) { // Looking at the question and potential answer in parallel
					AbstractConcept currentQuestionConcept = questionConcepts[i];
					AbstractConcept currentAnswerConcept = currFactConcepts[i];
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
				say("No idea :(");
			} else {
				say(answers);
			}
		} else {
			System.err.println("Can't answer what is not a question. Should never happen :/");
		}
	}

	private void say(String s) {
		this.translator.say(s);
	}

	private void say(List<DeclarativeSentence> s) {
		this.translator.say(s);
	}


	private void terminate() {
		say("Stopping.");
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
		List<AbstractConcept> allConcepts = getAllConceptsDesignatedBy(vocabulary, designation, classLookedFor);
		if (allConcepts.isEmpty()) {
			return null;
		} else {
			return allConcepts.get(0);
		}
	}


	/**
	 * Searches the given vocabulary for all classLookedFor objects designated by designation.
	 * Returns an empty list if none are found
	 */
	public static List<AbstractConcept> getAllConceptsDesignatedBy(final List<Designation> vocabulary, final String designation, final Class<?> classLookedFor) {
		List<AbstractConcept> res = new LinkedList<AbstractConcept>();
		for (Designation d : vocabulary) {
			if (d.getValue().equals(designation) && classLookedFor.isInstance(d.getDesignatedConcept())) {
				res.add(d.getDesignatedConcept());
			}
		}
		return res;
	}
}
