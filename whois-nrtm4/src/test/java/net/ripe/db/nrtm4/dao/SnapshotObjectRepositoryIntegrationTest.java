package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.AbstractNrtm4IntegrationBase;
import net.ripe.db.nrtm4.domain.NrtmSourceHolder;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@Tag("IntegrationTest")
public class SnapshotObjectRepositoryIntegrationTest extends AbstractNrtm4IntegrationBase {

    @Autowired
    private SnapshotObjectRepository snapshotObjectRepository;

    @Autowired
    private SnapshotObjectDao snapshotObjectDao;

    @Autowired
    private NrtmVersionInfoRepository nrtmVersionInfoRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private NrtmSourceHolder source;

    @Test
    void inserts_payloads_and_callback_results() throws IOException {
        sourceRepository.createSource(source.getSource());
        final var version = nrtmVersionInfoRepository.createInitialVersion(source.getSource(), 0);
        snapshotObjectRepository.insert(version.getId(), 1, 1, inetnumObject);
        snapshotObjectRepository.insert(version.getId(), 2, 1, orgObject);
        final var list = new ArrayList<String>();
        snapshotObjectDao.consumeAllObjects(source.getSource(), list::add);
        assertThat(list.get(0), is("inetnum:        193.0.0.0 - 193.255.255.255\nsource:         TEST\n"));
        assertThat(list.get(1), is("organisation:   ORG-XYZ99-RIPE\norg-name:       XYZ B.V.\norg-type:       OTHER\naddress:        Zürich\naddress:        NETHERLANDS\nmnt-by:         XYZ-MNT\nmnt-ref:        PQR-MNT\nabuse-c:        XYZ-RIPE\ncreated:        2018-01-01T00:00:00Z\nlast-modified:  2019-12-24T00:00:00Z\nsource:         TEST\n"));
    }

    @Test
    void inserts_and_deletes_payloads_and_executes_callbacks() {
        sourceRepository.createSource(source.getSource());
        final var version = nrtmVersionInfoRepository.createInitialVersion(source.getSource(), 0);
        snapshotObjectRepository.insert(version.getId(), 1, 1, inetnumObject);
        snapshotObjectRepository.insert(version.getId(), 2, 1, orgObject);
        snapshotObjectRepository.delete(2);
        final var list = new ArrayList<String>();
        snapshotObjectDao.consumeAllObjects(source.getSource(), list::add);
        assertThat(list.get(0), is("inetnum:        193.0.0.0 - 193.255.255.255\nsource:         TEST\n"));
        assertThat(list.get(0), is("inetnum:        193.0.0.0 - 193.255.255.255\nsource:         TEST\n"));
    }

    private final RpslObject inetnumObject = RpslObject.parse("inetnum:        193.0.0.0 - 193.255.255.255\nsource:         TEST");
    private final RpslObject orgObject = RpslObject.parse("organisation:   ORG-XYZ99-RIPE\norg-name:       XYZ B.V.\norg-type:       OTHER\naddress:        Zürich\naddress:        NETHERLANDS\nmnt-by:         XYZ-MNT\nmnt-ref:        PQR-MNT\nabuse-c:        XYZ-RIPE\ncreated:        2018-01-01T00:00:00Z\nlast-modified:  2019-12-24T00:00:00Z\nsource:         TEST");

}
