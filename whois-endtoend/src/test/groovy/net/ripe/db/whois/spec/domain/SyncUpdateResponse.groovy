package net.ripe.db.whois.spec.domain

class SyncUpdateResponse extends Response {

    SyncUpdateResponse(String response) {
        super("", response);
        this.contents = response
    }

    @Override
    def getSuccess() {
        !getFailed()
    }

    @Override
    def getFailed() {
        hasAnyFailed() || !hasAnySucceeded()
    }

    boolean hasAnyFailed(){
        this.contents =~ "\\s+FAILED:"
    }

    boolean hasAnySucceeded(){
        this.contents =~ "\\s+SUCCEEDED:"
    }
}