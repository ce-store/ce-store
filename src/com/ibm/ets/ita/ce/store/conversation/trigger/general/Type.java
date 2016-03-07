package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Type {
    PROPERTY("property"),
    CONCEPT("concept");
    private String message;

    private Type(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
