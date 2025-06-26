package net.ripe.db.whois.query.dao;

import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.BlockEvents;
import net.ripe.db.whois.query.acl.SSOResourceConfiguration;

import java.time.LocalDate;
import java.util.List;

public interface SSOAccessControlListDao extends SSOResourceConfiguration.Loader {
    void saveAclEvent(String ssoId, LocalDate date, int limit, BlockEvent.Type type);

    List<BlockEvents> getTemporaryBlocks(LocalDate blockTime);

    void savePermanentBlock(String ssoId, LocalDate date, int limit, String comment);

    void removePermanentBlocksBefore(LocalDate date);

    void removeBlockEventsBefore(LocalDate date);
}
