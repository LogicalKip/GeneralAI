package grammar.sentence;

import grammar.verb.Verb;

public abstract class Sentence {
    public Sentence(Verb verb) {
        this.verb = verb;
    }

    private Verb verb;

    public Verb getVerb() {
        return verb;
    }
}
