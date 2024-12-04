package net.ripe.db.nrtm4.client.importer;

import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;

public interface MirrorImporter {

    void doImport(final String source, final UpdateNotificationFileResponse updateNotificationFile);
}
