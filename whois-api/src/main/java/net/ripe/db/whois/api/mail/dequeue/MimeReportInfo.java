package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.internet.MimeMessage;

public class MimeReportInfo {

    private boolean isReportDeliveryStatus;

    private MimeMessage reportReturningMessage;

    public MimeReportInfo() {}

    public MimeReportInfo(final boolean isReportDeliveryStatus, final MimeMessage reportReturningMessage) {
        this.isReportDeliveryStatus = isReportDeliveryStatus;
        this.reportReturningMessage = reportReturningMessage;
    }

    public boolean isReportDeliveryStatus() {
        return isReportDeliveryStatus;
    }

    public void setMimeReportType(boolean isDeliveryType) {
        this.isReportDeliveryStatus = isDeliveryType;
    }

    public MimeMessage getReportReturningMessage() {
        return reportReturningMessage;
    }

    public void setReportReturningMessage(MimeMessage reportReturningMessage) {
        this.reportReturningMessage = reportReturningMessage;
    }
}
