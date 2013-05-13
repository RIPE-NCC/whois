package net.ripe.db.whois.update.domain;

public enum Action {
    CREATE("Create"),
    MODIFY("Modify"),
    DELETE("Delete"),
    NOOP("Noop");

    private final String description;

    private Action(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
