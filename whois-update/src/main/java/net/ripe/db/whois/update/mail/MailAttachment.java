package net.ripe.db.whois.update.mail;

import org.springframework.core.io.InputStreamSource;

public class MailAttachment {

    private String attachmentFilename;
    private InputStreamSource inputStreamSource;
    private String contentType;

    public MailAttachment(String attachmentFilename, InputStreamSource inputStreamSource, String contentType) {
        this.attachmentFilename = attachmentFilename;
        this.inputStreamSource = inputStreamSource;
        this.contentType = contentType;
    }

    public String getAttachmentFilename() {
        return attachmentFilename;
    }

    public void setAttachmentFilename(String attachmentFilename) {
        this.attachmentFilename = attachmentFilename;
    }

    public InputStreamSource getInputStreamSource() {
        return inputStreamSource;
    }

    public void setInputStreamSource(InputStreamSource inputStreamSource) {
        this.inputStreamSource = inputStreamSource;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
