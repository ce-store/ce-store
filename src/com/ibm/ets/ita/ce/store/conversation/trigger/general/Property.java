package com.ibm.ets.ita.ce.store.conversation.trigger.general;

public enum Property {
    IN_REPLY_TO("is in reply to"),
    IS_FROM("is from"),
    IS_TO("is to"),
    FROM_CONCEPT("from concept"),
    FROM_INSTANCE("from instance"),
    TELL_SERVICE("tell service"),
    CONTENT("content"),
    SINGLE_QUAL("single qualifier"),
    INTERESTED_PARTY("interested party"),
    UNINTERESTED_PARTY("uninterested party"),
    MATCHING_THING("matching thing"),
    TEMPLATE("template"),
    TEMPLATE_STRING("template string"),
    RECIPIENT("recipient"),
    REPLY("reply"),
    KEYWORD("keyword"),
    QUERY("query");

    private String name;

    private Property(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
