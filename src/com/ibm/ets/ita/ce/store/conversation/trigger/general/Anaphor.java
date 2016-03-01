package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Anaphor {
    HE("he"), HIS("his"), HIM("him"), SHE("she"), HER("her"), IT("it"), THEY("they");
    private String name;

    private Anaphor(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
