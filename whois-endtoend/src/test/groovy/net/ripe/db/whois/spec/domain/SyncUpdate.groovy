package net.ripe.db.whois.spec.domain

import jakarta.ws.rs.core.MultivaluedMap

class SyncUpdate {
    String data
    String charset
    MultivaluedMap<String, String> headers
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

    def setHeaders(MultivaluedMap<String, String> headers) {
        this.headers = headers
    }

    def setRawData(String rawData) {
        this.data = rawData;
    }
}
