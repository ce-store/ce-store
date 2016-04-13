package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Prefix {
    UNKNOWN("unknown_");

    private String name;

    private Prefix(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
