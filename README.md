# GeneralAI
You say something to the AI, and the AI says or does something in response. Uses the awesome library SimpleNLG-EnFr.

Input/output currently in French. Some abstraction has been made to allow English, but that would probably still require some tweaking, if at all possible (SimpleNLG-EnFr seems to be made for French).

To use the word defining feature, add showDef to your PATH (Linux).

Intended for Linux. Everything works as well on Windows, except defining words, starting and stopping programs (though if you know how to do those on Windows, you're welcome to help).


Stuff to know before using the AI :
- Wait a few seconds before typing your first sentence (wait for "Ready."). 
- Indefinite determiners ("a") can be seen as "declaring" an entity, as you would a variable in programming. "Un chat" means "So, let there be a cat, a new one, not any of the ones you already know, if any". On the other hand, definite determiners ("the") refer to an entity already "defined". "Le chat" means "the last cat we talked about" and "Le petit chat noir" means "the last cat we talked about that was both small and black". For ease of use, you can still use definite determiners to refer to a new concept (such as "le chat" when it's the very first sentence)
- Nouns that are not in the lexicon are considered of the default gender by SNLG, even if given a determiner of the proper gender. Therefore the output may show incorrect genders if you use unusual nouns, but that'll have to do for now.
- Use question marks iff you ask questions.
- For now, the software starting feature only works (on Linux and) if the PATH contains a command which is the name of the software (i.e "démarrer firefox" works because "firefox" is also the bash command). This is also true when killing the processes.
- You may use first and second singular personnal pronouns ("je" and "tu") as subjects only, even though the AI may use them otherwise during output. The other pronouns are not yet implemented.
- Don't use capital letters (except in the name of the software you want to start/kill, since it will be lowercased anyway)
- Don't use plural.
- Don't use complicated tenses or passive voice. All valid input tenses will be parsed to present.
- Don't use apostrophes (')

Examples of sentences you could type (French) :

un petit chat noir mange une souris blanche

la souris est jolie

qui mange la souris ?

la souris mange qui ?

la souris mange le fromage

une grosse souris mange une pomme

la souris mange quoi ?

la blanche souris mange quoi ?

une bête mange un oiseau beau (or "une bête mange un bel oiseau")

qui mangerait qui ?

un chat signifie une bête

la souris ne mange pas la bête

quoi mange quoi ?

quoi ne mange pas quoi ?

la souris mange le chat ?

la souris ne mange pas le chat ???

qui mangera quoi ?

je comprends le principe

tu comprends la phrase

qui comprend quoi ?

lance Firefox

arrête firefox

éteins blender

explique firefox

explique intelligence_artificielle

arrête