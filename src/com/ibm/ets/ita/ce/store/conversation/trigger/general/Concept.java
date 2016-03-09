package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Concept {
    SERVICE("service"),
    COMMON_WORDS("common word list"),
    NEGATION_WORDS("negation word list"),
    INTERESTING("interesting thing"),
    UNINTERESTING("uninteresting property"),
    CONV_THING("conv thing"),
    MAN("man"),
    WOMAN("woman"),
    AGENT("agent");

    private String name;

    private Concept(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
