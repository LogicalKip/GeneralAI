package grammar;

import grammar.sentence.Sentence;
import grammar.verb.Verb;
import lombok.Getter;

public class Order extends Sentence {
    @Getter
    private final String object;

    public Order(Verb verb, String object) {
        super(verb);
        this.object = object;
    }
}
