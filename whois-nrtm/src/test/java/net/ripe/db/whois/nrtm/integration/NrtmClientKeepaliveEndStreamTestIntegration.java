package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.IntegrationTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.annotation.DirtiesContext;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmClientKeepaliveEndStreamTestIntegration extends NrtmClientTestIntegration {

    @BeforeClass
    public static void beforeKeepaliveClass() {
        System.setProperty("nrtm.keepalive.end.of.stream", "true");
    }

    @Test
    public void check_mntner_exists() {
        super.check_mntner_exists();
    }

    @Test
    public void add_person_from_nrtm() {
        super.add_person_from_nrtm();
    }

    @Test
    public void add_mntner_from_nrtm_gap_in_serials() {
        super.add_mntner_from_nrtm_gap_in_serials();
    }

    @Test
    public void delete_maintainer_from_nrtm() {
        super.delete_maintainer_from_nrtm();
    }

    @Test
    public void create_and_update_mntner_from_nrtm() {
        super.create_and_update_mntner_from_nrtm();
    }

    @Test
    public void network_error() {
        super.network_error();
    }

    @Test
    public void ensure_all_changes_of_object_are_imported_with_no_missing_references() {
        super.ensure_all_changes_of_object_are_imported_with_no_missing_references();
    }
}
