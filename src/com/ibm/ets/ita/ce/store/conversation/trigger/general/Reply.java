package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Reply {
    SAVED("I have saved that to the knowledge base"),
    NOT_UNDERSTOOD("I didn't manage to understand any of that, sorry"),
    NEW_INFORMATION("I've found new information on "),
    NEW_INTERESTING("A new interesting thing has been created."),
    STATE_INTEREST("Please state if you are interested in "),
    STATEMENT_MATCHES_MULTIPLE("Your statement matches the "),
    AGENTS(" agents."),
    BE_SPECIFIC("Please be specific and ask again.");

    private String message;

    private Reply(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    public String message() {
        return message;
    }
}
