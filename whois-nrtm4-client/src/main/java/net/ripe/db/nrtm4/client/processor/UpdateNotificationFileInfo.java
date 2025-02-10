package net.ripe.db.nrtm4.client.processor;


import java.net.URI;

public class UpdateNotificationFileInfo {

    private final URI unfUri;

    private final String source;

    private final String unfSignature;

    public UpdateNotificationFileInfo(final String unfUri, final String source, final String unfSignature){
        this.unfUri = URI.create(unfUri);
        this.source = source;
        this.unfSignature = unfSignature;
    }

    public String getSource() {
        return source;
    }

    public URI getUnfUri() {
        return unfUri;
    }

    public String getUnfSignature() {
        return unfSignature;
    }
}
