package net.ripe.db.whois.internal.api.rnd;

public enum ReferenceType {
    OUTGOING ("outgoing"),
    INCOMING ("incoming");

    private final String outputName;

    ReferenceType(final String outputName) {
        this.outputName = outputName;
    }

    public String getOutputName() {
        return outputName;
    }
}
