package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.AbstractNrtm4IntegrationBase;
import net.ripe.db.nrtm4.domain.NrtmSourceHolder;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@Tag("IntegrationTest")
public class NrtmVersionInfoRepositoryIntegrationTest extends AbstractNrtm4IntegrationBase {

    @Autowired
    private NrtmVersionInfoRepository nrtmVersionInfoRepository;

    @Autowired
    private NrtmSourceHolder nrtmSourceHolder;

    @Test
    public void result_is_not_present_when_source_is_not_populated() {
        final Optional<NrtmVersionInfo> version = nrtmVersionInfoRepository.findLastVersion(nrtmSourceHolder.getSource());
        assertThat(version.isPresent(), is(false));
    }

    @Test
    public void first_version_is_one() {
        nrtmVersionInfoRepository.createInitialVersion(nrtmSourceHolder.getSource(), 1);
        final Optional<NrtmVersionInfo> version = nrtmVersionInfoRepository.findLastVersion(nrtmSourceHolder.getSource());
        assertThat(version.isPresent(), is(true));
        assertThat(version.get().getSource(), is(nrtmSourceHolder.getSource()));
        assertThat(version.get().getVersion(), is(1L));
    }

}
