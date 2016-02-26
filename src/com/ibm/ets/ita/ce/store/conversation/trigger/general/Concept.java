package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Concept {
    SERVICE("service");
    private String name;

    private Concept(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
