package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.AbstractNrtm4IntegrationBase;
import net.ripe.db.nrtm4.NrtmSourceHolder;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Tag("IntegrationTest")
public class NrtmVersionInfoRepositoryIntegrationTest extends AbstractNrtm4IntegrationBase {

    @Autowired
    private NrtmVersionInfoRepository nrtmVersionInfoRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private NrtmSourceHolder nrtmSourceHolder;

    @Test
    public void result_is_not_present_when_source_is_not_populated() {
        final Optional<NrtmVersionInfo> version = nrtmVersionInfoRepository.findLastVersion();
        assertThat(version.isPresent(), is(false));
    }

    @Test
    public void first_version_is_one() {
        sourceRepository.createSource(nrtmSourceHolder.getSource());
        nrtmVersionInfoRepository.createInitialVersion(nrtmSourceHolder.getSource(), 1);
        final Optional<NrtmVersionInfo> version = nrtmVersionInfoRepository.findLastVersion();
        assertThat(version.isPresent(), is(true));
        assertThat(version.get().getSource().source().name(), is(nrtmSourceHolder.getSource().name()));
        assertThat(version.get().getVersion(), is(1L));
    }

    @Test
    public void source_is_unique() {
        sourceRepository.createSource(nrtmSourceHolder.getSource());
        final Exception thrown = assertThrows(
            DuplicateKeyException.class,
            () -> sourceRepository.createSource(nrtmSourceHolder.getSource()),
            "Expected nrtmVersionDao.createNew(...) to throw DuplicateKeyException"
        );
        assertThat(thrown.getMessage(), containsString("Duplicate entry 'TEST'"));
    }

}
