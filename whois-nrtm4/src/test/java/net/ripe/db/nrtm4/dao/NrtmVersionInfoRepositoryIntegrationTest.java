package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.AbstractNrtm4IntegrationBase;
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

    @Test
    public void result_is_not_present_when_source_is_not_populated() {
        final Optional<NrtmVersionInfo> version = nrtmVersionInfoRepository.findLastVersion();
        assertThat(version.isPresent(), is(false));
    }

    @Test
    public void first_version_is_one() {
        sourceRepository.createSources();
        nrtmVersionInfoRepository.createInitialVersion(sourceRepository.getWhoisSource().orElseThrow(), 1);
        final Optional<NrtmVersionInfo> version = nrtmVersionInfoRepository.findLastVersion();
        assertThat(version.isPresent(), is(true));
        assertThat(version.get().source().getId(), is(sourceRepository.getWhoisSource().orElseThrow().getId()));
        assertThat(version.get().source().getSource(), is(sourceRepository.getWhoisSource().orElseThrow().getSource()));
        assertThat(version.get().version(), is(1L));
    }

    @Test
    public void source_is_unique() {
        sourceRepository.createSources();
        final Exception thrown = assertThrows(
            DuplicateKeyException.class,
            () -> sourceRepository.createSources(),
            "Expected nrtmVersionDao.createNew(...) to throw DuplicateKeyException"
        );
        assertThat(thrown.getMessage(), containsString("Duplicate entry 'TEST'"));
    }

}
