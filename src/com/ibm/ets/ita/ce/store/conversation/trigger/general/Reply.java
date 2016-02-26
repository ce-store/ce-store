package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Reply {
    SAVED("I have saved that to the knowledge base");
    private String name;

    private Reply(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
