package grammar.verb;

import grammar.AbstractConcept;

/**
 * What a particular verb concept means, a meaning. Even if nothing is known about its meaning, it may still be known that several designations refer to a same verb meaning, and therefore that they mean the same thing. 
 * E.g The concept of [eating], without a particular tense
 */
public class Verb extends AbstractConcept {
    public final boolean IS_STATIVE_VERB;
    public final boolean IS_DYNAMIC_VERB;//FIXME when should we set/check the 2 constants ?

    public Verb(boolean isStativeVerb, boolean isDynamicVerb) {
        IS_STATIVE_VERB = isStativeVerb;
        IS_DYNAMIC_VERB = isDynamicVerb;
    }

    public Verb() {
        this(false, true);
    }
}
