package net.ripe.db.whois.common.support;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UpdateLogEntryTest {

    @Test
    public void upd_noop_inetnum_rest_api() {
        assertThat(UpdateLogEntry.parse(
            "20150508 03:45:40 [ 9984] 19.73 ms   UPD NOOP   inetnum      " +
            "10.0.0.0 - 10.0.0.255 (1) SUCCESS               : " +
            "<E0,W1,I1> AUTH OVERRIDE - WhoisRestApi(127.0.0.1)").getPrimaryKey(), is("10.0.0.0 - 10.0.0.255"));
    }

    @Test
    public void dry_noop_mntner_syncupdate() {
        assertThat(UpdateLogEntry.parse(
            "20150805 00:02:27 [413949] 18.91 ms   DRY NOOP   mntner       " +
            "AA1-MNT                       (1) SUCCESS               : " +
            "<E0,W1,I1> AUTH PWD - SyncUpdate(127.0.0.1)").getPrimaryKey(), is("AA1-MNT"));
    }

    @Test
    public void upd_modify_organisation_failed_auth() {
        assertThat(UpdateLogEntry.parse(
            "20150805 16:44:57 [421894] 129.6 ms   UPD MODIFY organisation " +
            "ORG-AA1-RIPE                  (1) FAILED_AUTHENTICATION : " +
            "<E1,W1,I0> AUTH PWD,SSO - WhoisRestApi(127.0.0.1)").getPrimaryKey(), is("ORG-AA1-RIPE"));
    }

    @Test
    public void upd_create_inet6num_mailupdate() {
        assertThat(UpdateLogEntry.parse(
            "20150805 16:01:27 [421508] 144.0 ms   UPD CREATE inet6num     " +
            "2a02:aa01:ff00:f8f8::/64       (2) SUCCESS               : " +
            "<E0,W1,I0> AUTH PWD - Mail(<auto-000024602071@fe02.example.com>)").getPrimaryKey(), is("2a02:aa01:ff00:f8f8::/64"));
    }

    @Test
    public void upd_create_inet6num_syncupdate_failed() {
        assertThat(UpdateLogEntry.parse(
            "20150805 17:10:02 [422105] 10.93 ms   UPD CREATE inet6num     " +
            "2a01:aa01:ff0::/48             (1) FAILED                : " +
            "<E0,W1,I0> AUTH PWD - SyncUpdate(2a01:a500:1::2a02)").getPrimaryKey(), is("2a01:aa01:ff0::/48"));
    }

    @Test
    public void upd_create_inetnum_failed_auth() {
        assertThat(UpdateLogEntry.parse(
             "20190206 12:00:04 [3066741] 1.485 min  UPD CREATE inetnum      " +
             "10.0.0.0 - 10.0.0.255     (1) FAILED_AUTHENTICATION : " +
              "<E3,W0,I0> AUTH PWD - Mail(<2191ba665c5d45478f98600e43bb68cf@TELMBXB07RM001.example.local>)").getPrimaryKey(), is("10.0.0.0 - 10.0.0.255"));
    }

    @Test
    public void null_action_syncupdate() {
        assertThat(UpdateLogEntry.parse(
             "20190528 16:10:05 [624552] 14.79 ms   UPD null   inet6num     " +
              "2a01:aa01:ff0::/48            (1) FAILED                : " +
               "<E1,W0,I0> AUTH PWD - SyncUpdate(127.0.0.1)").getPrimaryKey(), is("2a01:aa01:ff0::/48"));
    }

    @Test
    public void null_action_rest_api() {
        assertThat(UpdateLogEntry.parse(
            "20150207 13:26:29 [214573] 1.413 s    UPD null   inetnum      " +
                "10.0.0.0 - 10.0.0.255  (1) FAILED                : " +
                "<E1,W0,I0> AUTH PWD - WhoisRestApi(127.0.0.1)").getPrimaryKey(), is("10.0.0.0 - 10.0.0.255"));
    }

    @Test
    public void get_credentials() {
        assertThat(UpdateLogEntry.parse(
            "20180730 00:03:31 [445067] 172.7 ms   UPD CREATE inetnum      " +
             "10.0.0.0 - 10.255.255.255 (1) SUCCESS               : " +
              "<E0,W0,I1> AUTH PWD,SSO - WhoisRestApi(2a01:aa01::250)").getCredentials(), containsInAnyOrder("SSO", "PWD"));
    }

    @Test
    public void get_origin_type() {
        assertThat(UpdateLogEntry.parse(
         "20180730 00:03:31 [445067] 172.7 ms   UPD CREATE inetnum      " +
         "10.0.0.0 - 10.255.255.255 (1) SUCCESS               : " +
         "<E0,W0,I1> AUTH PWD,SSO - WhoisRestApi(2a01:aa01::250)").getOriginType(), is("WhoisRestApi"));
    }

    @Test
    public void get_origin_id_rest_api() {
        assertThat(UpdateLogEntry.parse(
            "20180730 00:03:31 [445067] 172.7 ms   UPD CREATE inetnum      " +
            "10.0.0.0 - 10.255.255.255 (1) SUCCESS               : " +
            "<E0,W0,I1> AUTH PWD,SSO - WhoisRestApi(2a01:aa01::250)").getOriginId(), is("2a01:aa01::250"));
    }

    @Test
    public void get_origin_id_mailupdate() {
        assertThat(UpdateLogEntry.parse(
             "20190206 12:00:04 [3066741] 1.485 min  UPD CREATE inetnum      " +
             "10.0.0.0 - 10.0.0.255     (1) FAILED_AUTHENTICATION : " +
              "<E3,W0,I0> AUTH PWD - Mail(<2191ba665c5d45478f98600e43bb68cf@TELMBXB07RM001.example.local>)").getOriginId(), is("<2191ba665c5d45478f98600e43bb68cf@TELMBXB07RM001.example.local>"));
    }
}
