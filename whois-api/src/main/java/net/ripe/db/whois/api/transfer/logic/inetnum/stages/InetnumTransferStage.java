package net.ripe.db.whois.api.transfer.logic.inetnum.stages;

import net.ripe.commons.ip.Ipv4Range;
import net.ripe.db.whois.api.transfer.logic.TransferStage;


public abstract class InetnumTransferStage extends TransferStage<Ipv4Range> {

    public InetnumTransferStage(String source) {
        super(source);
    }


}
