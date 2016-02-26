package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Card {
    NL("nl card"), TELL("tell card");
    private String name;

    private Card(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
