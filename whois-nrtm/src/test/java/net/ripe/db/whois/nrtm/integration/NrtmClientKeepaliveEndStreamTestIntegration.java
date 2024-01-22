package net.ripe.db.whois.nrtm.integration;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmClientKeepaliveEndStreamTestIntegration extends NrtmClientTestIntegration {

    @BeforeAll
    public static void beforeKeepaliveClass() {
        System.setProperty("nrtm.keepalive.end.of.stream", "true");
    }

    @AfterAll
    public static void afterKeepaliveClass() {
        System.clearProperty("nrtm.keepalive.end.of.stream");
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
    public void add_mntner_from_nrtm_gap_in_serials() throws InterruptedException {
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
    public void network_error() throws InterruptedException {
        super.network_error();
    }

    @Test
    public void ensure_all_changes_of_object_are_imported_with_no_missing_references() throws InterruptedException {
        super.ensure_all_changes_of_object_are_imported_with_no_missing_references();
    }
}
