package net.ripe.db.whois.spec.domain

import javax.mail.internet.MimeMessage

class AckResponse extends Response {

    AckResponse(MimeMessage mimeMessage) {
        super(mimeMessage.subject, mimeMessage.content.toString())
    }

    AckResponse(String subject, String contents) {
        super(subject, contents)
    }

    @Override
    def getSuccess() {
        subject =~ "^SUCCESS"
    }

    @Override
    def getFailed() {
        subject =~ "^FAILED"
    }

    def getQuote() {
        return new Quote()
    }

    class Quote {
        def getFrom() { string(/>\s*From:\s*(.*)/) }

        def getSubject() { string(/>\s*Subject:\s*(.*)/) }

        def getDate() { string(/>\s*Date:\s*(.*)/) }

        def getReplyTo() { string(/>\s*Reply\-To:\s*(.*)/) }

        def getMessageId() { string(/>\s*Message\-ID:\s*(.*)/) }
    }
}
