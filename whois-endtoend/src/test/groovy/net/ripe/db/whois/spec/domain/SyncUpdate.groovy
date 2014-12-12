package net.ripe.db.whois.spec.domain

class SyncUpdate {
    String data
    boolean help
    boolean diff
    boolean forceNew
    boolean redirect

    def setData(String data) {
        this.data = data.stripIndent()
    }

    def setRawData(String rawData) {
        this.data = rawData;
    }
}
