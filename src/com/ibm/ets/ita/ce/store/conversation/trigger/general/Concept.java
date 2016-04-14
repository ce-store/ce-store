package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Concept {
    SERVICE("service"),
    COMMON_WORDS("common word list"),
    NEGATION_WORDS("negation word list"),
    INTERESTING("interesting thing"),
    UNINTERESTING("uninteresting property"),
    UNINTERESTING_RULE("uninteresting rule"),
    CONV_THING("conv thing"),
    MAN("man"),
    WOMAN("woman"),
    AGENT("agent"),
    COMMAND_WORD("command word"),
    PROPERTY_TEMPLATE("property template"),
    INSTANCE_TEMPLATE("instance template"),
    CONCEPT_TEMPLATE("concept template"),
    CONFIRM_REPLY("confirm reply"),
    POSITIVE_CONFIRM_REPLY("positive confirm reply"),
    NEGATIVE_CONFIRM_REPLY("negative confirm reply");

    private String name;

    private Concept(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
