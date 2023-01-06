package net.ripe.db.whois.spec.domain

class SyncUpdate {
    String data
    String charset
    boolean help
    boolean diff
    boolean forceNew
    boolean redirect

    def setData(String data) {
        this.data = data.stripIndent(true)
    }

    def setCharset(String charset) {
        this.charset = charset
    }

    def setRawData(String rawData) {
        this.data = rawData;
    }
}
