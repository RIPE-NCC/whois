package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.planner.RpslAttributes;

public class ApiResponseHandlerAbuseC extends ApiResponseHandler  {
    RpslAttributes abuseContactInfo = null;

    @Override
    public void handle(final ResponseObject responseObject) {
        System.out.println("APIRESPONSEHANDLER:" + responseObject + "," + responseObject.getClass());
        if (responseObject instanceof RpslAttributes) {
            this.abuseContactInfo = (RpslAttributes)responseObject;
        }
    }

    public RpslAttributes getAbuseContactInfo() {
        return abuseContactInfo;
    }
}
