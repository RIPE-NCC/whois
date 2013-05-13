package spec.domain

class SyncUpdate {
    String data
    boolean help
    boolean diff
    boolean forceNew
    boolean redirect
    boolean post = true
    int responseCode = 200

    def setData(String data) {
        this.data = data.stripIndent()
    }
}
