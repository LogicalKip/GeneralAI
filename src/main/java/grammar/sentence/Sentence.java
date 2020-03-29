package grammar.sentence;

import grammar.verb.Verb;
import lombok.Getter;

public abstract class Sentence {
    public Sentence(Verb verb) {
        this.verb = verb;
    }

    @Getter
    private Verb verb;
}
