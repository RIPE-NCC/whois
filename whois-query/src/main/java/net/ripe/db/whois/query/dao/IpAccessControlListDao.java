package net.ripe.db.whois.query.dao;

import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.BlockEvents;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import java.time.LocalDate;

import java.util.List;

public interface IpAccessControlListDao extends IpResourceConfiguration.Loader {
    void saveAclEvent(IpInterval<?> interval, LocalDate date, int limit, BlockEvent.Type type);

    List<BlockEvents> getTemporaryBlocks(LocalDate blockTime);

    void savePermanentBlock(IpInterval<?> interval, LocalDate date, int limit, String comment);

    void removePermanentBlocksBefore(LocalDate date);

    void removeBlockEventsBefore(LocalDate date);
}
