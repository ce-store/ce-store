package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Reply {
    SAVED("I have saved that to the knowledge base"),
    NOT_UNDERSTOOD("I didn't manage to understand any of that, sorry");
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
