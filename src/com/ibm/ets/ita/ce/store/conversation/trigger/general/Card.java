package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Card {
    NL("nl card"), GIST("gist card"), TELL("tell card"), CONFIRM("confirm card");
    private String name;

    private Card(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
